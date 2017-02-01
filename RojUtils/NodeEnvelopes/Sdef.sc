Sdef {

	classvar <>library;
	classvar rate;
	classvar hasInitSynthDefs, bufferSynthDef;

	var <key, <path;
	var <bus;
	var <buffers, <currentSynth, <releasedSynths;
	var <layers;
	var <parentNode;
	var clock;
	var hasPlotWin;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
		// rate = 44100;
		rate = 44100 / 64;
		hasInitSynthDefs = false;
	}

	*new { |name = nil, index = nil|
		var sDef;
		if(hasInitSynthDefs.not) { this.initSynthDefs; };

		if(name.asArray.notEmpty)
		{
			sDef = this.exist(name);
			if(sDef.isNil) { sDef = super.new.init(name).initBus };
		}
		{ sDef = super.new.init(nil) };

		if(index.notNil)
		{
			var layer =	sDef.layers.at(index);
			if(layer.isNil) {
				layer = SdefLayer(sDef, index);
				sDef.layers.put(index, layer);
			};
			^layer;
		}
		{ ^sDef }
	}

	*exist { |name|
		var path = name.asArray ++ \def;
		var sDef = this.library.atPath(path);
		if(sDef.notNil) { ^sDef; } { ^nil; }
	}

	*printAll { this.library.postTree; ^nil; }

	*frame { |time| ^ rate * time }
	*time { |frame| ^ frame / rate }

	// init //////////////////////////



	init { |name|
		this.key = name;
		hasPlotWin = false;

		bus = nil;
		currentSynth = nil;
		releasedSynths = Order.new;
		buffers = Order.new;

		layers = Order.new;
		layers.put(0, SdefLayer(this, 0));

		parentNode = nil;

		if(currentEnvironment.isKindOf(ProxySpace))
		{ clock = currentEnvironment.clock }
		{ clock = TempoClock.default };
	}

	initBus {
		if(Server.default.serverRunning.not)
		{
			Server.default.onBootAdd({
				bus = Bus.control(Server.default, 1);
				"\t- Sdef(%) alloc control bus at index %".format(this.key, bus.index).postln;
			});
		}
		{ bus = Bus.control(Server.default, 1) }
	}

	// instance //////////////////////////

	key_ {|name|
		if(name.notNil)
		{
			key = name;
			if(path.notNil) { library.removeEmptyAtPath(path) };
			path = key.asArray ++ \def;
			library.putAtPath(path, this);
		}
	}

	layerCount { ^layers.lastIndex }

	signal {
		var lastIndex = layers.lastIndex;
		if(lastIndex.isNil) { ^nil } { ^layers.at(lastIndex).signal };
	}

	duration {
		var lastIndex = layers.lastIndex;
		if(lastIndex.isNil) { ^nil } { ^layers.at(lastIndex).duration };
	}

	render {
		// var startRenderTime = SystemClock.beats;
		var fTime = 8;
		if(parentNode.notNil) {	fTime = parentNode.fadeTime };
		if(parentNode.monitor.isPlaying) { this.fadeInSynth(fTime) };
	}


	*initSynthDefs{
		if(Server.default.serverRunning.not) { Server.default.onBootAdd({ this.initSynthDefs }) }
		{
			bufferSynthDef = { |bus, bufnum, startTime = 0, multFrom = 0, multTo = 0, fTime = 0, tempo = 1|
				var buf, mult;
				buf = PlayBuf.kr(
					numChannels: 1,
					bufnum: bufnum,
					startPos: startTime * rate,
					rate: tempo,
					trigger: \reset.tr,
					loop: 1
				);

				mult = EnvGen.kr(
					envelope: Env([ multFrom, multTo ], fTime, \lin),
					gate: \multTrig.tr(0),
					timeScale: tempo.reciprocal,
					doneAction: 0
				);

				XOut.kr(bus, mult, buf);
			}.asSynthDef;

			// controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			"\nSdef initialization of SynthDefs done. Control rate set on %".format(rate).postln;
		};
		hasInitSynthDefs = true;
	}

	fadeInSynth { |fTime|
		var buf, syn;
		var group = parentNode.group ? RootNode(Server.default);
		var time2quant = clock.timeToNextBeat(this.duration);

		buf = Buffer.alloc(
			server: Server.default,
			numFrames: this.signal.size,
			numChannels: 1
		);

		buf.loadCollection(
			collection: this.signal,
			startFrame: 0,
			action: {|buff| { this.updatePlot; }.defer }
		);

		this.fadeOutSynth(fTime);

		// {
		bufferSynthDef.name_("Sdef(%)".format(this.printName));
		currentSynth = bufferSynthDef.play(
			target: group,
			args:
			[
				\bus: bus,
				\bufnum: buf.bufnum,
				\startTime: (this.duration - time2quant),
				\reset: 1,
				\multTrig: 1,
				\multFrom: 0,
				\multTo: 1,
				\fTime: (fTime * clock.tempo),
				\tempo, clock.tempo
			]
		);

		currentSynth.onFree({|freeSynth|
			var id = freeSynth.nodeID;
			releasedSynths.removeAt(id);
			// releasedSynths.postln;
			// "% DELETED".format(freeSynth).warn;
		});

	}

	fadeOutSynth { |fTime = 0|
		if (currentSynth.notNil) {
			var id = currentSynth.nodeID;
			releasedSynths.put(id, currentSynth);
			releasedSynths.do({|oldSynth|
				oldSynth.set(
					\multTrig, 1,
					\multFrom, 1,
					\multTo, 0,
					\fTime, (fTime * clock.tempo),
					\tempo, clock.tempo
				);
				{
					(fTime * clock.tempo).wait;
					oldSynth.free;
				}.fork;
			});
			currentSynth = nil;
		};
	}

	kr { ^BusPlug.for(bus)	}

	setNode { |nodeProxy, controlName|
		parentNode = nodeProxy;
		nodeProxy.map(controlName.asSymbol, BusPlug.for(bus));
		this.addDependencyOnNode;
	}

	addDependencyOnNode {
		if(parentNode.notNil)
		{
			if(parentNode.dependants.matchItem(this).not)
			{
				parentNode.addDependant(this);
			}
		}
	}

	removeDependencyOnNode {
		if(parentNode.notNil)
		{
			if(parentNode.dependants.matchItem(this))
			{
				parentNode.removeDependant(this);
			}
		};
		// parentNode.dependants.postln;
	}

	update { |from, what, args| // object dependency -> this is target when object.changed is called
		// var offsetTime = this.duration - clock.timeToNextBeat(this.duration);
		// "\nSdef.update \n\tfrom:% \n\twhat:% \n\targs:%".format(from, what, args).postln;
		case
		{ what.asSymbol == \play } {  this.play(from.fadeTime); "update PLAY".warn; }
		{ what.asSymbol == \stop } {  this.stop(args[0]);  "update STOP".warn; }
		{ what.asSymbol == \free } {  this.stop(args[0]);  "update FREE".warn;  }
		// { what.asSymbol == \set } { }
		;
	}

	// controlAll //////////////////////////

	*play { |from = 0, to = nil|
		library.leafDo({|path|
			var sDef = library.atPath(path);
			sDef.removeDependencyOnNode;
			if(sDef.parentNode.monitor.isPlaying.not) { sDef.parentNode.play };
			sDef.play;
			sDef.addDependencyOnNode;
			/*
			if(sDef.parentNode.notNil) {
			if(sDef.parentNode.monitor.isPlaying.not)
			{ sDef.parentNode.play }
			// { sDef.p

			};
			*/
		})
	}

	*stop {
		library.leafDo({|path|
			var sDef = library.atPath(path);
			sDef.removeDependencyOnNode;
			sDef.stop;
			if(sDef.parentNode.monitor.isPlaying) { sDef.parentNode.stop };
			// sDef.addDependencyOnNode;
			// if(sDef.parentNode.notNil) {
			// sDef.parentNode.removeDependant(this);
			// if(sDef.parentNode.monitor.isPlaying) { sDef.parentNode.stop }
			// };
		});
	}


	play { |time|
		this.fadeInSynth(time)
		// var bufferFramesCnt = buffer.numFrames;
		// var group = parentNode.group ? RootNode(Server.default);
		// var time2quant = clock.timeToNextBeat(this.duration);
		// var startTime = from ? 0;
		// var endTime = to ? this.duration;

		// if(parentNode.notNil) { group = parentNode.group };
		/*
		if(synth.notNil) { synth.free };

		bufferSynthDef.name_("Sdef(%)".format(this.printName));
		synth =	bufferSynthDef.play(
		target: group,
		args:
		[
		\bus: bus,
		\bufnum: buffer.bufnum,
		// \startTime: (this.duration - time2quant),
		\startTime: startTime,
		\tempoClock: clock.tempo
		]
		);
		"Sdef play".warn;
		*/
	}

	stop { |time|
		{
			this.fadeOutSynth(time);
			if(time.notNil) { (time * clock.tempo).wait; };
			// if(synth.notNil) {
			// synth.free;
			// synth = nil;
			// };
			// if(parentNode.notNil) { parentNode.stop };
			// if(currentSynth.notNil) { currentSynth
			bus.set(0);
			// "Sdef stop".warn;
		}.fork;
	}

	// informations //////////////////////////

	printOn { |stream|	stream << this.class.name << "('" << this.key << "' | cnt: " << this.layerCount << ")"; }

	printName {
		var txtPath = "";
		path.do({|oneFolder|
			if(txtPath.isEmpty)
			{ txtPath = "%%".format("\\", oneFolder); }
			{
				if(oneFolder != \def)
				{ txtPath = "%%%".format(txtPath,"\\", oneFolder); }
			}
		});
		^txtPath;
	}

	plot {
		if(this.signal.notNil)
		{
			var winName = "Sdef(%)".format(this.printName);
			var plotWin = nil;

			Window.allWindows.do({|oneW| if(winName.asSymbol == oneW.name.asSymbol) { plotWin = oneW } });

			if(plotWin.isNil)
			{
				var plotter = this.signal.plot(
					name: winName.asSymbol,
					bounds: Rect(700,680,500,300)
				);
				plotter.parent.alwaysOnTop_(true);
				plotter.parent.view.background_(Color.new255(30,30,30)).alpha_(0.9);
				plotter.parent.onClose_({ hasPlotWin = false });
				hasPlotWin = true;
			};

			this.updatePlot;
		}
		{ "% signal is empty".format(this).warn; };
	}

	updatePlot {
		if(hasPlotWin)
		{
			var winName = "Sdef(%)".format(this.printName);
			var windows = Window.allWindows;
			var plotWin = nil;
			var plotter;

			Window.allWindows.do({|oneW| if(winName.asSymbol == oneW.name.asSymbol) { plotWin = oneW; }	});

			if(plotWin.notNil)
			{
				plotWin.view.children[0].close;
				plotter = Plotter(
					name: winName.asSymbol,
					parent: plotWin
				);
				plotWin.view.children[0].bounds_(Rect(8,8,plotWin.view.bounds.width-16,plotWin.view.bounds.height-16));
				plotter.value = this.signal;
				// plotter.domainSpecs = [[0,  super.class.time(this.signal.size), 0, 0, "", " s"]];
				plotter.domainSpecs = [[0, this.duration, 0, 0, "", " s"]];
				plotter.setProperties (
					\backgroundColor, Color.new255(30,30,30),
					\plotColor, Color.new255(30,190,230),
					\fontColor, Color.new255(90,90,90),
					\gridColorX, Color.new255(60,60,60),
					\gridColorY, Color.new255(60,60,60),
					\gridLinePattern, FloatArray[2,4],
				);
				plotter.refresh;
			}
		}
	}
}
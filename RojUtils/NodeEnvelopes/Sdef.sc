Sdef {

	classvar <>library;
	classvar rate;
	classvar hasInitSynthDefs, bufferSynthDef;

	var <key, <path;
	var <bus, <buffer, bufferID, <synth;
	var <layers;
	var parentNode;
	var <duration;
	var hasPlotWin;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
		rate = 44100;
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
			}
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

	*frame { |time| ^rate * time }
	*time { |frame| ^frame / rate }

	// init //////////////////////////

	*initSynthDefs{
		if(Server.default.serverRunning.not) { Server.default.onBootAdd({ this.initSynthDefs }) }
		{
			bufferSynthDef = { |bus, bufnum, startTime = 0|
				var buf = PlayBuf.ar(
					numChannels: 1,
					bufnum: bufnum,
					startPos: startTime * rate,
					rate: \tempoClock.kr(1),
					trigger: \reset.tr,
					loop: 1
				);
				Out.kr(bus, buf);
			}.asSynthDef;

			// controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			"\nSdef initialization of SynthDefs done. Control rate set on %".format(rate).postln;
		};
		hasInitSynthDefs = true;
	}

	init { |name|
		this.key = name;
		hasPlotWin = false;

		bus = nil;
		buffer = Buffer.alloc( Server.default, 1 );
		bufferID = buffer.bufnum;
		synth = nil;

		layers = Order.new;
		layers.put(0, SdefLayer(this, 0));

		parentNode = nil;
		duration = nil;
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

	render {
		var startRenderTime = SystemClock.beats;

		duration = super.class.time(this.signal.size);

		buffer = Buffer.alloc(
			server: Server.default,
			numFrames: this.signal.size,
			numChannels: 1,
			bufnum: bufferID
		);

		buffer.loadCollection(
			collection: this.signal,
			startFrame: 0,
			action: {|buff|
				var clock = currentEnvironment.clock;
				var time2quant = clock.timeToNextBeat(duration);
				if(synth.notNil)
				{
					synth.set(
						\startTime, (duration - time2quant),
						\reset, 1
					);
				};

				// "buffer \n\t beats: % \n\t duration: % \n\t t2q: % \n\t offset: %".format(clock.beats, duration, time2quant, clock.beats + (duration - time2quant)).postln;

/*
				"Rendering of buffer ID(%) done \n\t- buffer duration: % sec \n\t- render time: % sec \n\t- frame count: %".format(
					bufferID,
					duration,
					(SystemClock.beats - startRenderTime),
					buff.numFrames
				).postln;
*/
				{ this.updatePlot; }.defer;
			}
		);
	}

	kr { ^BusPlug.for(bus)	}

	setNode { |nodeProxy, controlName|
		if(nodeProxy.dependants.matchItem(this).notNil) {
			nodeProxy.addDependant(this);
			nodeProxy.map(controlName.asSymbol, BusPlug.for(bus));
			parentNode = nodeProxy;
		};
		if(nodeProxy.monitor.isPlaying) { this.play };
	}

	update { |from, what, args| // object dependency -> this is target when object.changed is called
		// "\nSdef.update \n\tfrom:% \n\twhat:% \n\targs:%".format(from, what, args).postln;
		case
		{ what.asSymbol == \play } { this.play }
		{ what.asSymbol == \stop } { this.stop(args[0]) }
		{ what.asSymbol == \free } { this.stop(args[0]) };
	}

	play { |clock = nil|
		var bufferFramesCnt = buffer.numFrames;
		var dur = super.class.time(bufferFramesCnt);
		var group = RootNode(Server.default);
		var time2quant;
		if(clock.isNil) { clock = currentEnvironment.clock; };
		time2quant = clock.timeToNextBeat(duration);
		if(parentNode.notNil) { group = parentNode.group };

		if(synth.notNil) { synth.free };

		bufferSynthDef.name_("Sdef(%)".format(this.printName));
		synth =	bufferSynthDef.play(
			target: group,
			args:
			[
				\bus: bus,
				\bufnum: buffer.bufnum,
				\startTime: (dur - time2quant),
				\tempoClock: currentEnvironment.clock.tempo
			]
		);
	}

	stop { |time|
		{
			if(time.notNil) { (time * currentEnvironment.clock.tempo).wait; };
			if(synth.notNil) {
				synth.free;
				synth = nil;
			};
			bus.set(0);
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
				plotter.domainSpecs = [[0,  super.class.time(this.signal.size), 0, 0, "", " s"]];
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
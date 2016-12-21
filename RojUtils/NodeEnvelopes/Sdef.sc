Sdef {
	var <key, <path;

	var <duration;
	var <size;
	var <bus;

	var <references;
	var <parents, <children;

	var <signal;
	var <layers, <layers2;

	var <buffer;
	var <isRendered;

	// var <timeline;
	var <updatePlot;

	classvar <>library;
	classvar controlRate;

	classvar hasInitSynthDefs;
	classvar bufferSynthDef, testSynthDef;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
		controlRate = 44100 / 64;
		hasInitSynthDefs = false;
	}

	*new { |key, dur = nil ... args|
		if(hasInitSynthDefs.not) { this.initSynthDefs; };

		if(key.asArray.notEmpty)
		{
			var sDef = this.exist(key);
			if(sDef.notNil)
			{
				// "new sdef: %, key: %, dur: %, args: %".format(sDef, key, dur, args).postln;
				if(dur.notNil) { sDef.duration_(dur) };
				if(args.notEmpty)
				{
					sDef.initLayers;
					args.do({|oneArg| sDef.addLayer(oneArg) });
					// sDef.updateParents;
				}
				^sDef;
			}
			{
				sDef = super.new.init(key, dur).initBus;
				args.do({|oneArg| sDef.addLayer(oneArg) });
				^sDef;
			}
		}
		{ ^super.new.init(nil, dur)	}
	}

	*exist { |key|
		var path = key.asArray ++ \def;
		var sDef = this.library.atPath(path);
		if(sDef.notNil) { ^sDef; } { ^nil; }
	}

	*printAll { this.library.postTree; ^nil; }

	init { |initKey, initDur|
		this.key = initKey;

		this.updatePlot = false;
		// layers = List.new;
		this.initLayers;

		if(initDur.isNil)
		{ this.duration = 0 }
		{ this.duration = initDur };

		parents = Set.new;
		children = Set.new;

		bus = nil;
		buffer = nil;
		isRendered = false;
	}

	*frame { |time| ^controlRate * time; }

	initBus {
		if(Server.default.serverRunning.not)
		{
			Server.default.onBootAdd({
				bus = Bus.control(Server.default, 1);
				"\t- Sdef(%) alloc control bus at index %".format(this.key, bus.index).postln;
			});
		}
		{
			bus = Bus.control(Server.default, 1);
			"busMade".warn;
		}
	}

	*initSynthDefs{
		Server.default.onBootAdd({
			bufferSynthDef = { |bus, bufnum, freq = 440, startTime = 0|
				var buf = PlayBuf.kr(
					numChannels: 1,
					bufnum: bufnum,
					startPos: startTime * controlRate,
					rate: \tempoClock.kr(1),
					loop: 0
				);
				FreeSelfWhenDone.kr(buf);
				Out.kr(bus, buf);
			}.asSynthDef;

			testSynthDef = { |bufnum, freq = 440, startTime = 0|
				var buf = PlayBuf.kr(
					numChannels: 1,
					bufnum: bufnum,
					startPos: startTime * controlRate,
					rate: \tempoClock.kr(1),
					loop: 0
				);
				var sig = SinOsc.ar(freq!2, 0, mul: buf);
				FreeSelfWhenDone.kr(buf);
				Out.ar(0, sig);
			}.asSynthDef;

			controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			"\nSdef initialization of SynthDefs done. Control rate set on %".format(controlRate).postln;
		});
		hasInitSynthDefs = true;
	}

	// empty defs //////////////////////////

	*level { |level = 1, dur = 1, offset = 0|
		var sDef = Sdef(nil, dur + offset);
		var levelSignal = Signal.newClear(this.frame(dur)).fill(level);
		sDef.addLayer(levelSignal, offset, \new);
		^sDef;
	}

	*ramp { |from = 1, to = 0, dur = 1, offset = 0|
		// "ramp".warn;
		^this.env([from, to], dur, \lin, offset);
	}

	*env { |levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3], offset = 0|
		var envelope = Env(levels, times, curves);
		var sDef = Sdef(nil, envelope.duration + offset);
		var envSignal = envelope.asSignal(this.frame(envelope.duration));
		sDef.addLayer(envSignal, offset, \new);
		// sDef.signal.overWrite(envSignal, this.frame(offset));
		// sDef.signal.overDub(envSignal, this.frame(offset));
		// "env".warn;
		// sDef.update;
		^sDef;
	}

	// instance //////////////////////////

	key_ {|name|
		// "rename def from % to %".format(key, name).postln;
		var tempParents = parents.copy;
		var tempChildren = children.copy;

		parents.do({|parentKey| Sdef.disconnectRefs(parentKey, key); });
		children.do({|childKey| Sdef.disconnectRefs(key, childKey); });

		key = name;
		if(path.notNil) { library.removeEmptyAtPath(path) };
		path = key.asArray ++ \def;

		library.putAtPath(path, this);

		tempParents.do({|parentKey| Sdef.connectRefs(parentKey, key); });
		tempChildren.do({|childKey| Sdef.connectRefs(key, childKey); });
	}

	duration_ {|dur|
		duration = dur;
		signal = Signal.newClear(super.class.frame(duration));
		size = signal.size;

		// if(layers.notEmpty) { this.prAdd(layers.array) };
		// if(layers.notEmpty) { this.addLayer(layers.array) };

		// this.updateParents;
	}

	// references //////////////////////////

	*connectRefs {|parentKey, childKey|
		var parentDef = this.exist(parentKey);
		var childDef = this.exist(childKey);
		// "Sdef.connectRefs(parent:% | child:%)".format(parentDef, childDef).warn;
		if(parentDef.key.notNil && childDef.key.notNil)
		{
			parentDef.addChild(childDef);
			childDef.addParent(parentDef);
		}
	}

	*disconnectRefs {|parentKey, childKey|
		var parentDef = this.exist(parentKey);
		var childDef = this.exist(childKey);
		if(parentDef.key.notNil) { parentDef.removeChild(childDef) };
		if(childDef.key.notNil) { childDef.removeParent(parentDef) };
	}

	addChild { |target| children.add(target.key); }
	addParent { |target| parents.add(target.key); }

	removeChild { |target| children.remove(target.key); }
	removeParent { |target| parents.remove(target.key); }

	updateParents {
		parents.do({|parentKey|
			var sDef = Sdef.exist(parentKey);
			if(sDef.notNil) {
				"%.updateParents -> %".format(this, sDef).warn;
				sDef.update;
			};
		});
	}

	update {
		// "update".warn;
		this.mergeLayers;

	}

	// layers //////////////////////////

	initLayers {
		// "%.initLayers".format(this).warn;
		layers = List.new;
		layers2 = Table(\selector, \offset, \signal);
	}

	addLayer {|data, offset = 0, type = \add|
		"%.addLayer from class: % | val: %".format(this, data.class, data).postln;
		"FIX! resend to layer".warn;
		this.layer(layers2.lines, type, offset, data);
	}

	layer {|index, type, offset, data|
		"Sdef.layer data class: %".format(data.class).postln;
		case
		{ data.isKindOf(Signal) }
		{ layers2.putLine(index, type.asSymbol, offset, data.signal) }
		{ data.isKindOf(Env) }
		{ layers2.putLine(index, type.asSymbol, offset, data.asSignal(super.class.frame(data.duration))) }
		{ data.isKindOf(Integer) || data.isKindOf(Float)}
		{ layers2.putLine(index, type.asSymbol, offset, Signal.newClear(super.class.frame(this.duration)).fill(data)) }
		{ data.isKindOf(Sdef) }
		{
			layers2.putLine(index, type.asSymbol, offset, data.signal);
			Sdef.connectRefs(key, data.key);
		}
		{ data.isKindOf(Function) } {
			Routine.run({
				var condition = Condition.new;
				"Rendering layer from function. Duration: %".format(this.duration).warn;
				data.loadToFloatArray(this.duration, Server.default, {|array|
					layers2.putLine(index, type.asSymbol, offset, array);
					condition.test = true;
					condition.signal;
				});
				condition.wait;
				"render done".postln;
			});
			^nil;
		};
		this.mergeLayers;
	}

	mergeLayers {
		signal = Signal.newClear(super.class.frame(duration));
		layers2.lines.do({|i|
			var oneLine = layers2.getLine(i);
			var type = oneLine[0];
			var offset = oneLine[1];
			var sig = oneLine[2];
			"type: % || off: % || sig: %".format(type, offset, sig).postln;
			case
			{ type.asSymbol == \new } { signal.overWrite(sig, super.class.frame(offset));	}
			{ type.asSymbol == \add } { signal.overDub(sig, super.class.frame(offset));	}

		});
		if(updatePlot) { this.plot };
		this.updateParents;
	}


	kr { ^BusPlug.for(bus);	}

	copy {|...path|
		/*
		if(path.notEmpty)
		{
		var itemPath = path ++ \item;
		if(super.class.exist(itemPath))
		{
		var sDef = super.class.library.atPath(itemPath);

		duration = sDef.duration;
		signal = sDef.signal;
		size = sDef.size;
		this.addRef(sDef);
		}
		{ "Sdef(%) not found".format(path).warn; };
		}
		*/
	}

	add {|... args|
		args.pairsDo({|offset, data|
			"Sdef.add offset: % | data: %".format(offset, data).postln;
			this.addLayer(data, offset, \add);
		});
	}

	shift { |time|
		// var originSignal = this.signal;
		// offset = time;
		// signal = this.emptySignal(duration);
		// signal.overWrite(originSignal, this.frame(offset));

		// this.updateRefs;
		// if(autoPlot) { this.plot };
	}

	clone {|targetDur, cloneDur|
		var dupSignal = signal;
		var rest = targetDur % cloneDur;
		var loopCnt = (targetDur-rest)/cloneDur;
		var currentTime = 0;
		/*
		var temp = Timeline2.new;
		// var sDef = Sdef.new.sig(signal);
		// ("dupSize:" + dupSignal.size).postln;

		signal = Signal.newClear(controlRate * targetDur);
		loopCnt.do({|i|
		signal.overWrite(dupSignal, controlRate * currentTime);
		// var sDef = Sdef.new.sig(dupSignal);
		// temp.put(currentTime, sDef, cloneDur);
		currentTime = i * cloneDur;
		});
		temp.put(0, this, targetDur);
		// size = signal.size;

		this.prSetSignal(temp);
		// this.updateRefs;
		// this.prRender;
		// if(autoPlot) { this.plot };
		*/
	}

	render {
		var startRenderTime = SystemClock.beats;

		if(hasInitSynthDefs.not) { this.initSynthDefs; };

		if(buffer.notNil) { buffer.free; };
		isRendered = false;

		buffer = Buffer.alloc(
			server: Server.default,
			numFrames: size,
			numChannels: 1,
		);
		buffer.loadCollection(
			collection: signal,
			action: {|buff|
				var bufferID = buff.bufnum;
				var bufferFramesCnt = buff.numFrames;
				isRendered = true;

				/*
				"Rendering of buffer ID(%) done \n\t- buffer duration: % sec \n\t- render time: % sec \n\t- frame count: %".format(
				bufferID,
				this.duration,
				(SystemClock.beats - startRenderTime),
				bufferFramesCnt
				).postln;
				*/
			}
		);
	}

	trig { |startTime = 0, endTime = nil, parentGroup = nil, clock = nil, multBus = nil|
		if(clock.isNil) { clock = currentEnvironment.clock; };

		if(buffer.notNil)
		{
			var synth;
			var group = RootNode(Server.default);
			buffer.bufnum.postln;
			if(parentGroup.notNil) { group = parentGroup; };
			bufferSynthDef.name_("Sdef(%)".format(this.path2txt));
			synth =	bufferSynthDef.play(
				target: group,
				args:
				[
					\bus: bus,
					\bufnum: buffer.bufnum,
					\startTime, startTime,
					\tempoClock, currentEnvironment.clock.tempo,
					// \multiplicationBus, multBus.asMap
				]
			);
			// synth.set(\multiplicationBus, multBus);
			if(endTime.notNil)
			{
				clock.sched((endTime - startTime), { synth.free; nil; });
			}
		}
		{ "% buffer not found".format(this).warn; }
	}

	test {|freq = 120, startTime = 0|
		{
			var clock;
			if(currentEnvironment[\tempo].notNil)
			{ clock = currentEnvironment.clock }
			{ clock = TempoClock.default };

			if(this.duration - startTime > 0)
			{
				var testSynth;
				testSynthDef.name_("Sdef_test_%".format(this.path2txt));
				testSynth = testSynthDef.play(
					target: RootNode(Server.default),
					args:[
						\bufnum: buffer.bufnum,
						\freq: freq,
						\tempoClock, clock.tempo,
					]
				);
				// ^testSynth;
			}
			{ "% is shorter than arg startTime(%)".format(this, startTime).warn; }
		}.defer(0.01);
	}


	plot {|update|
		this.updatePlot = update;

		if(signal.notEmpty)
		{
			var windows = Window.allWindows;
			var plotWin = nil;
			var plotter;

			windows.do({|oneW|
				if(this.path2txt.asSymbol == oneW.name.asSymbol) { plotWin = oneW; };
			});

			if(plotWin.isNil)
			{
				plotter = signal.plot(
					name: this.path2txt.asSymbol,
					bounds: Rect(700,680,500,300)
				);
				plotter.parent.alwaysOnTop_(true);
				plotter.parent.view.background_(Color.new255(30,30,30)).alpha_(0.9);
			}
			{
				plotWin.view.children[0].close;
				plotter = Plotter(
					name: this.path2txt.asSymbol,
					parent: plotWin
				);
				plotWin.view.children[0].bounds_(Rect(8,8,plotWin.view.bounds.width-16,plotWin.view.bounds.height-16));
				plotter.value = signal;
			};

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
		{ "% signal is empty".format(this).warn; };
	}

	updatePlot_ {|bool|	if(bool.isKindOf(Boolean)) { updatePlot = bool } }

	printOn { |stream|	stream << this.class.name << "('" << this.key << "' | dur: " << this.duration << ")"; }
	// printOn { |stream|	stream << this.class.name << " (dur: " << duration << ")"; }

	path2txt {
		var txtPath = "";
		path.do({|oneFolder|
			if(txtPath.isEmpty)
			{ txtPath = "%%".format("\\", oneFolder); }
			{
				if(oneFolder != \item)
				{ txtPath = "%%%".format(txtPath,"\\", oneFolder); }
			}
		});
		^txtPath;
	}

}
Sdef {
	var <key, <path;

	var <duration;
	var <size;
	var <bus;

	var <references;
	var <parents, <children;

	var <layers, <modifications;
	var <signal;

	var <buffer;
	var <isRendered;

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
					args.do({|oneArg| sDef.layer(sDef.layers.lines, \add, 0, oneArg) });
				}
				^sDef;
			}
			{
				sDef = super.new.init(key, dur).initBus;
				args.do({|oneArg| sDef.layer(sDef.layers.lines, \add, 0, oneArg) });
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
		sDef.layer(0, \new, offset, levelSignal);
		^sDef;
	}

	*ramp { |from = 1, to = 0, dur = 1, offset = 0|
		^this.env([from, to], dur, \lin, offset);
	}

	*env { |levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3], offset = 0|
		var envelope = Env(levels, times, curves);
		var sDef = Sdef(nil, envelope.duration + offset);
		sDef.layer(0, \new, offset, envelope);
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

	update { this.mergeLayers }

	// layers //////////////////////////


	layer {|index, type, offset, data|
		"Sdef.layer data class: %".format(data.class).postln;
		case
		{ data.isKindOf(Signal) || data.isKindOf(FloatArray)}
		{
			layers.putLine(index, type.asSymbol, offset, data, false)
		}
		{ data.isKindOf(Env) }
		{ layers.putLine(index, type.asSymbol, offset, data.asSignal(super.class.frame(data.duration)), false) }
		{ data.isKindOf(Integer) || data.isKindOf(Float)}
		{ layers.putLine(index, type.asSymbol, offset, Signal.newClear(super.class.frame(this.duration)).fill(data), false) }
		{ data.isKindOf(Sdef) }
		{
			layers.putLine(index, type.asSymbol, offset, data, false);
			Sdef.connectRefs(key, data.key);
		}
		{ data.isKindOf(Function) } {
			Routine.run({
				var condition = Condition.new;
				"Rendering layer from function. Duration: %".format(this.duration).warn;
				data.loadToFloatArray(this.duration, Server.default, {|array|
					layers.putLine(index, type.asSymbol, offset, Signal.newFrom(array), false);
					condition.test = true;
					condition.signal;
				});
				condition.wait;
				"render done".warn;
				this.mergeLayers;
			},clock: AppClock);
			^nil;
		};
		this.mergeLayers;
	}
	initLayers {
		// "%.initLayers".format(this).warn;
		layers = Table(\selector, \offset, \sdef, \mute);
		modifications = Table(\mute, \shift);
	}

	mergeLayers {
		signal = signal.fill(0); // Signal.newClear(super.class.frame(duration));

		modifications.lines.do({|i|
			var oneLine = modifications.getLine(i);
			var mute = oneLine[0];
			var shift = oneLine[1];
			// var value = oneLine[2];

			if(mute.notNil) { layers.put(i, \mute, mute) };
			if(shift.notNil) { layers.put(i, \offset, shift) };

			// case
			// { type.asSymbol == \mute } { layers.put(target, \mute, value) }
			// { type.asSymbol == \shift } { layers.put(target, \offset, value) }
		});

		layers.lines.do({|i|
			var oneLine = layers.getLine(i);
			var type = oneLine[0];
			var offset = oneLine[1];
			var sig = oneLine[2];
			var mute = oneLine[3];
			// "type: % || off: % || sig: %".format(type, offset, sig).postln;
			if(sig.isKindOf(Sdef)) { sig = sig.signal };
			if(mute.not)
			{
				case
				{ type.asSymbol == \new } { signal.overWrite(sig, super.class.frame(offset)) }
				{ type.asSymbol == \add } { signal.overDub(sig, super.class.frame(offset));	};
			};
		});

		if(updatePlot) { this.plot };
		this.updateParents;
	}

	// edit //////////////////////////

	mute { |...indexs|
		indexs.do({|layer| modifications.put(layer, \mute, true) });
		this.mergeLayers;
	}
	unmute { |...indexs|
		indexs.do({|layer| modifications.put(layer, \mute, false) });
		this.mergeLayers;
	}
	unmuteAll {
		modifications.lines.do({|i|	modifications.put(i, \mute, false) });
		this.mergeLayers;
	}

	shift { |layer, offset|
		modifications.put(layer, \shift, offset);
		this.mergeLayers;
	}

	dup { |layer, times|

	}


	kr { ^BusPlug.for(bus);	}

	add {|... args|
		args.pairsDo({|offset, data|
			"Sdef.add offset: % | data: %".format(offset, data).postln;
			this.addLayer(\add, offset, data);
		});
	}


	clone {|targetDur, cloneDur|
		/*
		var dupSignal = signal;
		var rest = targetDur % cloneDur;
		var loopCnt = (targetDur-rest)/cloneDur;
		var currentTime = 0;
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
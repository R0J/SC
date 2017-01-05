Sdef {
	var <key, <path;

	var <duration;
	var <size;
	var <bus;

	var <references;
	var <parents, <children;

	var <layers, <modifications;
	var <signal;

	var <buffer, <synth;
	var <isRendered;

	var <updatePlot;

	classvar <>library;
	classvar controlRate;

	classvar hasInitSynthDefs;
	classvar bufferSynthDef, testSynthDef;
	classvar playBuf;

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
				sDef = super.new.init(key);
				sDef.initBus;
				sDef.duration_(dur);
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

	*frame { |time| ^controlRate * time; }

	init { |initKey, initDur|
		this.key = initKey;

		this.updatePlot = false;
		this.initLayers;
		/*
		if(initDur.isNil)
		{ this.duration = 0 }
		{ this.duration = initDur };
		*/
		parents = Set.new;
		children = Set.new;

		bus = nil;
		buffer = nil;
		synth = nil;
		isRendered = false;
	}

	*initSynthDefs{
		if(Server.default.serverRunning.not) { Server.default.onBootAdd({ this.initSynthDefs }) }
		{
			bufferSynthDef = { |bus, bufnum, startTime = 0|
				var buf = PlayBuf.kr(
					numChannels: 1,
					bufnum: bufnum,
					startPos: startTime * controlRate,
					rate: \tempoClock.kr(1),
					loop: 1
				);
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
		};
		hasInitSynthDefs = true;
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
		if(duration != dur)
		{
			if(buffer.notNil) { buffer.free; };
			duration = dur;
			signal = Signal.newClear(super.class.frame(duration));
			size = signal.size;
			buffer = Buffer.alloc(
				server: Server.default,
				numFrames: size,
				numChannels: 1,
			);
			"new buffer init (%)".format(buffer).warn;
			if(layers.lines.size > 0) { this.mergeLayers };
			this.play;
		}
	}

	play { |clock = nil|
		var time2quant;
		if(clock.isNil) { clock = currentEnvironment.clock; };
		time2quant = clock.timeToNextBeat(this.duration);

		if(synth.notNil) { synth.free };
		// "play buffer: % || bus: % || t2q: %".format(buffer, bus, time2quant).warn;

		bufferSynthDef.name_("Sdef(%)".format(this.path2txt));
		synth =	bufferSynthDef.play(
			target: RootNode(Server.default),
			args:
			[
				\bus: bus,
				\bufnum: buffer.bufnum,
				\startTime, this.duration - time2quant,
				\tempoClock, currentEnvironment.clock.tempo,
				// \multiplicationBus, multBus.asMap
			]
		);
		// "play buffer init (%)".format(synth).warn;
	}

	stop { synth.free; synth = nil; bus.set(0); }

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

	initLayers {
		// "%.initLayers".format(this).warn;
		layers = Table(\selector, \offset, \sdef, \mute);
		modifications = Table(\mute, \start, \shift);
	}

	at { |index| ^layers.get(\sdef, index) }

	layer { |index, type, offset, data|
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

	mergeLayers {
		signal = signal.fill(0); // Signal.newClear(super.class.frame(duration));

		modifications.lines.do({|i|
			var oneLine = modifications.getLine(i);
			var mute = oneLine[0];
			var start = oneLine[1];
			var shift = oneLine[2];

			if(mute.notNil) { layers.put(i, \mute, mute) };
			if(start.notNil) { layers.put(i, \offset, start) };
			if(shift.notNil) {
				var layerStart = layers.get(\offset, i);
				layers.put(i, \offset, shift + layerStart)
			};
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
				offset.asArray.do({|oneTime|
					case
					{ type.asSymbol == \new } { signal.overWrite(sig, super.class.frame(oneTime)) }
					{ type.asSymbol == \add } { signal.overDub(sig, super.class.frame(oneTime)) };
				});
			};
		});

		if(updatePlot) { this.plot };
		this.updateParents;
		this.render;
	}

	// edit //////////////////////////

	mute { |...indexs|
		indexs.do({|oneLayer| modifications.put(oneLayer, \mute, true) });
		this.mergeLayers;
	}
	unmute { |...indexs|
		indexs.do({|oneLayer| modifications.put(oneLayer, \mute, false) });
		this.mergeLayers;
	}
	unmuteAll {
		modifications.lines.do({|i|	modifications.put(i, \mute, false) });
		this.mergeLayers;
	}

	shift { |offset ...indexs|
		indexs.do({|oneLayer| modifications.put(oneLayer, \shift, offset) });
		this.mergeLayers;
	}

	dup { |index, targetDur, cloneDur|
		var rest = targetDur % cloneDur;
		var loopCnt = (targetDur-rest)/cloneDur;
		var offsets = Array.newClear(loopCnt);
		// "rest: %; loopCnt: % ".format(rest, loopCnt).postln;

		loopCnt.do({|loopNum|
			offsets.put(loopNum, cloneDur * loopNum);
		});
		modifications.put(index, \start, offsets);
		this.mergeLayers;
	}

	kr { ^BusPlug.for(bus);	}

	add {|... args|
		args.pairsDo({|offset, data|
			"Sdef.add offset: % | data: %".format(offset, data).postln;
			this.addLayer(\add, offset, data);
		});
	}

	render {
		if(buffer.notNil)
		{
			var renderedBuffer;
			var startRenderTime = SystemClock.beats;

			isRendered = false;

			"render buffer: %".format(buffer).warn;
			buffer.loadCollection(
				collection: signal,
				action: {|buff|
					var bufferID = buff.bufnum;
					var bufferFramesCnt = buff.numFrames;
					var time2quant = currentEnvironment.clock.timeToNextBeat(this.duration);
					isRendered = true;

					"Rendering of buffer ID(%) done \n\t- buffer duration: % sec \n\t- render time: % sec \n\t- frame count: %".format(
						bufferID,
						this.duration,
						(SystemClock.beats - startRenderTime),
						bufferFramesCnt
					).postln;
				}
			);
		};
	}

	trig { |startTime = 0, endTime = nil, parentGroup = nil, clock = nil, multBus = nil|
		if(clock.isNil) { clock = currentEnvironment.clock; };

		if(buffer.notNil)
		{
			// var synth;
			var group = RootNode(Server.default);
			// buffer.bufnum.postln;
			// bufferSynthDef.postln;
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
			var winName = "Sdef(%)".format(this.path2txt);
			var windows = Window.allWindows;
			var plotWin = nil;
			var plotter;

			windows.do({|oneW|
				if(winName.asSymbol == oneW.name.asSymbol) { plotWin = oneW; };
			});

			if(plotWin.isNil)
			{
				plotter = signal.plot(
					name: winName.asSymbol,
					bounds: Rect(700,680,500,300)
				);
				plotter.parent.alwaysOnTop_(true);
				plotter.parent.view.background_(Color.new255(30,30,30)).alpha_(0.9);
			}
			{
				plotWin.view.children[0].close;
				plotter = Plotter(
					name: winName.asSymbol,
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
				if(oneFolder != \def)
				{ txtPath = "%%%".format(txtPath,"\\", oneFolder); }
			}
		});
		^txtPath;
	}

}
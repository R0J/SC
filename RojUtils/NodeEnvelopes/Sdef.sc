Sdef {
	var <path;

	var <duration;
	var <size;
	var <bus;

	var <references;
	var <signal;
	var <buffer;
	var <isRendered;

	var <timeline;
	var autoPlot;

	classvar <>library;
	classvar controlRate;

	classvar hasInitSynthDefs;
	classvar bufferSynthDef, testSynthDef;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
		controlRate = 44100 / 64;
		// controlRate = 44100;
		hasInitSynthDefs = false;
	}

	*new { |...path|
		if(hasInitSynthDefs.not) { this.initSynthDefs; };
		if(path.notEmpty)
		{
			path = path ++ \item;
			if(this.exist(path))
			{ ^this.library.atPath(path); }
			{ ^super.new.init(path); };
		}
		{ ^super.new.init(nil) }
	}


	*exist { |path|	if(this.library.atPath(path).notNil) { ^true; } { ^false; } }

	*printAll { this.library.postTree; }

	init { |pathKey|
		path = pathKey;
		timeline = Timeline2.new;
		size = nil;
		references = Set.new;
		buffer = nil;
		isRendered = false;
		autoPlot = false;

		bus = nil;
		Server.default.waitForBoot({
			bus = Bus.control(Server.default, 1);
		});

		if(pathKey.notNil) { library.putAtPath(pathKey, this); };
	}

	*initSynthDefs{
		Server.default.waitForBoot({
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

			hasInitSynthDefs = true;
			controlRate = Server.default.sampleRate / Server.default.options.blockSize;
		});
	}

	kr { ^BusPlug.for(bus); }

	addRef { |target| references.add(target); }
	updateRefs {
		references.do({|oneRef|
			case
			{ oneRef.isKindOf(Sdef) } {	oneRef.prSetSignal(oneRef.timeline) }
			{ oneRef.isKindOf(Stage) } { oneRef.update }
		})
	}

	frame { |time| ^controlRate * time; }

	emptySignal { |dur| ^Signal.newClear(this.frame(dur)); }

	level { |dur, level = 1, time = 1, shift = 0|
		var levelSignal = this.emptySignal(time).fill(level);
		signal = this.emptySignal(dur);
		duration = dur;
		size = signal.size;

		signal.overWrite(levelSignal, this.frame(shift));

		this.updateRefs;
		this.prRender;
		if(autoPlot) { this.plot };
	}

	env { |levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3], shift = 0|
		var envelope = Env(levels, times, curves);
		// timeline = Timeline2.new;
		signal = envelope.asSignal(controlRate * envelope.duration);
		timeline.put(0, this, envelope.duration);
		size = signal.size;
		this.updateRefs;
		// this.prRender;
		if(autoPlot) { this.plot };
	}

	setn { |...pairsTimeItem|
		var temp = Timeline2.new;
		if(pairsTimeItem.size % 2 != 0)
		{
			"Arguments of time and items are't set in pairs. MethodArgExample (0, Env(), 1.2, Sdef(\\x), ...)".warn;
			^this;
		};

		pairsTimeItem.pairsDo({|time, item|
			var sDef = item;
			case
			{ item.isKindOf(Env) }
			{ sDef = Sdef.new.env(item.levels, item.times, item.curves); }
			;
			temp.put(time, sDef, sDef.duration);
		});
		this.prSetSignal(temp);
	}

	prSetSignal {|inOrder|
		if(inOrder.isKindOf(Timeline2)) {
			var totalDuration = 0;
			timeline = Timeline2.new;

			inOrder.items({|time, duration, item|
				case
				{ item.isKindOf(Sdef) }
				{
					if(item.duration.notNil)
					{
						timeline.put(time, item, item.duration);
						item.addRef(this);
					}
					{ "% not found".format(item).warn; }
				};
			});

			signal = Signal.newClear(controlRate * timeline.duration);
			// duration = timeline.duration;
			size = signal.size;

			timeline.items({|time, duration, sdef|
				// "time: % | sig: %".format(time, sdef.duration).postln;
				// signal.overWrite(oneSignal, time * controlRate);
				signal.overDub(sdef.signal, time * controlRate);
			});

			this.updateRefs;
			// this.prRender;
			if(autoPlot) { this.plot };
		};
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

	prRender {
		// Server.default.waitForBoot({
		var startRenderTime = SystemClock.beats;
		if(buffer.notNil) { buffer.free; };
		isRendered = false;
		"size: %".format(size).warn;
		buffer = Buffer.alloc(
			server: Server.default,
			numFrames: size,
			numChannels: 1,
		);
		"ok".warn;
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
		// });
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


	plot {
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

	updatePlot {|bool| if(bool.isKindOf(Boolean)) { autoPlot = bool; }; }

	printOn { |stream|	stream << this.class.name << "( " << this.path2txt << " | dur: " << this.duration << ")"; }
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
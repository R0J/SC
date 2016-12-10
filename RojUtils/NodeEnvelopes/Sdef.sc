Sdef {
	var <path;
	var <duration;
	var <size;
	var <>signal;
	var <setOrder;
	var <references;
	var <buffer;

	var autoPlot;

	classvar <>library;
	classvar controlRate;

	classvar hasInitSynthDefs;
	classvar testSynthDef;

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
		duration = nil;
		size = nil;
		references = Set.new;
		buffer = nil;
		autoPlot = false;
		if(pathKey.notNil) { library.putAtPath(pathKey, this); }
	}

	*initSynthDefs{
		Server.default.waitForBoot({
			testSynthDef = { |numBuf, freq = 440, startTime = 0|
				var buf = PlayBuf.kr(
					numChannels: 1,
					bufnum: numBuf,
					startPos: startTime * controlRate,
					rate: \tempoClock.kr(1),
					loop: 0
				);
				var sig = SinOsc.ar(freq!2, 0, mul: buf);
				FreeSelfWhenDone.kr(buf);
				Out.ar(0, sig);
			}.asSynthDef;

			hasInitSynthDefs = true;
		});
	}

	addRef { |target| references.add(target); }
	updateRefs {
		references.do({|oneRef|
			oneRef.prSetSignal(oneRef.setOrder);
		})
	}

	level { |level = 1, dur = 1, shift = 0|
		signal = Signal.newClear(controlRate * dur).fill(level);
		duration = dur;
		size = signal.size;
		this.updateRefs;
		this.prRender;
	}

	env { |levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3], shift = 0|
		var envelope = Env(levels, times, curves);
		signal = envelope.asSignal(controlRate * envelope.duration);
		duration = envelope.duration;
		size = signal.size;
		this.updateRefs;
		this.prRender;
	}

	setn { |...pairsTimeItem|
		var inOrder = Order.new;

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
			inOrder.put(time, sDef);
		});
		this.prSetSignal(inOrder);
	}

	prSetSignal {|inOrder|
		if(inOrder.isKindOf(Order)) {
			var totalDuration = 0;
			setOrder = Order.new;

			inOrder.keysValuesDo({|time, item|
				var endTime;
				case
				{ item.isKindOf(Sdef) }
				{
					if(item.duration.notNil)
					{
						endTime = time + item.duration;
						if(totalDuration < endTime) { totalDuration = endTime };
						setOrder.put(time, item);
						item.addRef(this);
					}
				};
			});

			signal = Signal.newClear(controlRate * totalDuration);
			duration = totalDuration;
			size = signal.size;

			setOrder.indicesDo({|sdef, time|
				// "time: % | sig: %".format(time, sdef.duration).postln;
				// signal.overWrite(oneSignal, time * controlRate);
				signal.overDub(sdef.signal, time * controlRate);
			});

			this.updateRefs;
			this.prRender;
			if(autoPlot) { this.plot };
		};
	}

	prRender {
		var renderTimeStart = SystemClock.beats;
		if(buffer.notNil) { buffer.free; };

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
				var renderTimeEnd = SystemClock.beats;
				"Rendering of buffer ID(%) done \n\t- buffer duration: % sec \n\t- render time: % sec \n\t- frame count: %".format(
					bufferID,
					duration,
					(renderTimeEnd - renderTimeStart),
					bufferFramesCnt
				).postln;
			}
		);
	}

	test {|freq = 120, startTime = 0|
		{
			if(duration - startTime > 0)
			{
				var testSynth;
				// currentEnvironment.clock.sched(0, {
				testSynthDef.name_("Sdef_test_%".format(this.path2txt));
				testSynth = testSynthDef.play(
					target: RootNode(Server.default),
					args:[
						\numBuf: buffer.bufnum,
						\freq: freq
					]
				);
				// this.trig(startTime);
				// nil;
				// });
				// currentEnvironment.clock.sched((duration - startTime), {/
				// currentEnvironment.clock.sched((duration - startTime), {
				// "End of test %".format(testSynth).postln;
				// testSynth.free;
				// testSynth.release(2);
				// nil;
				// });
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

			plotter.domainSpecs = [[0, duration, 0, 0, "", " s"]];
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

	updatePlot {|bool|
		bool.isKindOf(Boolean).postln;
		if(bool.isKindOf(Boolean)) { autoPlot = bool; };
	}

	printOn { |stream|	stream << this.class.name << "( " << this.path2txt << " | dur: " << duration << ")"; }
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
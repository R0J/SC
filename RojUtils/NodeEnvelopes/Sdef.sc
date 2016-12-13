Sdef {
	var <key, path;

	var <duration;
	var <size;
	var <bus;

	var <references;
	var <>parents, <children;

	var <signal;
	var <buffer;
	var <isRendered;

	// var <timeline;
	var autoPlot;

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
			if(this.exist(key))
			{
				var path = key.asArray  ++ \def;
				if(dur.notNil)
				{ ^super.new.init(key, dur) }
				{ ^this.library.atPath(path) }
			}
			{ ^super.new.init(key, dur).initBus }
		}
		{ ^super.new.init(nil, dur)	}
	}

	*exist { |key|
		var path = key.asArray ++ \def;
		if(this.library.atPath(path).notNil) { ^true; } { ^false; }
	}

	*printAll { this.library.postTree; ^nil; }

	init { |initKey, initDur|

		key = initKey;
		path = key.asArray ++ \def;

		if(initDur.isNil)
		{ this.duration = 0 }
		{ this.duration = initDur };

		parents = Set.new;
		children = Set.new;

		bus = nil;
		buffer = nil;
		isRendered = false;

		autoPlot = false;

		if(key.notNil) { library.putAtPath(path, this); }
	}

	frame { |time| ^controlRate * time; }

	duration_ {|dur|
		duration = dur;
		signal = Signal.newClear(this.frame(duration));
		size = signal.size;
	}

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

	kr {
		// this.prRender;
		^BusPlug.for(bus);
	}

	addRef { |target|
		children.add(target);
		target.parents.add(this);
	}

	updateRefs { parents.do({|oneRef| oneRef.update}) }

	update {
		signal = this.emptySignal(duration);
		children.do({|oneRef|
			signal.overDub(oneRef.signal, 0);
			this.updateRefs;
			if(autoPlot) { this.plot };
		})
	}

	copy {|...path|
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
	}




	newSignal {
		signal = Signal.newClear(this.frame(duration));
		size = signal.size;
		this.updateRefs;
		if(autoPlot) { this.plot };
		"newSignal".warn;
	}

	level { |from = 0, dur = 1, level = 1|
		var levelSignal = Signal.newClear(this.frame(dur)).fill(level);
		// signal.overWrite(levelSignal, this.atFrame(from));
		signal.overDub(levelSignal, this.frame(from));

		this.updateRefs;
		if(autoPlot) { this.plot };
		"level".warn;
	}

	env { |from = 0, levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3]|
		var envelope = Env(levels, times, curves);
		var envSignal = envelope.asSignal(this.frame(envelope.duration));
		// signal.overWrite(envSignal, this.atFrame(from));
		signal.overDub(envSignal, this.frame(from));

		this.updateRefs;
		if(autoPlot) { this.plot };
		"env".warn;
	}

	ramp { |from = 0, dur = 1, startLevel = 1, endLevel = 0|
		this.env(from, [startLevel, endLevel], dur, \lin);
		"ramp".warn;
	}

	shift { |time|
		// var originSignal = this.signal;
		// offset = time;
		// signal = this.emptySignal(duration);
		// signal.overWrite(originSignal, this.frame(offset));

		// this.updateRefs;
		// if(autoPlot) { this.plot };
	}

	add {|dur ...sDefs|
		duration = dur;
		signal = this.emptySignal(duration);
		size = signal.size;
		sDefs.do({ |sDef|
			this.addRef(sDef);
			signal.overDub(sDef.signal, 0);
		});

		this.updateRefs;
		if(autoPlot) { this.plot };
	}
	/*
	setn { |...pairsTimeItem|
	// timeline = Timeline2.new;
	if(pairsTimeItem.size % 2 != 0)
	{
	"Arguments of time and items are't set in pairs. MethodArgExample (0, Env(), 1.2, Sdef(\\x), ...)".warn;
	^this;
	};

	pairsTimeItem.pairsDo({|time, item|
	var sDef = item;

	case
	{ item.isKindOf(Sdef) }
	{
	if(item.duration.notNil)
	{
	this.addRef(item);
	// item.addRef(this)
	}
	{ "% not found".format(item).warn; };
	}
	{ item.isKindOf(Env) }
	{ sDef = Sdef.new.env(item.levels, item.times, item.curves); }
	;

	// timeline.put(time, sDef, sDef.duration);
	});

	// this.prSetSignal(timeline);
	}

	prSetSignal {|inOrder|
	if(inOrder.isKindOf(Timeline2)) {
	/*
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
	*/

	// duration = timeline.duration;
	// "dur: %, ref:%".format(duration, references).warn;
	// duration = timeline.duration;
	signal = this.emptySignal(duration);
	size = signal.size;

	timeline.items({|time, duration, sdef|
	// timeline.items({|time, duration, sdef|
	// "time: % | sig: %".format(time, sdef.duration).postln;
	// signal.overWrite(oneSignal, time * controlRate);
	signal.overDub(sdef.signal, time * controlRate);
	});

	this.updateRefs;
	if(autoPlot) { this.plot };
	};
	}
	*/


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
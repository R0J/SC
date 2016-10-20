NodeEnv {

	var nodeName, controlName, envName;
	var envelope;

	var synth, controlBus;
	var <quant, loopTime;

	var >setPlot = false;

	*new {|nodeName, controlName, envName|
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var path = [\envelopes, controlName.asSymbol];
		var nEnv = library.atPath(path ++ envName.asSymbol);

		if(nEnv.isNil,
			{
				("NodeEnv" + envName + "novy").postln;
				^super.newCopyArgs(nodeName.asSymbol, controlName.asSymbol, envName).init;
			}, {
				("NodeEnv" + envName + "nalezen").postln;
				^nEnv;
			}
		);
	}

	init {
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);

		synth = nil;
		controlBus = library.at(\envelopes, controlName.asSymbol, \controlBus);
		node.set(controlName.asSymbol, controlBus.asMap);

		this.prepareSynthDef;

		this.storeToMap;
	}

	env {|env|
		envelope = env;
		if(setPlot) { this.plot; };
	}

	envCode { ^envelope.storeArgs; }

	printOn { |stream|
		stream << this.class.name << "[~" << nodeName << "; \\" << controlName << "]";
		// if(name.notNil) { stream << this.class.name << "[" << name << "]"; }
	}

	plot {|size| envelope.plotNamedEnv(envName.asSymbol); }

	prepareSynthDef {
		var envSynthDef;
		var envSynthName = nodeName ++ "_" ++ controlName ++ "_" ++ envName;
		var fTime = 0;

		envSynthDef = {|cBus|
			var envelope = EnvGen.kr(
				\env.kr(Env.newClear(200,1).asArray),
				gate: \envTrig.tr(0),
				timeScale: \tempoClock.kr(1).reciprocal,
				doneAction: 0
			);

			var fade = EnvGen.kr(
				Env([ \fromVal.kr(0), \toVal.kr(0)], \fadeTime.kr(fTime), \sin),
				gate:\fadeTrig.tr(0),
				timeScale: \tempoClock.kr(1).reciprocal
			);

			Out.kr( cBus, envelope * fade );
		};
		envSynthDef.asSynthDef(name:envSynthName.asSymbol).add;
		("SynthDef" + envSynthName + "added").postln;

		Server.default.sync;
		// this.prFadeOutSynths(control, envName, fTime);

		synth = Synth(envSynthName.asSymbol, [
			\cBus: controlBus,
			\env: [envelope],
			\fromVal, 0,
			\toVal, 1,
			\fadeTime, fTime,
			\fadeTrig, 1
		], nodeName.envirGet.group);
		// synthID = synth.nodeID;


		// library.putAtPath(path ++ \synths ++ synthID.asSymbol, synth);
		// });
	}

	trig {
		// synth.do({|selectedSynth| selectedSynth.set(\envTrig, 1, \tempoClock, currentEnvironment.clock.tempo); });
		("Synth trig to " + controlBus + "by synth:" + synth.nodeID + "env:" + this.envCode).postln;
		synth.set(
			\envTrig, 1,
			\tempoClock, currentEnvironment.clock.tempo,
			\env, [envelope]
		);
	}

	storeToMap {
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var stage = \default;
		var path = [\envelopes, controlName.asSymbol];
		// if(envName.isNil) {	path = [controlName.asSymbol, \stages, stage.asSymbol, \envelopes, \default]; };
		// if(envName.isNil) {	path = [controlName.asSymbol, \envelopes]; };

		library.putAtPath(path ++ envName.asSymbol, this);

		library.postTree;
	}
}

NodeCycle {
	var nodeName, cycleName;
	var clock;
	var timeline;

	*new {|nodeName, cycleName|
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var path = [\cycles];
		var nCycle = library.atPath(path ++ cycleName.asSymbol);

		if(nCycle.isNil,
			{
				("NodeCycle" + cycleName + "novy").postln;
				^super.newCopyArgs(nodeName.asSymbol, cycleName.asSymbol).init;
			}, {
				("NodeCycle" + cycleName + "nalezen").postln;
				^nCycle;
			}
		);
	}

	init {
		// var node = nodeName.envirGet;
		// var library = node.nodeMap.get(\qMachine);
		// clock = TempoClock.new(currentEnvironment.clock.tempo);
		timeline = Order.new();
		this.storeToMap;
	}

	schedEnv {|time, nodeEnv|
		timeline.put(time, nodeEnv);
	}

	trig {
		clock = TempoClock.new(currentEnvironment.clock.tempo);

		timeline.keysValuesDo ({|time|
			var selectedNodeEnv = timeline[time];
			// var selectedEnvDuration = library.atPath(envPath ++ selectedEnvKey ++ \dur);
			("timeLine time:" + time).postln;
			("timeLine selectedNodeEnv:" + selectedNodeEnv).postln;
			/*
			clock.sched(0, {
			// (time + "env:" + selectedEnvKey).postln;
			this.prTrigSynths(control, selectedEnvKey);
			});
			*/
		});
	}

	storeToMap {
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var stage = \default;
		var path = [\cycles];

		library.putAtPath(path ++ cycleName.asSymbol, this);
		library.postTree;
	}
}

NodeStage { }

+ NodeProxy {

	// ~node.env(\amp, Env() ) --- env \default, ma se stat jen jednou, start node.quant
	// ~node.env(\amp, Env(), loopTime ) --- env \default, ma se stat opakovane, start node.quant
	env { |controlName, envelope, loopTime = nil|
		var library = this.prGetLibrary(controlName);
		var path = [controlName.asSymbol, \envelopes];
		// var nEnv = library.atPath(path ++ \default);
		var nEnv, nCycle;
		{
			case
			{ envelope.isKindOf(Env) } {
				"envelope je to env".postln;

				nEnv = NodeEnv(this.envirKey, controlName, \default);
				nEnv.env(envelope);

				nCycle = NodeCycle(this.envirKey, \default);
				nCycle.schedEnv(0, nEnv);
			}
			{ envelope.isKindOf(Array) } {
				"envelope je to array".postln;

			}
			;

			nCycle.trig;
		}.fork;
		// var nEnv = NodeEnv(this.envirKey, control, envelope);
		// var quant = this.quant;
		// nEnv.storeToMap;


	}

	cycle { }

	stage { }

	prGetLibrary { |control|
		var library = this.nodeMap.get(\qMachine);

		if((library == nil), {
			this.nodeMap.put(\qMachine, MultiLevelIdentityDictionary.new);
			library = this.nodeMap.get(\qMachine);
			("NodeMap library qMachine prepared").postln;
		});

		if((library.at(control.asSymbol) == nil),
			{
				var controlBus = Bus.control(Server.default, 1);
				library.put(\envelopes, controlName.asSymbol, \controlBus);
			}
		);

		^library;
	}


	///////////////////////////////////////

	qenv { |control, envName, env, duration = nil, fTime = 1|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var path = [control.asSymbol, \stages, stage.asSymbol, \envelopes, envName.asSymbol];
		var envSynthDef;
		var envSynthName = this.key ++ "_" ++ control ++ "_" ++ envName;
		var controlBus = library.at(control.asSymbol, \controlBus);

		var synth, synthID;
		{
			if((library.atPath(path ++ \synth) == nil), {
				envSynthDef = {|cBus|

					var envelope = EnvGen.kr(
						\env.kr(Env.newClear(20,1).asArray),
						gate: \envTrig.tr(0),
						timeScale: \tempoClock.kr(1).reciprocal,
						doneAction: 0
					);

					var fade = EnvGen.kr(
						Env([ \fromVal.kr(0), \toVal.kr(0)], \fadeTime.kr(fTime), \sin),
						gate:\fadeTrig.tr(0),
						timeScale: \tempoClock.kr(1).reciprocal
					);

					Out.kr( cBus, envelope * fade );
				};
				envSynthDef.asSynthDef(name:envSynthName.asSymbol).add;
				("SynthDef" + envSynthName + "added").postln;
			});

			Server.default.sync;

			if((duration == nil), {duration = env.duration});

			this.prFadeOutSynths(control, envName, fTime);

			synth = Synth(envSynthName.asSymbol, [
				\cBus: controlBus,
				\env: [env],
				\fromVal, 0,
				\toVal, 1,
				\fadeTime, fTime,
				\fadeTrig, 1
			], this.group);
			synthID = synth.nodeID;

			library.putAtPath(path ++ \synths ++ synthID.asSymbol, synth);
			library.putAtPath(path ++ \env, env);
			library.putAtPath(path ++ \dur, duration);

			this.prCyclesDuration(control);

			library.postTree;
		}.fork;
	}

	qcycle {|control, cycleName, pattern|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var cyclesPath = [control.asSymbol, \stages, stage.asSymbol, \cycles, cycleName.asSymbol];
		var envelopesPath = [control.asSymbol, \stages, stage.asSymbol, \envelopes];
		var synthName = this.key ++ "_" ++ control;

		var stream = pattern.asStream;
		var cycleDuration = 0;

		case
		{ stream.isKindOf(Routine) } { stream = stream.all; } // Pseq([\aaa, \bbb], 3) ++ \ccc
		{ stream.isKindOf(Symbol) }	{ stream = stream.asArray; }
		{ stream.isKindOf(Integer) } { stream = stream.asSymbol.asArray; }
		{ stream.isKindOf(String) }	{ stream = stream.asSymbol.asArray; }
		;

		library.putAtPath(cyclesPath ++ \envPattern, stream);
		this.prCyclesDuration(control);

		library.postTree;
	}

	qstage {|control, pattern, repeats = inf|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var stagePath = [control.asSymbol, \stages, stage.asSymbol];

		var stream = pattern.asStream;
		case
		{ stream.isKindOf(Routine) } { stream = stream.all; } // Pseq([\aaa, \bbb], 3) ++ \ccc
		{ stream.isKindOf(Symbol) }	{ stream = stream.asArray; }
		{ stream.isKindOf(Integer) } { stream = stream.asSymbol.asArray; }
		{ stream.isKindOf(String) }	{ stream = stream.asSymbol.asArray; }
		;

		library.putAtPath(stagePath ++ \cyclePattern, stream);
		library.putAtPath(stagePath ++ \stageLoopEnd, repeats);

		this.prUpdateTimeline(control);
		// library.postTree;
	}

	qplay {|control, startTime = nil, endTime = nil, loop = false|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var stagePath = [control.asSymbol, \stages, stage.asSymbol];
		var envPath = [control.asSymbol, \stages, stage.asSymbol, \envelopes];
		var controlBus = library.atPath([control.asSymbol, \controlBus]);
		var stageTimeline = library.atPath(stagePath ++ \stageTimeline);
		var stageClock = library.atPath(stagePath ++ \stageClock);

		if(controlBus.isNil, {
			"controlBus je NIL".warn;
			^nil;
		},{
			this.set( control.asSymbol, controlBus.asMap );
		});

		if(stageTimeline.isNil, {
			"stageTimeline je NIL".warn;
			^nil;
		},{
			var clock = TempoClock.new(currentEnvironment.clock.tempo);
			var firstEnvelope,  lastEnvelope;
			var lastEnvelopeTimeReserve;
			var loopTimeGap = 1;

			if(startTime.isNil, { startTime = 0; });
			if(endTime.isNil, {	endTime = stageTimeline.lastIndex; });

			if(stageClock.notNil, {	stageClock.stop; });
			this.prUpdateTimeline(control);

			stageTimeline.keysValuesDo ({|time|
				var selectedEnvKey = stageTimeline[time].asSymbol;
				var selectedEnvDuration = library.atPath(envPath ++ selectedEnvKey ++ \dur);

				if((time >= startTime) && (time <= endTime),
					{
						var delta = time - startTime;
						clock.sched(delta, {
							(time + "env:" + selectedEnvKey).postln;
							this.prTrigSynths(control, selectedEnvKey);
						});
					}
				);

				// time reserver for playing last found envelope
				if((time < endTime) && ((time + selectedEnvDuration) >= endTime), {
					var time2end = endTime - time;
					lastEnvelopeTimeReserve = selectedEnvDuration - time2end;
				});
			});

			clock.sched(endTime - startTime + lastEnvelopeTimeReserve + loopTimeGap, {
				stageClock.stop;
				if(loop,
					{
						(endTime + "stageClock restarted\n").postln;
						this.qplay(control, startTime, endTime, loop);
					},{
						(endTime + "stageClock stopped\n").postln;
					}
				);
			});

			library.putAtPath(stagePath ++ \stageClock, clock);
		});
	}

	qstop { |control|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var stagePath = [control.asSymbol, \stages, stage.asSymbol];
		var stageClock = library.atPath(stagePath ++ \stageClock);

		if(stageClock.notNil, {	stageClock.stop; });
		this.unmap(control.asSymbol);
	}

	qplot {|control, cycleName, segments = 400|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var winName = control.asString + "[ stage:" + stage.asString + "|| cycleName:" + cycleName.asString + "]";

		var arrEnv = this.prEvelopesArray(control, cycleName);
		var cycleEnv = this.prConnectEnvelopes(arrEnv);

		cycleEnv.plot(segments, name:winName);
	}

	prTrigSynths {|control, envName|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var envPath = [control.asSymbol, \stages, stage.asSymbol, \envelopes, envName.asSymbol];
		var synthsDict = library.atPath(envPath ++ \synths);

		synthsDict.do({|selectedSynth| selectedSynth.set(\envTrig, 1, \tempoClock, currentEnvironment.clock.tempo); });
	}

	prFadeOutSynths {|control, envName, fTime = 0|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var envPath = [control.asSymbol, \stages, stage.asSymbol, \envelopes, envName.asSymbol];
		var synthsDict = library.atPath(envPath ++ \synths);

		synthsDict.do({|selectedSynth|
			var nodeID = selectedSynth.nodeID;
			Task({
				selectedSynth.set(
					\fromVal, 1,
					\toVal, 0,
					\fadeTime, fTime,
					\fadeTrig, 1
				);
				fTime.wait;
				selectedSynth.free;
				library.putAtPath(envPath ++ \synths ++ nodeID.asSymbol, nil);
			}).play(currentEnvironment.clock);
		});
	}

	prCyclesDuration {|control|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var cyclesPath = [control.asSymbol, \stages, stage.asSymbol, \cycles];
		var envelopesPath = [control.asSymbol, \stages, stage.asSymbol, \envelopes];
		var cycles = library.atPath(cyclesPath);

		if((cycles != nil), {
			cycles.keysDo({|cycleKey|
				var envPattern = library.atPath(cyclesPath ++ cycleKey.asSymbol ++ \envPattern);
				var cycleDuration = 0;
				envPattern.do({|patternKey|
					var selectedDuration = library.atPath(envelopesPath ++ patternKey.asSymbol ++ \dur);
					if((selectedDuration == nil), { selectedDuration = 0; });
					cycleDuration = cycleDuration + selectedDuration;
				});
				library.putAtPath(cyclesPath ++ cycleKey.asSymbol ++ \cycleDur, cycleDuration);
			});
		});
	}

	prEvelopesArray { |control, cycleName|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var cyclesPath = [control.asSymbol, \stages, stage.asSymbol, \cycles];
		var envelopesPath = [control.asSymbol, \stages, stage.asSymbol, \envelopes];

		var pattern = library.atPath(cyclesPath ++ cycleName.asSymbol ++ \envPattern);
		var envelopes = library.atPath(envelopesPath);
		var arrEnv = List.new;

		pattern.do({|selector|
			var selectedEnv = library.atPath(envelopesPath ++ selector ++ \env);
			var selectedDuration = library.atPath(envelopesPath ++ selector ++ \dur);

			if((selectedEnv != nil), {
				var envDur = selectedEnv.duration;

				if((selectedDuration > envDur),
					{
						var endGap = selectedDuration - envDur;
						var levels = selectedEnv.levels;
						var times = selectedEnv.times;
						var curves = selectedEnv.curves;

						levels = levels.insert(levels.size,levels[levels.size-1]);
						times = times.insert(times.size,endGap);
						curves = curves.insert(curves.size,0);
					}
				);

				arrEnv.add(selectedEnv);
			});
		});

		^arrEnv.asArray;
	}

	prConnectEnvelopes { |arrEnv|
		var levels = List.new;
		var times = List.new;
		var curves = List.new;

		levels.add(arrEnv[0].levels[0]);

		arrEnv.do({|env|
			var oneL = env.levels;
			var oneT = env.times;
			var oneC = env.curves;
			oneL = oneL[1..oneL.size-1];

			oneL.size.do({|i|
				levels.add(oneL[i]);
				times.add(oneT.wrapAt(i));
				curves.add(oneC.wrapAt(i));
			});
		});

		^Env(levels.array, times.array, curves.array);
	}

	prUpdateTimeline {|control|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var stagePath = [control.asSymbol, \stages, stage.asSymbol];
		var cyclesPath = [control.asSymbol, \stages, stage.asSymbol, \cycles];
		var envelopesPath = [control.asSymbol, \stages, stage.asSymbol, \envelopes];

		var cyclePattern = library.atPath(stagePath ++ \cyclePattern);
		var stageLoopEnd = library.atPath(stagePath ++ \stageLoopEnd);
		var currentBeat = 0;

		var timeline = Order.new();

		if((stageLoopEnd == inf), { stageLoopEnd = 1; });

		stageLoopEnd.do({
			cyclePattern.do({|selectedCycle|
				var envPattern = library.atPath(cyclesPath ++ selectedCycle ++ \envPattern);

				envPattern.do({|patternKey|
					var selectedDuration = library.atPath(envelopesPath ++ patternKey.asSymbol ++ \dur);
					if(selectedDuration.isNil, { selectedDuration = 0; });

					timeline.put(currentBeat, patternKey);
					currentBeat = currentBeat + selectedDuration;
				});
			});
		});

		library.putAtPath(stagePath ++ \stageTimeline, timeline);
	}

	qTimeline {|control|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var stagePath = [control.asSymbol, \stages, stage.asSymbol];
		var cyclesPath = [control.asSymbol, \stages, stage.asSymbol, \cycles];
		var envelopesPath = [control.asSymbol, \stages, stage.asSymbol, \envelopes];

		var cyclePattern = library.atPath(stagePath ++ \cyclePattern);
		var stageLoopEnd = library.atPath(stagePath ++ \stageLoopEnd);
		var currentBeat = 0;
		var stageLoop = 0;

		// var timeline = Order.new();

		if((stageLoopEnd == inf), { stageLoopEnd = 1; });

		stageLoopEnd.do({
			("\nstageLoop" + stageLoop).postln;
			cyclePattern.do({|selectedCycle|
				var envPattern = library.atPath(cyclesPath ++ selectedCycle ++ \envPattern);

				("\n\t - selectedCycle" + selectedCycle).postln;

				envPattern.do({|patternKey|
					var selectedDuration = library.atPath(envelopesPath ++ patternKey.asSymbol ++ \dur);
					var selectedSynth = library.atPath(envelopesPath ++ patternKey.asSymbol ++ \synths);
					var fromBeat, toBeat;

					if((selectedDuration == nil), { selectedDuration = 0; });

					fromBeat = currentBeat;
					// timeline.put(fromBeat, patternKey);
					currentBeat = currentBeat + selectedDuration;
					toBeat = currentBeat;

					(
						"\t\t - " + fromBeat +
						"->" + toBeat +
						"\t env:" + patternKey
					).postln;
				});
			});
			stageLoop = stageLoop + 1;
		});

		// library.putAtPath(stagePath ++ \stageTimeline, timeline);
	}


}
















+ NodeProxy {

	qenv { |control, envName, env, duration = nil, fTime = 0|
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
						timeScale: currentEnvironment.clock.tempo.reciprocal,
						doneAction: 0
					);

					var fade = EnvGen.kr(
						Env([ \fromVal.kr(0), \toVal.kr(0)], \fadeTime.kr(fTime), \sin),
						gate:\fadeTrig.tr(0),
						timeScale: currentEnvironment.clock.tempo.reciprocal
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

	qplay {|control, pattern|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var stagePath = [control.asSymbol, \stages, stage.asSymbol];
		var cyclePath = [control.asSymbol, \stages, stage.asSymbol, \cycles];
		var envPath = [control.asSymbol, \stages, stage.asSymbol, \envelopes];
		var controlBus = library.atPath([control.asSymbol, \controlBus]);
		var stream = pattern.asStream;
		var controlDuration = 0;

		if((library.at(control.asSymbol) != nil), {
			this.set( control.asSymbol,	controlBus.asMap );

			case
			{ stream.isKindOf(Routine) } { stream = stream.all; } // Pseq([\aaa, \bbb], 3) ++ \ccc
			{ stream.isKindOf(Symbol) }	{ stream = stream.asArray; }
			{ stream.isKindOf(Integer) } { stream = stream.asSymbol.asArray; }
			{ stream.isKindOf(String) }	{ stream = stream.asSymbol.asArray; }
			;

			library.putAtPath(stagePath ++ \cyclePattern, stream);

			if((library.atPath(stagePath ++ \stageTask) != nil), {
				var firstCycleDuration = library.atPath(cyclePath ++ stream[0].asSymbol ++ \cycleDur );
				Task ({
					currentEnvironment.clock.timeToNextBeat(firstCycleDuration).wait;
					library.atPath(stagePath ++ \stageTask).stop;
				}).play(currentEnvironment.clock);
			});
			library.putAtPath(stagePath ++ \stageTask,
				Task ({
					var firstCycleDuration = library.atPath(cyclePath ++ stream[0].asSymbol ++ \cycleDur );
					currentEnvironment.clock.timeToNextBeat(firstCycleDuration).wait;
					{
						var selectedCycle = stream[0].asSymbol;
						var cyclePattern = library.atPath(cyclePath ++ selectedCycle ++ \envPattern );

						cyclePattern.do({|selectedEnv|
							var envDuration = library.atPath(envPath ++ selectedEnv.asSymbol ++ \dur );
							// var selectedSynth = library.atPath(envPath ++ selectedEnv ++ \synth );

							this.prTrigSynths(control, selectedEnv);

							envDuration.wait;
						});

						stream = stream.rotate(-1);
					}.loop;
				}).play(currentEnvironment.clock);
			);
		});

		library.postTree;
	}

	qstop { |control, fTime = 0|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var stagePath = [control.asSymbol, \stages, stage.asSymbol];

		library.atPath(stagePath ++ \stageTask).stop;
		library.putAtPath(stagePath ++ \stageTask, nil);

		this.unmap(control.asSymbol);

		// var busIndex = this.controlBusIndex(control);
		// var nodeKey = this.key;
		// var synthName = nodeKey ++ "_" ++ control;
		// var controlProxy = synthName.asSymbol.envirGet;
		// ("controlProxy" + controlProxy).postln;

		// controlProxy.bus.free(true);
		// controlProxy.clear;
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

		synthsDict.do({|selectedSynth|
			selectedSynth.set(\envTrig, 1);
		});

		// ("Trig synth" + envName + "-> at time" + currentEnvironment.clock.beats).postln;
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

				"FadeDone".postln;
				library.postTree;

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
				library.put(control.asSymbol, \controlBus, controlBus);
			}
		);

		^library;
	}


	/*
	prCrossFadeTask { |control, fTime, value|

	Task({
	var nodeFadeTime = this.fadeTime;
	this.fadeTime = fTime;
	this.xset( control.asSymbol, value );

	this.fadeTime = nodeFadeTime;

	fTime.wait;
	("CrossFadeTask" + fTime + "DONE").postln;
	this.nodeMap.at(\qMachine);

	}).play(currentEnvironment.clock);
	}
	*/

	/*
	qset { |control, quant, value, fTime = 1|
	var nodeKey = this.key;
	var synthName = nodeKey ++ "_" ++ control;
	var fadeName = "Fade_" ++ this.key ++ "_" ++ control;
	var valueType = value.class.asSymbol;

	{
	this.prInitQuantMachine(control);
	Server.default.sync;

	// ("ValueType:" + valueType).postln;

	case
	{ valueType == 'Integer' }	{
	this.prCrossFadeTask(control, fTime, value);
	}
	{ valueType == 'Float' }
	{
	this.prCrossFadeTask(control, fTime, value);
	}
	{ valueType == 'Env' }
	{
	var library = this.nodeMap.get(\qMachine).at(control.asSymbol, \controlLibrary);
	var bus = Bus.control(Server.default,1);
	var taskName = "task_" ++ bus.index;
	var nodeFadeTime = this.fadeTime;
	var oldTasks = List.new;

	library.treeDo({|branchName|
	if((branchName[0].asSymbol != nil.asSymbol),
	{
	("oldTasks" + branchName[0]).postln;
	oldTasks.add(branchName[0]);
	});
	});

	library.put(taskName.asSymbol, \bus, bus);
	library.put(taskName.asSymbol, \task,
	Task ({
	currentEnvironment.clock.timeToNextBeat(quant).wait;
	{
	Synth(synthName, [
	\controlBus: bus,
	\proxyTempo: currentEnvironment.clock.tempo,
	\env: [value],
	], this.group);
	quant.wait;
	}.loop;
	}).play(currentEnvironment.clock);
	);

	this.fadeTime = fTime;
	this.xset( control.asSymbol, bus.asMap );
	this.fadeTime = nodeFadeTime;

	Task({
	fTime.wait;
	("CrossFade" + control ++ fTime + "DONE").postln;

	oldTasks.do({|branchName|

	var deleteBus = library.at(branchName.asSymbol, \bus);
	var deleteTask = library.at(branchName.asSymbol, \task);

	deleteBus.free;
	deleteTask.stop;

	library.put(branchName.asSymbol, \bus, nil);
	library.put(branchName.asSymbol, \task, nil);
	library.put(branchName.asSymbol, nil);
	});

	// this.nodeMap.get(\qMachine).at(control.asSymbol, \controlLibrary).postTree;
	}).play(currentEnvironment.clock);
	};
	}.fork;
	}
	*/
}




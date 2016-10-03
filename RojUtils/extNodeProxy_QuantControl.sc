+ NodeProxy {

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


	qenv { |control, envName, env, duration = nil|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var path = [control.asSymbol, stage.asSymbol, \envelopes, envName.asSymbol];

		if((duration == nil), {duration = env.duration});

		library.putAtPath(path ++ \env, env);
		library.putAtPath(path ++ \dur, duration);

		library.postTree;
	}

	qcycle {|control, cycleName, pattern|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var path = [control.asSymbol, stage.asSymbol, \cycles, cycleName.asSymbol];

		var stream = pattern.asStream;
		var arrEnv, cycleEnv;

		case
		{ stream.isKindOf(Routine) } { stream = stream.all; } // Pseq([\aaa, \bbb], 3) ++ \ccc
		{ stream.isKindOf(Symbol) }	{ stream = stream.asArray; }
		{ stream.isKindOf(Integer) } { stream = stream.asSymbol.asArray; }
		{ stream.isKindOf(String) }	{ stream = stream.asSymbol.asArray; }
		;

		library.putAtPath(path ++ \pattern, stream);

		arrEnv = this.prEvelopesArray(control, cycleName);
		cycleEnv = this.prConnectEnvelopes(arrEnv);

		library.putAtPath(path ++ \cycleEnv, cycleEnv);
		library.putAtPath(path ++ \cycleDur, cycleEnv.duration);

		library.postTree;
	}

	qplay {|control, cycleName, quant, fadeTime|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var cycleEnv = library.at(control.asSymbol, stage.asSymbol, \cycles, cycleName.asSymbol, \cycleEnv);

		this.qset(control, quant, cycleEnv, fadeTime);
	}

	qstop { |control|
		// var busIndex = this.controlBusIndex(control);
		var nodeKey = this.key;
		var synthName = nodeKey ++ "_" ++ control;
		var controlProxy = synthName.asSymbol.envirGet;
		// ("controlProxy" + controlProxy).postln;

		this.unmap(control.asSymbol);
		controlProxy.bus.free(true);
		controlProxy.clear;
	}

	qplot {|control, cycleName, segments = 400|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var winName = control.asString + "[ stage:" + stage.asString + "|| cycleName:" + cycleName.asString + "]";

		library.at(control.asSymbol, stage.asSymbol, \cycles, cycleName.asSymbol, \cycleEnv).plot(segments, name:winName);
	}

	prEvelopesArray { |control, cycleName|
		var library = this.prGetLibrary(control);
		var stage = \default;
		var cyclesPath = [control.asSymbol, stage.asSymbol, \cycles];
		var envelopesPath = [control.asSymbol, stage.asSymbol, \envelopes];

		var pattern = library.atPath(cyclesPath ++ cycleName.asSymbol ++ \pattern);
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
						/*
						("Levels:" + levels).postln;
						("Times:" + times).postln;
						("Curves:" + curves).postln;
						*/
					}
				);

				arrEnv.add(selectedEnv);
				/*
				(
				"\n////////////////////"
				"\n selector:" + selector +
				"\n\t env:" + selectedEnv +
				"\n\t dur:" + selectedDuration
				).postln;
				*/
			});
		});

		^arrEnv.asArray;
	}

	prConnectEnvelopes { |arrEnv|
		// var connectedEnv;
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
			/*
			(
			"\n////////////////////"
			"\n oneL:" + oneL +
			"\n oneT:" + oneT +
			"\n oneC:" + oneC
			).postln;
			*/
		});
		/*
		("Levels:" + levels.array).postln;
		("Times:" + times.array).postln;
		("Curves:" + curves.array).postln;
		*/
		// connectedEnv = Env(levels.array, times.array, curves.array);

		^Env(levels.array, times.array, curves.array);
	}

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

	prGetLibrary { |control|
		var synthName = this.key ++ "_" ++ control;
		var library = this.nodeMap.get(\qMachine);

		if((library == nil), {
			this.nodeMap.put(\qMachine, MultiLevelIdentityDictionary.new);
			library = this.nodeMap.get(\qMachine);
			("NodeMap library qMachine prepared").postln;
		});

		if((library.at(control.asSymbol) == nil),
			{
				var synthDef = {|controlBus, proxyTempo = 1|
					Out.kr( controlBus,
						EnvGen.kr(
							\env.kr(Env.newClear(200,1).asArray),
							timeScale: proxyTempo.reciprocal,
							doneAction: 2
						)
					);
				};
				synthDef.asSynthDef(name:synthName.asSymbol).add;
				("SynthDef" + synthName + "added").postln;
		});
		^library;
	}
}




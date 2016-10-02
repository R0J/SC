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
				var library = this.nodeMap.get(\qMachine).at(control.asSymbol);
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
					}).play;
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

					this.nodeMap.get(\qMachine).at(control.asSymbol).postTree;
				}).play;
			};
		}.fork;
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

	qenv { |control, symbol, duration, env|
		var envLibrary;
		var stage = \default;
		this.prInitQuantMachine(control);
		envLibrary = this.nodeMap.get(\qMachine).at(control.asSymbol, \envLibrary);

		("\nControl:" + control).postln;
		envLibrary.put(stage.asSymbol, \envelopes, symbol.asSymbol, env);
		envLibrary.put(stage.asSymbol, \durations, symbol.asSymbol, duration);

		this.nodeMap.get(\qMachine).at(control.asSymbol, \envLibrary).postTree;
	}

	qselect {|control, symbol, stream|
		var envLibrary;
		var stage = \default;
		this.prInitQuantMachine(control);
		envLibrary = this.nodeMap.get(\qMachine).at(control.asSymbol, \envLibrary);

		("\nControl:" + control).postln;
		envLibrary.put(stage.asSymbol, \streams, symbol.asSymbol, stream);

		this.nodeMap.get(\qMachine).at(control.asSymbol, \envLibrary).postTree;
	}

	qplot {|control, symbol|
		// this.prConnectEnvelopes
		var arrEnv = this.prEvelopesArray(control, symbol);
		var outEnv = this.prConnectEnvelopes(arrEnv);
		("ArrEnv:" + arrEnv).postln;
		outEnv.plot(name:control.asSymbol);
	}

	prEvelopesArray { |control, symbol|
		var stage = \default;
		var envLibrary = this.nodeMap.get(\qMachine).at(control.asSymbol, \envLibrary);
		var stream = envLibrary.at(stage.asSymbol, \streams, symbol.asSymbol);
		var arrEnv = List.new;

		// ("stream:" + stream).postln;

		stream.do({|selector|
			var selectedEnv = envLibrary.at(stage.asSymbol, \envelopes, selector.asSymbol);
			var selectedDuration = envLibrary.at(stage.asSymbol, \durations, selector.asSymbol);

			// if((envCouter > 0), {selectedEnv.levels.removeAt(0)});
			arrEnv.add(selectedEnv);

			(
				"\n////////////////////"
				"\n selector:" + selector +
				// "\n\t env:" + selectedEnv.asArrayForInterpolation +
				// "\n\t env:" + selectedEnv.asControlInput +
				"\n\t env:" + selectedEnv +
				"\n\t dur:" + selectedDuration
			).postln;
			// which.postln;
			// envCouter = envCouter + 1;
		});
		^arrEnv.asArray;
	}

	prConnectEnvelopes { |arrEnv|
		var connectedEnv;
		var levels = List.new;
		var times = List.new;
		var curves = List.new;


		levels.add(arrEnv[0].levels[0]);

// env.levels.removeAt(0);

		arrEnv.do({|env|
			var oneL = env.levels;
			var oneT = env.times;
			var oneC = env.curves;
			oneL = oneL[1..oneL.size];

			oneL.size.do({|i|
				levels.add(oneL[i]);
				times.add(oneT.wrapAt(i));
				curves.add(oneC.wrapAt(i));
			});
			(
				"\n////////////////////"
				"\n oneL:" + oneL +
				"\n oneT:" + oneT +
				"\n oneC:" + oneC
			).postln;
		});

		("Levels:" + levels).postln;
		("Times:" + times).postln;
		("Curves:" + curves).postln;

		connectedEnv = Env(levels, times, curves);
		connectedEnv.duration.postln;

		^connectedEnv;
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

		}).play;

	}

	prInitQuantMachine { |control|
		var synthName = this.key ++ "_" ++ control;

		if((this.nodeMap.get(\qMachine) == nil), {
			var library = MultiLevelIdentityDictionary.new;
			this.nodeMap.put(\qMachine, library);
			("NodeMap qMachine prepared").postln;
		});

		if((this.nodeMap.get(\qMachine).at(control.asSymbol) == nil),
			{
				var envLibrary = MultiLevelIdentityDictionary.new;
				var controlLibrary = MultiLevelIdentityDictionary.new;
				var synthDef = {|controlBus, proxyTempo = 1|
					Out.kr( controlBus,
						EnvGen.kr(
							\env.kr(Env.newClear().asArray),
							timeScale: proxyTempo.reciprocal,
							doneAction: 2
						)
					);
				};
				synthDef.asSynthDef(name:synthName.asSymbol).add;
				("SynthDef" + synthName + "ulozen").postln;
				this.nodeMap.get(\qMachine).put(control.asSymbol, \envLibrary, envLibrary);
				this.nodeMap.get(\qMachine).put(control.asSymbol, \controlLibrary, controlLibrary);
		});
	}
}




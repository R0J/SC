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
				this.nodeMap.get(\qMachine).put(control.asSymbol, controlLibrary);
		});
	}
}




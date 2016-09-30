+ NodeProxy {

	qset { |control, quant, value, fadeTime = 1|
		var nodeKey = this.key;
		var synthName = nodeKey ++ "_" ++ control;
		var fadeName = "Fade_" ++ this.key ++ "_" ++ control;
		var valueType = value.class.asSymbol;

		{
			this.prInitQuantMachine(control);
			Server.default.sync;

			("ValueType:"+valueType).postln;

			case
			{ valueType == 'Integer' }	{ "JSEM INT".postln; }
			{ valueType == 'Float' }
			{
				var library = this.nodeMap.get(\qMachine);
				var bus = library.at(control.asSymbol,\bus);
				var oldFadeSynth = library.at(control.asSymbol,\fadeSynth);

				"JSEM FLOAT".postln;

				if((oldFadeSynth != nil),
					{
						library.at(control.asSymbol,\fadeTask).stop;
						library.at(control.asSymbol,\fadeSynth).free;
						library.at(control.asSymbol,\bus).get({|busValue| bus.setAt(2, busValue[0]); });
					}
				);

				library.put(control.asSymbol,\fadeTask,
					Task({
						bus.setAt(1,value);

						library.put(control.asSymbol,\fadeSynth,
							Synth(fadeName.asSymbol, [
								\controlBus: bus,
								\fadeTime: fadeTime
							], this.group);
						);

						fadeTime.wait;

						("FadeTime" + fadeTime + "END").postln;
						bus.setAt(2, value);
					}).play;
				);
			}
			{ valueType == 'Env' }
			{
				var library = this.nodeMap.get(\qMachine);
				var bus = library.at(control.asSymbol,\bus);

				"JSEM ENV".postln;

				library.put(control.asSymbol,\fadeTask,
					Task({

						if((library.at(control.asSymbol,\currentTask) != nil),
							{
								library.at(control.asSymbol,\currentTask).stop;
								library.at(control.asSymbol,\oldTask).stop;

								library.put(control.asSymbol,\oldTask, library.at(control.asSymbol,\newTask));
								library.put(control.asSymbol,\oldQuant, library.at(control.asSymbol,\newQuant));
								library.put(control.asSymbol,\oldEnv, library.at(control.asSymbol,\newEnv));

							}
						);

						library.put(control.asSymbol,\newQuant, quant);
						library.put(control.asSymbol,\newEnv, value);

						if((library.at(control.asSymbol,\currentTask) != nil),
							{
								library.put(control.asSymbol,\oldTask,
									Task ({
										currentEnvironment.clock.timeToNextBeat(
											library.at(control.asSymbol,\oldQuant)
										).wait;

										{
											Synth(synthName, [
												\controlBus: bus,
												\subIndex: 3,
												\proxyTempo: currentEnvironment.clock.tempo,
												\env: [library.at(control.asSymbol,\oldEnv)],
											], this.group);
											library.at(control.asSymbol,\oldQuant).wait;
										}.loop;
									}).play;
								);
						});

						library.put(control.asSymbol,\currentTask,
							Task ({
								currentEnvironment.clock.timeToNextBeat(
									library.at(control.asSymbol,\newQuant)
								).wait;
								{
									Synth(synthName, [
										\controlBus: bus,
										\subIndex: 2,
										\proxyTempo: currentEnvironment.clock.tempo,
										\env: [library.at(control.asSymbol,\newEnv)],
									], this.group);
									library.at(control.asSymbol,\newQuant).wait;
								}.loop;
							}).play;
						);
						library.put(control.asSymbol,\fadeSynth,
							Synth(fadeName.asSymbol, [
								\controlBus: bus,
								\fadeTime: fadeTime
							], this.group);
						);

						fadeTime.wait;

						this.set(
							control.asSymbol,
							(library.at(control.asSymbol,\bus).index+1).asMap
						);

						("FadeTime" + fadeTime + "END").postln;
					}).play;
				);

				library.at(control.asSymbol).postln;
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

	prInitQuantMachine { |control|

		var synthName = this.key ++ "_" ++ control;
		var fadeName = "Fade_" ++ this.key ++ "_" ++ control;

		if((this.nodeMap.at(control.asSymbol) == nil),
			{

				var library = MultiLevelIdentityDictionary.new;
				var synthDef = {|controlBus, subIndex, proxyTempo = 1|
					// var envControlBus = In.kr(controlBus, 3);
					Out.kr( subIndex,
						EnvGen.kr(
							\env.kr(Env.newClear().asArray),
							timeScale: proxyTempo.reciprocal,
							doneAction: 2
						)
					);
				};
				var fadeDef = {|controlBus, fadeTime|
					ReplaceOut.kr( controlBus,
						SelectX.kr(
							EnvGen.kr(
								Env([0,1], fadeTime, \lin),
								timeScale: currentEnvironment.clock.tempo.reciprocal,
								doneAction: 2
							),
							[ In.kr(controlBus, 3)[2], In.kr(controlBus, 3)[1] ]
						)
					);
				};

				synthDef.asSynthDef(name:synthName.asSymbol).add;
				fadeDef.asSynthDef(name:fadeName.asSymbol).add;

				// "SynthDefy ulozeny".postln;

				this.nodeMap.put(\qMachine, library);

				library.put(control.asSymbol, \bus, Bus.control(Server.default,3));
				library.put(control.asSymbol, \fadeTime, 0);

				this.set(
					control.asSymbol,
					this.nodeMap.get(\qMachine).at(control.asSymbol,\bus).asMap
				);
			}
		);


	}
}




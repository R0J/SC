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
			{ valueType == 'Integer' }
			{
				"JSEM INT".postln;
			}
			{ valueType == 'Float' }
			{
				var bus = this.nodeMap.get(\qMachine).at(control.asSymbol,\bus);
				var oldFadeSynth = this.nodeMap.get(\qMachine).at(control.asSymbol,\fadeSynth);
				if((oldFadeSynth != nil),
					{
						this.nodeMap.get(\qMachine).at(control.asSymbol,\fadeTask).stop;
						this.nodeMap.get(\qMachine).at(control.asSymbol,\fadeSynth).free;
						this.nodeMap.get(\qMachine).at(control.asSymbol,\bus).get({|busValue| bus.setAt(2, busValue[0]); });
					}
				);

				"JSEM FLOAT".postln;
				this.nodeMap.get(\qMachine).put(control.asSymbol,\fadeTask,
					Task({
						bus.setAt(1,value);

						this.nodeMap.get(\qMachine).put(control.asSymbol,\fadeSynth,
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
				"JSEM ENV".postln;
			};
		}.fork;
		/*
		var controlProxy;
		var oldControlProxy;
		var krProxyCreate = block {|break|
		currentEnvironment.krProxyNames.collect{|krProxy|
		krProxy.postln;
		if(krProxy.asSymbol == synthName.asSymbol)
		{
		break.value(false);
		}
		};
		break.value(true);
		};

		// ("krProxyFound" + krProxyFound).postln;

		if(krProxyCreate, {
		synthName.asSymbol.envirPut( NodeProxy.new( Server.local,\control, 1));
		controlProxy = synthName.asSymbol.envirGet;
		// controlProxy.group_(this.group);

		oldControlProxy = nil;

		},
		{
		oldControlProxy = synthName.asSymbol.envirGet;
		controlProxy = oldControlProxy.copy;
		controlProxy.setGroup (this.group);
		synthName.asSymbol.envirPut(controlProxy);
		}
		);
		if(oldControlProxy != nil)
		{
		("oldControlProxy:" + oldControlProxy).postln;
		("oldControlProxy.group:" + oldControlProxy.group).postln;
		("oldControlProxy.source:" + oldControlProxy.source).postln;
		};

		("controlProxy:" + controlProxy).postln;
		("controlProxy.group:" + controlProxy.group).postln;
		("controlProxy.source:" + controlProxy.source).postln;

		controlProxy[0] = Task ({
		currentEnvironment.clock.timeToNextBeat(quant).wait;
		{
		Synth(synthName, [
		\bus: controlProxy.bus,
		\proxyTempo: currentEnvironment.clock.tempo,
		\env: [env],
		], this.group);

		/*
		(
		"ProxyClock beats: " ++  currentEnvironment.clock.beats ++
		" tempo: " ++ currentEnvironment.clock.tempo
		).postln;
		*/
		quant.wait;
		}.loop;
		});
		if(oldControlProxy != nil)
		{
		("stoping old task:").postln;
		oldControlProxy[0].stop;
		oldControlProxy.free(8);
		};

		this.fadeTime_(8);
		this.xset(control.asSymbol, controlProxy);

		};
		// this.nodeMap.postln;
		*/
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
				var synthDef = {|bus, proxyTempo = 1|
					Out.kr( bus,
						EnvGen.kr(
							\env.kr( Env.newClear().asArray ),
							timeScale: proxyTempo.reciprocal,
							doneAction: 2
						)
					);
				};
				var fadeDef = {|controlBus, fadeTime|
					Out.kr( controlBus,
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

		this.nodeMap.postln;
	}
}




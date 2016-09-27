+ NodeProxy {

	qset { |control, quant, env|
		var nodeKey = this.key;
		var synthName = nodeKey ++ "_" ++ control;
		var envType = env.class.asSymbol;

		var synthDef = {|bus, proxyTempo = 1|
			Out.kr( bus,
				EnvGen.kr(
					\env.kr( Env.newClear().asArray ),
					timeScale: proxyTempo.reciprocal,
					doneAction: 2
				)
			);
		};
		synthDef.asSynthDef(name:synthName).add;

		case
		{ envType == 'Env' }
		{
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
}
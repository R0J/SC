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
			var krProxyFound = block {|break|
				currentEnvironment.krProxyNames.collect{|krProxy|
					krProxy.postln;
					if(krProxy.asSymbol == synthName.asSymbol)
					{
						("Found krProxy:"+control).postln;
						//krProxyFound = true;
						break.value(true);
					}
				};
				break.value(false);
			};

			// ("krProxyFound" + krProxyFound).postln;

			if(krProxyFound, {},
				{
					synthName.asSymbol.envirPut( NodeProxy.new( Server.local,\control, 1));
				}
			);

			controlProxy = synthName.asSymbol.envirGet;
			controlProxy.fadeTime = 8;
			// ("controlProxy" + controlProxy).postln;
			("controlProxy.bus" + controlProxy.bus).postln;

			controlProxy.source_(
				Task {
					currentEnvironment.clock.timeToNextBeat(quant).wait;
					loop {
						Synth(synthName, [
							\bus: controlProxy.bus,
							\proxyTempo: currentEnvironment.clock.tempo,
							\env: [env]
						], this.group);
						/*
						(
						"ProxyClock beats: " ++  currentEnvironment.clock.beats ++
						" tempo: " ++ currentEnvironment.clock.tempo
						).postln;
						*/
						quant.wait;
					}
				}
			);

			this.set(control.asSymbol, controlProxy);
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
		controlProxy.clear;
	}
}
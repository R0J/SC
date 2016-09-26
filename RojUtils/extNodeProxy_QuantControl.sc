+ NodeProxy {

	qset { |control, quant, env|
		var nodeKey = this.key;
		var synthName = nodeKey ++ "_" ++ control;
		var busIndex = this.controlBusIndex(control);
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

		("NodeGroup:" + this.group).postln;

		case
		{ envType == 'Env' }
		{
			var nodeSlot = busIndex + 30;

			this.set(control.asSymbol, BusPlug.for(busIndex));
			this.put(nodeSlot,
				Task {
					currentEnvironment.clock.timeToNextBeat(quant).wait;
					loop {
						Synth(synthName, [
							\bus: busIndex,
							\proxyTempo: currentEnvironment.clock.tempo,
							\env: [env]
						], this.group);
						/*
						(
						"ProxyClock beats: " ++  currentEnvironment.clock.beats ++
						" tempo: " ++ p.clock.tempo
						).postln;
						*/
						quant.wait;
					}
				}
			);
		};
		// this.nodeMap.postln;
	}

	qstop { |control|
		var busIndex = this.controlBusIndex(control);

		this.put(busIndex + 30, nil);
		this.unmap(control.asSymbol);
	}

	controlBusIndex { |control|
		var nodeKey = this.key;
		var busIndex;
		if(Library.global.at(nodeKey.asSymbol, control.asSymbol) == nil,
			{
				busIndex = Library.global.flatSize+1;
				Library.put(nodeKey.asSymbol, control.asSymbol,busIndex);
			},
			{
				busIndex = Library.global.at(nodeKey.asSymbol, control.asSymbol);
			}
		);
		Library.postTree;
		^busIndex;
	}
}
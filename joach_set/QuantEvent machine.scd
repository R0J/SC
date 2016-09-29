(
e = ();
e.hraj = {|e, node, control, quant, env|

	var envType = env.class.asSymbol;
	var synthName = node.key++"_"++control;
	var busIndex = e.busIndex(node, control);
	var tolerance = 0.001;

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
	{ envType == 'Integer' }  {
		node[busIndex+30] = Task {
			p.clock.timeToNextBeat(quant).wait;
			node.set(control.asSymbol, env);
			"Integer set".postln;
		}
	}
	{ envType == 'Float' }  {
		node[busIndex+30] = Task {
			p.clock.timeToNextBeat(quant).wait;
			node.set(control.asSymbol, env);
			"Float set".postln;
		}
	}
	{ envType == 'Env' }  {
		"Env set".postln;
		//e.envRange(env);

		node.set(control.asSymbol, BusPlug.for(busIndex));

		node[busIndex+30] = Task {
			p.clock.timeToNextBeat(quant).wait;
			loop {
				Synth(synthName, [
					\bus: busIndex,
					\proxyTempo: p.clock.tempo,
					\env: [env]
				]);

				quant.wait;

				if(p.clock.beats % quant > tolerance, {
					//e.hraj(node, control, quant, env);
				});
				// ("ProxyClock beats: " ++  p.clock.beats ++ " tempo: " ++ p.clock.tempo).postln;
			}
		}
	}
	{ envType == 'Pswitch' }  {
		var counter = 0;
		var totalDur = 0;
		var quantList = List.new();
		var sumQuant = quant.sum;
		var currentSumQuant = 0;

		var pattern = env.which;
		var repeats = pattern.repeats;
		var selectArray;

		if(repeats == inf){	pattern.repeats_(1); };
		selectArray = pattern.asStream.all;

		("Repeats: " + repeats).postln;
		("SelectArray: " + selectArray).postln;

		selectArray.do({|noSelect|
			var selectQuant = quant.asArray.wrapAt(noSelect);
			quantList.add(selectQuant);
			totalDur = totalDur + selectQuant;
		});

		("quantList:" + quantList).postln;

		node[busIndex+30] = Task {
			// p.clock.timeToNextBeat(totalDur).wait;
			quantList[0].wait;
			node.set(control.asSymbol, BusPlug.for(busIndex));

			loop {
				var currentIndex = selectArray.wrapAt(counter);
				var currentQuant = quantList.wrapAt(counter);
				var currentEnv = env.list[currentIndex];
				/*
				(
				"currentIndex:" + currentIndex +
				"currentQuant:" + currentQuant +
				"currentEnv:" + (currentEnv[0]).asArray
				).postln;
				*/

				Synth(synthName, [
					\bus: busIndex,
					\proxyTempo: p.clock.tempo,
					\env: [currentEnv]
				]);
				counter = counter + 1;
				currentQuant.wait;
			}
		}
	}
	;

};

e.zastav = {|e, node, control|
	var busIndex = e.busIndex(node, control);
	var bus = BusPlug.for(busIndex);
	var nodeMap = node.nodeMap;
	var fadeTime = 10;
	var fadeTask;
	/*
	("Node:" + node.key).postln;
	("Control:" + control).postln;
	("BusIndex:" + busIndex).postln;
	("Bus:" + bus).postln;
	("BusMonitor:" + bus.monitor).postln;

	("NodeMap:" + node.nodeMap).postln;
	("NodeMap:" + node.nodeMap.at(control)).postln;
	*/

	// e.makeFadeSynth(busIndex);
	node[busIndex+30] = nil;
	node.unmap(control.asSymbol);
	// nodeMap.sendToNode(node);

	/*
	bus.stop(fadeTime, true);
	fadeTask = Task {
	bus * Line.kr(1,0,fadeTime, doneAction:2);
	fadeTime.wait;
	("fadeTime DONE:" + busIndex).postln;
	node[busIndex+30] = nil;
	};
	fadeTask.start;
	*/
};

e.busIndex = {|e,node, control|
	var busIndex;

	if(Library.global.at(node.key.asSymbol, control.asSymbol) == nil,
		{
			busIndex = Library.global.flatSize+1;
			Library.put(node.key.asSymbol, control.asSymbol,busIndex);
		},
		{
			busIndex = Library.global.at(node.key.asSymbol, control.asSymbol);
		}
	);
	Library.postTree;
	busIndex;
};
)


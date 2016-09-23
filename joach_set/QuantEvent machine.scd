(
e = ();
e.hraj = {|e, node, control, quant, env|

	var envType = env.class.asSymbol;
	var synthName = node.key++"_"++control;
	var busIndex = e.busIndex(node, control);
	var tolerance = 0.001;

	//e.makeSynthDef(synthName);
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
					e.hraj(node, control, quant, env);
				});
				// ("ProxyClock beats: " ++  p.clock.beats ++ " tempo: " ++ p.clock.tempo).postln;
			}
		}
	}
	{ envType == 'Pswitch' }  {
		var counter = 0;
		var sumQuant = quant.sum;
		var currentSumQuant = 0;
		var patternSize = env.which.list.size;
		var totalDur = 0;
		var quantList = List.new();

		"Pswitch set".postln;
		patternSize.do({|i|
			var noSelect = env.which.list.at(i);
			var selectQuant = quant.asArray.wrapAt(noSelect);
			("id:"+noSelect+"; q:" + selectQuant).postln;
			quantList.add(selectQuant);
			totalDur = totalDur + selectQuant;
		});
		("quantList:"+quantList).postln;

		node.set(control.asSymbol, BusPlug.for(busIndex));
		node[busIndex+30] = Task {
			p.clock.timeToNextBeat(totalDur).wait;
			loop {
				var currentQuant = quantList.wrapAt(counter);
				// var currentQuant = quant.asArray.wrapAt(counter%(patternSize-1));
				var currentIndex = env.which.list[counter%patternSize];
				var currentEnv = env.list[currentIndex];


				Synth(synthName, [
					\bus: busIndex,
					\proxyTempo: p.clock.tempo,
					\env: [currentEnv]
				]);
				("\nProxyClock beats: " ++  p.clock.beats ++ " tempo: " ++ p.clock.tempo).postln;
				(
					"currentQuant: " ++ currentQuant ++
					"; currentIndex: " ++ currentIndex //++
					// "; currentEnv:" ++ [currentEnv].asArray).toString
				).postln;
				counter = counter + 1;
				currentSumQuant = currentSumQuant + currentQuant;
				currentQuant.wait;

				if(sumQuant <= currentSumQuant, {

					"END OF CYCLE".postln;
					(
					"beat:" + p.clock.beats +
					"; totalDur:" + totalDur +
					"; Mod: "++(p.clock.beats % totalDur)
					).postln;

					if(p.clock.beats % (totalDur) > tolerance, {
						"Reset CYCLE".postln;
						e.hraj(node, control, quant, env);
					});

					currentSumQuant = 0;
				});

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
	("Node:" + node.key).postln;
	("Control:" + control).postln;
	("BusIndex:" + busIndex).postln;
	("Bus:" + bus).postln;
	("BusMonitor:" + bus.monitor).postln;

	("NodeMap:" + node.nodeMap).postln;
	("NodeMap:" + node.nodeMap.at(control)).postln;
	// bus.clear;
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

e.makeSynth = {|e, name|
	var synthDef = {|bus, proxyTempo = 1|
		Out.kr( bus,
			EnvGen.kr(
				\env.kr( Env.newClear().asArray ),
				timeScale: proxyTempo.reciprocal,
				doneAction: 2
			)
		);
	};
	synthDef.asSynthDef(name:name).add;
};

/*
e.makeFadeSynth = {|e, bus|
var synthDef = {|bus, proxyTempo = 1|
Out.kr( bus, InBus.kr(bus, 0) * Line.kr(1,0,10));
};
synthDef.asSynthDef(name:"fadeOut").add;
};
*/

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


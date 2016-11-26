NodeComposition {
	classvar <library;
	classvar currentStage;
	classvar compositionClock;
	classvar stageChangeTask = nil;

	classvar compositionTimeline = nil;

	*initLibrary {
		if(library.isNil) {
			library = MultiLevelIdentityDictionary.new();
			("NodeComposition library init...").postln;
		};
	}

	*addNode {|node|
		var path = [node.envirKey.asSymbol, \node];
		this.initLibrary;
		if(library.atPath(path).isNil)
		{
			library.putAtPath(path, node);
			("NodeProxy ~" ++  node.envirKey + "added to library").postln;
		}
	}

	*getNode {|nodeName|
		if(library.notNil) {
			var path = [nodeName.asSymbol, \node];
			var node = library.atPath(path);
			if(node.notNil) { ^node; };
		};
		^nil;
	}

	*addBus {|nodeEnv|
		var path = [nodeEnv.nodeName.asSymbol, \buses, nodeEnv.controlName.asSymbol];
		if(library.atPath(path).isNil)
		{
			library.putAtPath(path, Bus.control(Server.default, 1));
			("Control bus" + nodeEnv.controlName + "mapped to NodeProxy ~" ++ nodeEnv.nodeName).postln;
		};
	}

	*getBus {|nodeName, controlName|
		if(library.notNil) {
			var path = [nodeName.asSymbol, \buses, controlName.asSymbol];
			var bus = library.atPath(path).index;
			if(bus.notNil) { ^bus; };
		};
		^nil;
	}

	*addEnvelope { |nodeEnv|
		var path = [nodeEnv.nodeName.asSymbol, \envelopes, nodeEnv.envelopeName.asSymbol];
		var envName = nodeEnv.nodeName ++ "_" ++ nodeEnv.envelopeName;
		this.initLibrary;
		if(library.atPath(path).isNil)
		{
			library.putAtPath(path, nodeEnv);
			("NodeEnv" +  envName + "added to library").postln;
		}
	}

	*getEnvelope {|nodeName, envelopeName|
		if(library.notNil) {
			var path = [nodeName.asSymbol, \envelopes, envelopeName.asSymbol];
			var nEnv = library.atPath(path);
			if(nEnv.notNil) { ^nEnv; };
		};
		^nil;
	}

	*addCycle {|nodeCycle|
		var path = [nodeCycle.nodeName.asSymbol, \cycles, nodeCycle.cycleName.asSymbol];
		this.initLibrary;
		if(library.atPath(path).isNil)
		{
			library.putAtPath(path, nodeCycle);
			("NodeCycle" + nodeCycle + "added to library").postln;
		}
	}

	*getCycle {|nodeName, cycleName|
		if(library.notNil) {
			var path = [nodeName.asSymbol, \cycles, cycleName.asSymbol];
			var nCycle = library.atPath(path);
			if(nCycle.notNil) { ^nCycle; };
		};
		^nil;
	}

	*addStage {|nodeStage|
		var path = [nodeStage.nodeName.asSymbol, \stages, nodeStage.stageName.asSymbol];
		this.initLibrary;
		if(library.atPath(path).isNil)
		{
			library.putAtPath(path, nodeStage);
			("NodeStage" + nodeStage + "added to library").postln;
		}
	}

	*getStage {|nodeName, stageName|
		if(library.notNil) {
			var path = [nodeName.asSymbol, \stages, stageName.asSymbol];
			var nStage = library.atPath(path);
			if(nStage.notNil) { ^nStage; };
		};
		^nil;
	}
	/*
	*updateTimes {|nodeName, controlName|
	if(library.notNil) {
	var cyclesFolder = library.atPath([nodeName.asSymbol, \cycles]);
	var stageFolder = library.atPath([nodeName.asSymbol, \stages]);

	if(cyclesFolder.notNil) {
	cyclesFolder.sortedKeysValuesDo({|oneCycleName|
	var oneCycle = this.getCycle(nodeName, oneCycleName);
	// oneCycle.set(controlName, oneCycle.envelopePattern.at(controlName.asSymbol), 0) // POZOR, uz nefunguje
	});
	};

	if(stageFolder.notNil) {
	stageFolder.sortedKeysValuesDo({|oneStageName|
	// var oneStage = this.getStage(nodeName, oneStageName); // POZOR, uz nefunguje
	oneStage.set(oneStage.cyclePattern, 0)
	});
	};
	}
	}
	*/

	*playStage {|stageName, fadeTime = 0, quantOfChange = 16|
		if(stageName.asSymbol != currentStage.asSymbol)
		{
			this.initLibrary;
			currentStage = stageName;
			if(stageChangeTask.notNil) { stageChangeTask.stop; };

			stageChangeTask = Task({
				currentEnvironment.clock.timeToNextBeat(quantOfChange).wait;

				library.dictionary.keysValuesDo({|nodeName, dict|
					var node = this.getNode(nodeName);
					var path = [nodeName.asSymbol, \stages];

					library.atPath(path).keysValuesDo({|oneStageName, nStage|
						if((nStage.stageName.asSymbol == stageName.asSymbol),
							{
								node.play(vol:1, fadeTime:fadeTime);
								nStage.play;
								nStage.fadeIn(fadeTime);
							},{
								nStage.fadeOut(fadeTime);
								nStage.stop(fadeTime);
								node.free(fadeTime)
						});
					});
				});
				stageChangeTask = nil;
			}).play;
		};
	}

	*test { |stageName, fadeTime = 0, quantOfChange = 0|
		// if(stageName.asSymbol != currentStage.asSymbol)
		// {
		this.initLibrary;
		currentStage = stageName;
		("currentStage: \\" ++ currentStage).postln;
		// if(stageChangeTask.notNil) { stageChangeTask.stop; };

		stageChangeTask = Task({
			currentEnvironment.clock.timeToNextBeat(quantOfChange).wait;

			library.dictionary.keysValuesDo({|nodeName, dict|
				var node = this.getNode(nodeName);
				var path = [nodeName.asSymbol, \stages];
				("nodeName: ~" ++ nodeName).postln;

				library.atPath(path).keysValuesDo({|oneStageName, nStage|
					if((nStage.stageName.asSymbol == stageName.asSymbol),
						{
							("START jsem tu:" + nodeName).warn;
							node.play(vol:1, fadeTime:fadeTime);
							nStage.play;
							nStage.fadeIn(fadeTime);
						},{
							("FREE  jsem tu:" + nodeName).warn;
							nStage.fadeOut(fadeTime);
							nStage.free(fadeTime);
							node.free(fadeTime)
					});
				});
			});
			// stageChangeTask = nil;
		}).play(currentEnvironment.clock);
		// };
	}

	*play {|from = 0, to = nil, loop = false|
		if(stageChangeTask.notNil) { stageChangeTask.stop; };

		library.dictionary.keysValuesDo({|nodeName, dict|
			var path = [nodeName.asSymbol, \stages];
			library.atPath(path).keysValuesDo({|oneStageName, nStage|
				nStage.play;
				nStage.fadeIn(4);
			});
		});
	}

	*stop {|fadeTime = 0|
		if(stageChangeTask.notNil) { stageChangeTask.stop; };

		library.dictionary.keysValuesDo({|nodeName, dict|
			var path = [nodeName.asSymbol, \stages];
			library.atPath(path).keysValuesDo({|oneStageName, nStage|
				nStage.fadeOut(fadeTime);
				nStage.stop(fadeTime);
			});
		});

		currentStage = nil;
	}

	*print {
		if(library.notNil) {
			"\n\nNodeComposition library \n-----------------------".postln;

			library.dictionary.sortedKeysValuesDo({|nodeName, dict|

				var envFolder = library.atPath([nodeName.asSymbol, \envelopes]);
				var cyclesFolder = library.atPath([nodeName.asSymbol, \cycles]);
				var stageFolder = library.atPath([nodeName.asSymbol, \stages]);

				("NodeProxy: ~" ++ nodeName).postln;

				if(envFolder.notNil) {
					"\tenvelopes:".postln;
					library.atPath([nodeName.asSymbol,\envelopes]).sortedKeysValuesDo({|oneControlNames|
						("\t\t \\" ++ oneControlNames).postln;
						library.atPath([nodeName.asSymbol,\envelopes, oneControlNames.asSymbol]).sortedKeysValuesDo({|oneEnvName|
							var oneEnv = library.atPath([nodeName.asSymbol, \envelopes, oneControlNames.asSymbol, oneEnvName.asSymbol]);
							("\t\t\t \\" ++ oneEnvName ++ " -> NodeEnv [ dur:" + oneEnv.duration + "]").postln;
							oneEnv.print(4);
						});
					});
				};

				if(cyclesFolder.notNil) {
					"\tcycles:".postln;
					cyclesFolder.sortedKeysValuesDo({|oneCycleNames|
						var oneCycle = library.atPath([nodeName.asSymbol,\cycles] ++ oneCycleNames.asSymbol);
						("\t\t\\" ++ oneCycleNames ++ " -> NodeCycle [ qnt:" + oneCycle.cycleQuant + "; dur:" + oneCycle.duration + "]").postln;
						oneCycle.timeline.print(3);
					});
				};

				if(stageFolder.notNil) {
					"\tstages:".postln;
					stageFolder.sortedKeysValuesDo({|oneStageNames|
						var oneStage = library.atPath([nodeName.asSymbol,\stages] ++ oneStageNames.asSymbol);
						("\t\t\\" ++ oneStageNames ++ " -> NodeStage [ dur:" + oneStage.timeline.duration + "]").postln;
						oneStage.timeline.print(3);
					});
				};
			});
		}
	}

}
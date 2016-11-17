NodeComposition {
	classvar <library;
	classvar currentStage;

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
			("Node" +  node.envirKey + "added to library").postln;
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
			("Control bus" + nodeEnv.controlName + " maped to node" + nodeEnv.nodeName).postln;
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
		var path = [nodeEnv.nodeName.asSymbol, \envelopes, nodeEnv.controlName.asSymbol, nodeEnv.envelopeName.asSymbol];
		var envName = nodeEnv.nodeName ++ "_" ++ nodeEnv.controlName ++ "_" ++ nodeEnv.envelopeName;
		this.initLibrary;
		if(library.atPath(path).isNil)
		{
			library.putAtPath(path, nodeEnv);
			("NodeEnv" +  envName + "added to library").postln;
		}
	}

	*getEnvelope {|nodeName, controlName, envelopeName|
		if(library.notNil) {
			var path = [nodeName.asSymbol, \envelopes, controlName.asSymbol, envelopeName.asSymbol];
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

	*updateTimes {|nodeName, controlName, envelopeName|
		if(library.notNil) {
			var cyclesFolder = library.atPath([nodeName.asSymbol, \cycles]);
			var stageFolder = library.atPath([nodeName.asSymbol, \stages]);

			if(cyclesFolder.notNil) {
				cyclesFolder.sortedKeysValuesDo({|oneCycleName|
					var oneCycle = this.getCycle(nodeName, oneCycleName);
					// ("NodeComposition.updateTimes:" + oneCycle.envelopePattern.at(controlName.asSymbol)).postln;
					oneCycle.set(controlName, oneCycle.envelopePattern.at(controlName.asSymbol), 0)
				});
			};

			if(stageFolder.notNil) {
				stageFolder.sortedKeysValuesDo({|oneStageName|
					var oneStage = this.getStage(nodeName, oneStageName);
					oneStage.set(oneStage.cyclePattern, 0)
				});
			};
		}
	}

	*stage {|stageName, fadeTime = 0|
		this.initLibrary;
		currentStage = stageName;
	}

	*play {|from = 0, to = nil, loop = false|
		"ahoj".postln;
	}


}
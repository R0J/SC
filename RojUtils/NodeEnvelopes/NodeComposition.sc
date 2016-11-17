NodeComposition {
	classvar <library;
	classvar currentStage;

	*initLibrary {
		if(library.isNil) {
			library = MultiLevelIdentityDictionary.new();
			("NodePlayer library init...").postln;
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

	*addBus {|node, controlName|
		var path = [node.envirKey.asSymbol, \buses, controlName.asSymbol];
		if(library.atPath(path).isNil)
		{
			library.putAtPath(path, Bus.control(Server.default, 1));
		};
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

	*stage {|stageName, fadeTime = 0|
		this.initLibrary;
		currentStage = stageName;
	}

	*play {|from = 0, to = nil, loop = false|
		"ahoj".postln;
	}


}
+ NodeProxy {

	env { |controlName, envelopeName = nil|
		var nEnv;
		NodeComposition.addNode(this);

		if(controlName.notNil)
		{
			if(envelopeName.isNil,
				{ nEnv = NodeEnv(this.envirKey, controlName.asSymbol, \default); },
				{ nEnv = NodeEnv(this.envirKey, controlName.asSymbol, envelopeName.asSymbol); }
			);
			NodeComposition.addEnvelope(nEnv);
		};
		^nEnv;
	}

	cycle { |cycleName = nil|
		var nCycle;
		if(cycleName.isNil,
			{ nCycle = NodeCycle(this.envirKey, \default); },
			{ nCycle = NodeCycle(this.envirKey, cycleName.asSymbol); }
		);
		NodeComposition.addCycle(nCycle);
		^nCycle;
	}

	stage { |stageName = nil|
		var nStage;
		if(stageName.isNil,
			{ nStage = NodeStage(this.envirKey, \default); },
			{ nStage = NodeStage(this.envirKey, stageName.asSymbol); }
		);
		NodeComposition.addStage(nStage);
		^nStage;
	}

	envMap {|key ...envDefKeys|

		envDefKeys.do({|key|
			if(EnvDef.exist(key))
			{
				buses.add(EnvDef(key).bus);
			};
		});
		buses.postln;
		// this.map(key.asSymbol, BusPlug.for(EnvDef(envDefKeys[0].asSymbol).bus));
		// this.map(key.asSymbol, In.ar([3,4]).asControlInput();
		this.nodeMap.set(key.asSymbol,
		}

	}
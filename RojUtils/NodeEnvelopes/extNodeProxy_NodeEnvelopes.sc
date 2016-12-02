+ NodeProxy {

	env { |envelopeName, envelope = nil, duration = nil|
		^EnvDef.newForNode(this, envelopeName, envelope, duration);
	}

	mapEnv { |controlName ... envDefKeys|
		envDefKeys.do({|oneArg|
			if(EnvDef.exist(oneArg, this))
			{ this.env(oneArg).map(this.envirKey, controlName); }
			{ "EnvDef ('%') in NodeProxy('%') not found".format(oneArg, this.envirKey).warn; }
		});
	}

	cycle { |cycleName, quant ... args|
		^CycleDef.newForNode(this, cycleName, quant, args);
		/*
		var name = "%_%".format(this.envirKey, cycleName).asSymbol;

		if(CycleDef.exist(name))
		{
			if(quant.notNil)
			{ CycleDef.newForNode(this, cycleName, quant, args); };
			{ ^CycleDef(name); }
		}
		{ ^CycleDef.newForNode(this, cycleName, quant, args); }
		*/
	}

	stage { |stageName = nil|
		if(stageName.notNil)
		{
			if(StageDef.exist(stageName))
			{ ^StageDef(stageName); }
			{ ^StageDef(stageName); }
		}
	}

}
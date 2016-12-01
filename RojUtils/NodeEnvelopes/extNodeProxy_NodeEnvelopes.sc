+ NodeProxy {

	env { |envelopeName, envelope = nil, duration = nil|
		var def;
		if(envelope.notNil)
		{ def = EnvDef.newForNode(this, envelopeName, envelope, duration); }
		{
			if(EnvDef.exist(envelopeName, this))
			{ def = EnvDef.newForNode(this, envelopeName); }
			{ def = nil; }
		}
		^def;
	}

	mapEnv { |controlName ... envDefKeys|

		envDefKeys.do({|oneArg|
			"%_%".format(this.envirKey, oneArg).postln;
			if(EnvDef.exist(oneArg, this))
			{
				EnvDef.get(oneArg, this).map(this.envirKey, oneArg);
			}
			{ "EnvDef ('%') not found".format(oneArg).warn; }
		});
	}

	cycle { |cycleName, quant ... args|

		var name = "%_%".format(this.envirKey, cycleName).asSymbol;

		if(CycleDef.exist(name))
		{
			if(quant.notNil)
			{ CycleDef.newForNode(this, cycleName, quant, args); };
			{ ^CycleDef(name); }
		}
		{ ^CycleDef.newForNode(this, cycleName, quant, args); }
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
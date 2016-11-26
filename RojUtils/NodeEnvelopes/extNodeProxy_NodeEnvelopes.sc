+ NodeProxy {

	env { |controlName, envelopeName, envelope = nil, duration = nil|

		if(controlName.notNil && envelopeName.notNil)
		{
			// var name = "%_%_%".format(this.envirKey, controlName, envelopeName).asSymbol;
			var name = "%_%".format(this.envirKey, envelopeName).asSymbol;
			// name.postln;
			// EnvDef.exist(name).postln;
			if(EnvDef.exist(name))
			{
				if(envelope.notNil)
				{ ^EnvDef(name, envelope, duration).map(this.envirKey.asSymbol, controlName.asSymbol); }
				{ ^EnvDef(name); }
			}
			{ if(envelope.notNil) { ^EnvDef(name, envelope, duration).map(this.envirKey.asSymbol, controlName.asSymbol); }}
		}
		^nil;
	}

	cycle { |cycleName = nil, quant = nil|

		if(cycleName.notNil)
		{
			// var name = "%_%".format(this.envirKey, cycleName).asSymbol;
			// ("cycleExist:" + CycleDef.exist(name)).postln;
			if(CycleDef.exist(cycleName))
			{ ^CycleDef(cycleName); }
			{ ^CycleDef(cycleName, quant).node(this.envirKey); }
		}
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
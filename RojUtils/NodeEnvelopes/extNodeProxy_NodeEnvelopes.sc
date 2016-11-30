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
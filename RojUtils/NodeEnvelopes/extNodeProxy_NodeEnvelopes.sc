+ NodeProxy {

	env { |envelopeName, envelope = nil, duration = nil|
		^EnvDef.newForNode(this, envelopeName, envelope, duration);
	}

	mapEnv { |controlName ... envDefKeys|
		envDefKeys.do({|oneArg|
			if(EnvDef.exist(oneArg, this))
			{ this.env(oneArg).map(this.envirKey, controlName); }
			// { EnvDef.get(oneArg, this.envirKey).map(this.envirKey, controlName); }
			{ "EnvDef ('%') in NodeProxy('%') not found".format(oneArg, this.envirKey).warn; }
		});
	}

	cycle { |cycleName, quant ... args| ^CycleDef.newForNode(this, cycleName, quant, args); }

}


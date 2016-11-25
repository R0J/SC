+ NodeProxy {

	env { |controlName, envelopeName, envelope = nil, duration = nil|
		if(controlName.notNil && envelopeName.notNil)
		{
			var name = "%_%_%".format(this.envirKey, controlName, envelopeName).asSymbol;
			// name.postln;
			// EnvDef.exist(name).postln;
			if(EnvDef.exist(name))
			{ ^EnvDef(name); }
			{ if(envelope.notNil) { ^EnvDef(name, envelope, duration).map(this.envirKey.asSymbol, controlName.asSymbol); }}
		}
		^nil;
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

}
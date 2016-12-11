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

	trig { |...args|
		var nMap = this.nodeMap;
		// args.postln;
		// nMap.postln;

		args.pairsDo({|controlName, sDef|
			var bus;
			var keyExist = false;
			nMap.mappingKeys.do({|nodeKey|
				if(nodeKey.asSymbol == controlName.asSymbol) { keyExist = true };
			});

			if(keyExist.not)
			{ bus = Bus.control(Server.default, 1) }
			{ bus = nMap.get(controlName.asSymbol) };
			// ("bus:" + bus).postln;
			this.set(controlName.asSymbol, BusPlug.for(bus));
			sDef.bus = bus;
		});
	}
}


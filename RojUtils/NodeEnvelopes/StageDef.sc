StageDef {
	var <key;
	var <group;
	var <bus;
	var <duration;
	var <timeline;

	classvar <>all;

	*initClass { all = IdentityDictionary.new; }

	*new { |key, item, dur = nil|
		var def = this.all.at(key);
		if(def.isNil)
		{ def = super.new.init.prStore(key, item, dur); }
		{ if(item.notNil) {	def.prStore(key, item, dur); }};
		^def;
	}

	cmdPeriod {
		{
			group = Group.new( RootNode (Server.default))
			// ("CmdPeriod protection" + this).warn;
		}.defer(0.01);
	}

	init {
		CmdPeriod.add(this);
		bus = Bus.control(Server.default, 1);
		group = Group.new( RootNode (Server.default));
		// group.onFree({ "Stage % end".format(key).postln; });
		timeline = Timeline.new();
	}

	free {
		group.free;
		bus.free;
		CmdPeriod.remove(this);
		all.removeAt(key);
	}

	times {  |cycleDefKey ... times|
		if(CycleDef.exist(cycleDefKey))
		{
			timeline.removeKeys(cycleDefKey);
			times.do({|oneTime|

				timeline.put(oneTime, CycleDef(cycleDefKey.asSymbol), CycleDef(cycleDefKey.asSymbol).cycleQuant, cycleDefKey);
				// if(oneTime + CycleDef(cycleDefKey.asSymbol).cycleQuant > duration)
				// {
				// "% at % is longer than % quant".format(CycleDef(cycleDefKey.asSymbol), oneTime, this).warn;
			// };
			});
		}
		{ "EnvDef ('%') not found".format(cycleDefKey).warn; }
	}

	trig { |startTime = 0|
		// |targetGroup, targetBus|
		timeline.play({|item| item.trig(0, group); });
	}

	prStore { |itemKey, item, dur|
		key = itemKey;
		all.put(itemKey, this);
	}

	printOn { |stream|
		stream << this.class.name << "('" << key << " | id:" << group.nodeID << "')";
	}

}
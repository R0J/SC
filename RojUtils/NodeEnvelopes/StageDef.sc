StageDef {
	var <key;
	var <group;
	var <bus;
	var <duration;

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
	}

	free {
		group.free;
		bus.free;
		CmdPeriod.remove(this);
		all.removeAt(key);
	}

	prStore { |itemKey, item, dur|
		key = itemKey;
		all.put(itemKey, this);
	}

	printOn { |stream|
		stream << this.class.name << "('" << key << " | id:" << group.nodeID << "')";
	}

}
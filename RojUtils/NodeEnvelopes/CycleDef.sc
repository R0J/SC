CycleDef {
	var <key;
	var <group;
	var <cycleQuant;
	var <timeline;

	var <env;

	var parentNode;

	classvar <>all;

	*initClass { all = IdentityDictionary.new; }

	*new { |key, quant ... args| ^this.newForNode(nil, key, quant, args); }

	*newForNode { |node, key, quant ... args|
		var def = this.all.at(key.asSymbol);
		if(def.isNil)
		{ def = super.new.init(node, key, quant, args); }
		{ if(quant.notNil) { def.init(node, key, quant, args); } } ;
		^def;
	}

	*exist { |key| if(this.all.at(key.asSymbol).notNil) { ^true; } { ^false; } }

	*print { this.all.sortedKeysValuesDo({|cycleName, oneCycle| oneCycle.postln; }) }

	cmdPeriod {
		{
			group = Group.new( RootNode (Server.default))
		}.defer(0.01);
	}

	init { |node, itemKey, itemQuant, args|
		// CmdPeriod.add(this);
		// bus = Bus.control(Server.default, 1);
		// group = Group.new( RootNode (Server.default));
		var currentEnvDef = nil;
		var isValidSymbol = false;

		parentNode = node;
		timeline = Timeline.new();
		env = nil;

		key = itemKey;
		if(itemQuant.notNil) { this.quant(itemQuant); };
		all.put(itemKey, this);

		args.flatten.do({|oneArg|
			if(oneArg.isKindOf(Symbol))
			{
				if(parentNode.notNil) { oneArg = "%_%".format(parentNode.envirKey, oneArg).asSymbol; };

				if(EnvDef.exist(oneArg))
				{
					currentEnvDef = oneArg;
					isValidSymbol = true;
				}
				{
					isValidSymbol = false;
					"EnvDef ('%') not found".format(oneArg).warn;
				}
			}
			{
				if(isValidSymbol) { timeline.put(oneArg, EnvDef(currentEnvDef), EnvDef(currentEnvDef).duration, currentEnvDef); }
			}
		});
	}

	free {
		group.free;
		// bus.free;
		CmdPeriod.remove(this);
		all.removeAt(key);
	}

	removeEnv {|envKey| timeline.removeKeys(envKey); }

	quant {|qnt|
		cycleQuant = qnt;
		// timeline.setEnd(qnt);
	}

	duration { ^timeline.duration; }

	trig { |startTime = 0, targetGroup = nil, clock = nil|
		if(clock.isNil) { clock = currentEnvironment.clock; };
		if(group.notNil) { group.free; };
		if(targetGroup.isNil)
		{ group = Group.new( RootNode (Server.default) ); }
		{ group = Group.new( targetGroup ); };

		"% trig time: %".format(this, clock.beats).postln;
		// timeline.array.postln;
		// |targetGroup, targetBus|

		timeline.items({|time, duration, item, key|
			if(item.isKindOf(EnvDef))
			{
				// "\nCycle % :".format(item).postln;
				// "at % to % -> key: % || %".format(time, (time + duration), key, item).postln;
				// if(clock.beats >= time)
				// {
				clock.sched(time, { item.trig(0, group, clock); nil;});
				// };
				// clock.schedAbs(time, { item.postln; nil;});
			};
		});


		/*
		timeline.play({|item|
		// "% from: %".format(item, item.from).postln;
		item.trig(0, group);

		}, startTime);
		*/
		currentEnvironment.clock.sched(timeline.duration + 5, {
			group.free;
			// bus.free;
			group = nil;
			nil;
		});
	}

	plot {|size = 400|
		var plotName = "CycleDef_" ++ key;
		var windows = Window.allWindows;
		var plotWin = nil;
		var envList = List.new();
		var plotter;

		windows.do({|oneW|
			("oneW.name:" + oneW.name).postln;
			if(plotName.asSymbol == oneW.name.asSymbol) { plotWin = oneW; };
		});

		if(plotWin.isNil, {
			// plotter = envList.asArray.plot(name:plotName.asSymbol);
			// plotter.parent.alwaysOnTop_(true);
		},{
			// plotWin.view.children[0].close;
			// plotter = Plotter(plotName.asSymbol, parent:plotWin);
			// plotter.value = envList.asArray;
		});
		// plotter.domainSpecs = [[0, cycleQuant, 0, 0, "", " s"]];
		// plotter.refresh;
		"neni dopsano".warn;
	}

	printOn { |stream|
		stream << this.class.name << "('" << key << "' | qnt: " << cycleQuant << " | dur: " << this.duration << ")";
	}

}
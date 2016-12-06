CycleDef {
	var <key;
	var <group;
	var <>quant;
	var <timeline;
	var parentNode;

	classvar <>all;

	*initClass { all = MultiLevelIdentityDictionary.new; }

	*new { |key, quant ... args| ^this.newForNode(nil, key, quant, args); }

	*newForNode { |node, key, qnt ... args|
		var def;
		if(this.exist(key, node))
		{ def = this.get(key, node); }
		{ def = nil; };

		if(def.isNil)
		{ def = super.new.init(node, key, qnt, args); }
		{ if(qnt.notNil) { def.init(node, key, qnt, args); } } ;
		^def;
	}

	*get { |key, node = nil|
		var path;
		if(node.isNil)
		{ path = [\default, key.asSymbol]; }
		{ path = [node.envirKey.asSymbol, key.asSymbol] };
		^this.all.atPath(path);
	}

	*exist { |key, node = nil| if(this.get(key, node).notNil) { ^true; } { ^false; } }

	*update {
		this.all.leafDo({ |path|
			var cycleDef = this.all.atPath(path);
			cycleDef.timeline.timeBars.do({|bar| bar.duration_(bar.item.duration) });
		});
		StageDef.update;
	}

	*print {
		this.all.dictionary.sortedKeysValuesDo({ |key, oneNode|
			"key: %".format(key).postln;
			oneNode.sortedKeysValuesDo({ |controlName, envDef|
				"\t- controlName: %, one: %".format(controlName, envDef).postln;
			});
		});
	}

	cmdPeriod {
		{ group = Group.new( RootNode (Server.default)) }.defer(0.01);
	}

	init { |node, itemKey, qnt, args|
		// CmdPeriod.add(this);
		// bus = Bus.control(Server.default, 1);
		// group = Group.new( RootNode (Server.default));
		var currentEnvDef = nil;
		var isValidSymbol = false;

		parentNode = node;
		timeline = Timeline.new();

		key = itemKey;
		if(qnt.notNil) { quant = qnt; };

		if(node.isNil)
		{ all.putAtPath([\default, itemKey.asSymbol], this); }
		{ all.putAtPath([node.envirKey.asSymbol, itemKey.asSymbol], this); };

		args.flatten.do({|oneArg|
			if(oneArg.isKindOf(Symbol))
			{
				if(EnvDef.exist(oneArg, node))
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
				if(isValidSymbol) { timeline.put(oneArg, EnvDef.get(currentEnvDef, node), EnvDef.get(currentEnvDef,node).duration, currentEnvDef);}
			}
		});
		StageDef.update;
	}

	free {
		group.free;
		// bus.free;
		CmdPeriod.remove(this);
		all.removeAt(key);
	}

	removeEnv {|envKey| timeline.removeKeys(envKey); }

	duration { ^timeline.duration; }

	trig { |startTime = 0, endTime = nil, parentGroup = nil, clock = nil, multBus = nil|
		if(clock.isNil) { clock = currentEnvironment.clock; };
		// if(group.notNil) { group.free; };
		if(parentGroup.isNil)
		{ group = Group.new( RootNode (Server.default) ); }
		{
			// group = Group.new( targetGroup );
			group = parentGroup ;
		};

		// "% trig time: %".format(this, clock.beats).postln;
		"% trig time: %".format(this, currentEnvironment.clock.beats).postln;

		// |targetGroup, targetBus|

		timeline.items({|time, duration, item, key|
			if(item.isKindOf(EnvDef))
			{
				case
				{ startTime <= time } { clock.sched((time - startTime), { item.trig(0, nil, group, clock, multBus); nil;}); }
				{ (startTime > time) && (startTime < (time + duration)) } {
					"play from middle %".format(item).warn;
					item.trig(startTime, nil, group, clock, multBus);
				};
				// "\nCycle % :".format(item).postln;
				// "at % to % -> key: % || %".format(time, (time + duration), key, item).postln;


			};
		});
		 /*
		currentEnvironment.clock.sched(timeline.duration + 5, {
			group.free;
			// bus.free;
			group = nil;
			nil;
		});
		*/
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
		stream << this.class.name << "('" << key << "' | qnt: " << quant << " | dur: " << this.duration << ")";
	}

}
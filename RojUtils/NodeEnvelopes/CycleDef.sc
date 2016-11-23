CycleDef {
	var <key;
	var <group;
	var <bus;
	var <cycleQuant;
	var <timeline;

	classvar <>all;

	*initClass { all = IdentityDictionary.new; }

	*new { |key, quant = nil|
		var def = this.all.at(key);
		if(def.isNil)
		{ def = super.new.init.prStore(key, quant); }
		{ def.prStore(key, quant); };
		^def;
	}

	cmdPeriod {
		{
			group = Group.new( RootNode (Server.default))
		}.defer(0.01);
	}

	init {
		CmdPeriod.add(this);
		bus = Bus.control(Server.default, 1);
		group = Group.new( RootNode (Server.default));
		timeline = Timeline.new();
	}

	free {
		group.free;
		bus.free;
		CmdPeriod.remove(this);
		all.removeAt(key);
	}

	quant {|qnt|
		cycleQuant = qnt;
		timeline.setEnd(qnt);
	}

	envTrigs {  |envDefKey ... times|
		if(EnvDef.exist(envDefKey))
		{
			// "at % -> %".format(time, envDefsKeys).postln;
			timeline.removeKeys(envDefKey);
			times.do({|oneTime|
				timeline.put(oneTime, EnvDef(envDefKey.asSymbol), EnvDef(envDefKey.asSymbol).duration, envDefKey);
				if(oneTime + EnvDef(envDefKey.asSymbol).duration > cycleQuant)
					{
						"% at % is longer than % quant".format(EnvDef(envDefKey.asSymbol), oneTime, this).warn;
					};
			});
			}
			{ "EnvDef ('%') not found".format(envDefKey).warn; }
		}

		trig { |startTime = 0|
			// |targetGroup, targetBus|
			timeline.play({|item| item.trig(0, group); });
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

		prStore { |itemKey, itemQuant|
			key = itemKey;
			if(itemQuant.notNil) { this.quant(itemQuant); };
			all.put(itemKey, this);
		}

		printOn { |stream|
			stream << this.class.name << "('" << key << "' | qnt: " << cycleQuant << ")";
		}

	}
StageDef {
	var <key;
	var <group;
	var <bus;
	var <timeline;
	var <nodes;
	var <quant;

	classvar <>all;

	*initClass { all = IdentityDictionary.new; }

	*new { |key, qnt ... cycleArgs|
		var def;
		if(this.exist(key))
		{ def = this.all.at(key); }
		{ def = nil; };

		if(def.isNil)
		{ def = super.new.init(key).times(qnt, cycleArgs); }
		{ if(cycleArgs.notEmpty) { def.times(qnt, cycleArgs); } } ;
		^def;
	}

	*exist { |key| if(this.all.at(key.asSymbol).notNil) { ^true; } { ^false; } }

	*update {
		this.all.keysDo({ |key|
			var stageDef = this.all.at(key.asSymbol);
			stageDef.timeline.timeBars.do({|bar| bar.duration_(bar.item.duration) });
		});
	}

	*print { this.all.sortedKeysValuesDo({|stageName, oneStage| oneStage.postln; }) }

	cmdPeriod {
		{
			group = Group.new( RootNode (Server.default))
			// ("CmdPeriod protection" + this).warn;
		}.defer(0.01);
	}

	init { |stageKey|
		CmdPeriod.add(this);
		bus = Bus.control(Server.default, 1);
		// group = Group.new( RootNode (Server.default));
		// group.onFree({ "Stage % end".format(key).postln; });
		timeline = Timeline.new();
		nodes = List.new();

		key = stageKey;
		// nodeLibrary = nodeNames;

		all.put(stageKey.asSymbol, this);
	}

	free {
		// group.free;
		bus.free;
		CmdPeriod.remove(this);
		all.removeAt(key);
	}

	playNodes {

	}

	stopNodes {

	}

	duration { ^timeline.duration; }

	times { |qnt, cycleDefKey|

		var currentNodeProxy = nil;
		var currentTime = 0;

		// var isValidSymbol = false;
		// "newTimes".warn;
		timeline = Timeline.new();
		nodes = List.new();
		quant = qnt;

		cycleDefKey.do({|oneArg|
			// oneArg.class.postln;
			case
			{ oneArg.isKindOf(NodeProxy) } {
				// "NodeProxy ('%') found".format(oneArg.envirKey).postln;
				nodes.add(oneArg);
				currentNodeProxy = oneArg;
				currentTime = 0;
			}
			{ oneArg.isKindOf(Pattern) } {
				// "Pattern ('%') found".format(oneArg).postln;
				// oneArg.asStream.all.postln;
				oneArg.asStream.all.do({|oneCycleKey|
					if(CycleDef.exist(oneCycleKey, currentNodeProxy))
					{
						var cycleDef = CycleDef.get(oneCycleKey, currentNodeProxy);
						// "CycleDef ('%') found".format(oneCycleKey).postln;
						timeline.put( currentTime, cycleDef, cycleDef.duration, oneCycleKey);

						if(cycleDef.quant.notNil)
						{ currentTime = currentTime + cycleDef.quant; }
						{ currentTime = currentTime + cycleDef.duration; }
					}
					{ "CycleDef ('%') not found".format(oneCycleKey).warn; }
				});
			}
			{ oneArg.isKindOf(Symbol) } {
				if(CycleDef.exist(oneArg, currentNodeProxy))
				{
					// "CycleDef ('%') found".format(oneArg).postln;
					timeline.put(currentTime, CycleDef.get(oneArg, currentNodeProxy), CycleDef.get(oneArg, currentNodeProxy).duration, oneArg);
				}
				{ "CycleDef ('%') not found".format(oneArg).warn; }
			}
			{ oneArg.isKindOf(Integer) } { currentTime = oneArg; }
			{ oneArg.isKindOf(Number) } { currentTime = oneArg; };
		});
	}

	trig { |startTime = 0, parentGroup = nil, clock = nil, multBus = nil|
		if(clock.isNil) { clock = currentEnvironment.clock; };
		// nodeLibrary.postln;
		// "% trig time: %".format(this, clock.beats).postln;

		"% trig time: %".format(this, currentEnvironment.clock.beats).postln;

		if(parentGroup.isNil)
		{ group = Group.new( RootNode (Server.default) ); }
		{
			// group = Group.new( targetGroup );
			group = parentGroup;
		};


		timeline.items({|time, duration, item, key|
			if(item.isKindOf(CycleDef))
			{
				// "\nCycle % :".format(item).postln;
				// "at % to % -> key: % || %".format(time, (time + duration), key, item).postln;

				clock.sched(time, { item.trig(0, nil, group, clock, multBus); nil;});
			};
		});
	}

	printOn { |stream|
		stream << this.class.name << "('" << key << "' | qnt:" << this.quant << " | dur:" << this.duration /*<< " | id:" << group.nodeID*/ << ")";
	}

}
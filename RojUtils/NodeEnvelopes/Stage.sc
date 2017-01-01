Stage {
	var <key;
	var <>quant;
	var <group;
	var <timeline;
	var <references;

	var <volLibrary;
	var <busLibrary;

	classvar <>all;
	classvar controlRate;
	classvar isServerBooted;


	*initClass {
		all = IdentityDictionary.new;
		controlRate = 44100 / 64;
		isServerBooted = false;
	}

	*new { |key, qnt = nil|
		var def;
		if(this.exist(key))
		{ def = this.all.at(key) }
		{ def = super.new.init(key) };

		if(qnt.notNil) {def.quant = qnt };

		^def;
	}

	*exist { |key| if(this.all.at(key.asSymbol).notNil) { ^true; } { ^false; } }

	init { |stageKey|

		Server.default.waitForBoot({
			isServerBooted = true;
			controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			// CmdPeriod.add(this);
			// bus = Bus.control(Server.default, 1);

			key = stageKey;
			quant = 1;
			timeline = nil;
			group = Group.new( RootNode (Server.default));
			group.onFree({ "Stage % end".format(key).postln; });
			references = Set.new;
			volLibrary = IdentityDictionary.new;
			busLibrary = IdentityDictionary.new;

			all.put(stageKey.asSymbol, this);
		});
	}

	addRef { |target| references.add(target); }
	update {
		// references.do({|oneRef|
		// oneRef.prSetSignal(oneRef.setOrder);
		// })
		"Stage UPDATE".warn;
	}

	setNode {|node, volume ...pairControlSignal|
		var nMap = node.nodeMap;

		volLibrary.put(node, volume);
		node.play(group:group, vol:volume);
		node.asTarget.moveToTail(group);

		pairControlSignal.pairsDo({ |controlName, sDef|
			var bus;
			var keyExist = false;
			nMap.mappingKeys.do({|nodeKey|
				if(nodeKey.asSymbol == controlName.asSymbol) { keyExist = true };
			});

			if(keyExist.not)
			{ bus = Bus.control(Server.default, 1) }
			{ bus = nMap.get(controlName.asSymbol).bus };
			// ("bus:" + bus).postln;
			node.set(controlName.asSymbol, BusPlug.for(bus));
			busLibrary.put(sDef, bus);
		});
	}

	times {|...cycleDefKey|
		timeline = Timeline2.new();
		// cycleDefKey.postln;

		cycleDefKey.pairsDo({|time, item|
			// item.class.postln;
			case
			{ item.isKindOf(Sdef) }
			{
				timeline.put(time, item, item.duration);
				item.addRef(this);
			}
		});
	}

	duration { ^timeline.duration; }

	trig { |startTime = 0, parentGroup = nil, clock = nil, multBus = nil|
		if(clock.isNil) { clock = currentEnvironment.clock; };

		"% trig time: %".format(this, currentEnvironment.clock.beats).postln;

		// this.playNodes;

		timeline.items({|time, duration, item|
			if(item.isKindOf(Sdef))
			{
				clock.sched(time, {
					item.trig(busLibrary.at(item), startTime, nil, group);
					nil;
				});
			};
		});
	}

	playNodes {
		volLibrary.keysValuesDo({|node, volume|
			node.play(group:group, vol:volume);
			node.asTarget.moveToTail(group);
		})
	}

	stopNodes {
		volLibrary.keysValuesDo({|node, volume|
			node.free;
		})
	}

	printOn { |stream|
		if(isServerBooted)
		{ stream << this.class.name << "('" << key << "' | qnt:" << this.quant << " | id:" << group.nodeID << ")" }
		{ stream << this.class.name << "('" << key << "' | qnt:" << this.quant << " | id: nil)" }
	}

}
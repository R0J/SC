Stage {
	var <key;
	var <>quant;
	var <group;
	var <timeline;

	classvar <>all;
	classvar controlRate;
	classvar isServerBooted;
	classvar hasInitSynthDefs;
	classvar bufferSynthDef;


	*initClass {
		all = IdentityDictionary.new;
		controlRate = 44100 / 64;
		isServerBooted = false;
		hasInitSynthDefs = false;
	}

	*new { |key, qnt = nil|
		var def;
		if(hasInitSynthDefs.not) { this.initSynthDefs };

		if(this.exist(key))
		{ def = this.all.at(key) }
		{ def = super.new.init(key) };

		if(qnt.notNil) {def.quant = qnt };

		^def;
	}

	*exist { |key| if(this.all.at(key.asSymbol).notNil) { ^true; } { ^false; } }

	*initSynthDefs{
		Server.default.waitForBoot({
			bufferSynthDef = { |bus, bufnum, startTime = 0|
				var buf = PlayBuf.kr(
					numChannels: 1,
					bufnum: bufnum,
					startPos: startTime * controlRate,
					rate: \tempoClock.kr(1),
					loop: 0
				);
				FreeSelfWhenDone.kr(buf);
				// Out.kr(cBus,buf * In.kr(multBus));
				// Out.kr(cBus,buf * \multiplicationBus.kr(1));
				Out.kr(bus,buf);
			}.asSynthDef;

			hasInitSynthDefs = true;
			isServerBooted = true;
		});
	}

	init { |stageKey|

		// CmdPeriod.add(this);
		// bus = Bus.control(Server.default, 1);
		// nodes = List.new();
		// nodeLibrary = nodeNames;

		key = stageKey;
		quant = 1;
		timeline = nil;
		group = Group.new( RootNode (Server.default));
		group.onFree({ "Stage % end".format(key).postln; });

		all.put(stageKey.asSymbol, this);
	}

	times {|...cycleDefKey|
		timeline = Timeline2.new();
		cycleDefKey.postln;

		cycleDefKey.pairsDo({|time, item|
		item.class.postln;
			case
			{ item.isKindOf(Sdef) }
			{
				timeline.put(time, item, item.duration);
			}
			{ item.isKindOf(NodeProxy) }
			{
				timeline.put(time, item, 0);
			}
		});
	}

	duration { ^timeline.duration; }

	trig { |startTime = 0, parentGroup = nil, clock = nil, multBus = nil|
		if(clock.isNil) { clock = currentEnvironment.clock; };
		// nodeLibrary.postln;
		// "% trig time: %".format(this, clock.beats).postln;

		"% trig time: %".format(this, currentEnvironment.clock.beats).postln;

		timeline.items({|time, duration, item|
			if(item.isKindOf(Sdef))
			{
				clock.sched(time, {
					// item.trig(0, nil, group, clock, multBus);
					var buffer = item.buffer;
					var bus = item.bus;
					var name = "Sdef(%)".format(item.path2txt);

					if(buffer.notNil)
					{
						var synth;
						var group = RootNode(Server.default);
						if(parentGroup.notNil) { group = parentGroup; };
						bufferSynthDef.name_("Sdef(%)".format(item.path2txt));
						synth =	bufferSynthDef.play(
							target: group,
							args:
							[
								\bus: bus,
								\bufnum: buffer.bufnum,
								\startTime, startTime,
								\tempoClock, currentEnvironment.clock.tempo,
								// \multBus, multBus.index
								// \multiplicationBus, multBus.asMap
							]
						);
						// synth.set(\multiplicationBus, multBus);

						// if(endTime.notNil)
						// {
							// clock.sched((endTime - startTime), { synth.free; nil; });
					// }
					}
					{ "% buffer not found".format(this).warn; };
					nil;
				});
			};
		});
	}

	printOn { |stream|
		if(isServerBooted)
		{ stream << this.class.name << "('" << key << "' | qnt:" << this.quant << " | id:" << group.nodeID << ")" }
		{ stream << this.class.name << "('" << key << "' | qnt:" << this.quant << " | id: nil)" }
	}

}
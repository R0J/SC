Composition {

	// classvar currentStage;

	// classvar clock;
	// classvar <timeline;

	classvar playingGroup;

	classvar hasInitSynthDefs;
	classvar groupMultSynthDef;

	var <group;
	var <groupMultBus, <fadeSynth;
	var <currentStage;
	var fadeOutTask;
	var clock;

	*initClass {
		hasInitSynthDefs = false;
		playingGroup = IdentityDictionary.new;
	}

	*initSynthDefs {
		groupMultSynthDef = { |bus, target, time|
			var fadeGen = EnvGen.kr(
				envelope: Env([ In.kr(bus), [target]], time, \lin),
				timeScale: currentEnvironment.clock.tempo.reciprocal,
				doneAction: 2
			);
			ReplaceOut.kr(bus, fadeGen);
		}.asSynthDef;
		hasInitSynthDefs = true;
		"Composition SynthDefs loaded".postln;
	}

	*playStage {|stageName, quant, fadeTime = 0|

		if(hasInitSynthDefs.not) { this.initSynthDefs; };
		if(StageDef.exist(stageName))
		{
			var instance = super.new;
			var envirClock = currentEnvironment.clock;
			var time2quant = envirClock.timeToNextBeat(quant);
			envirClock.sched( time2quant, {
				this.stop(fadeTime);
				instance.initGroup(stageName, fadeTime);
				instance.prPlay(stageName);
				nil;
			})
			^instance;
		}
		{ "StageDef ('%') not found".format(stageName).warn; }
		^nil;
	}

	*stop { |fadeTime = 0|
		"Composition class stop".warn;
		playingGroup.keysValuesDo({|nodeID, oldInstance|
			oldInstance.removeGroup(nodeID, fadeTime);
		});
	}

	initGroup { |stageName, fadeTime = 0|

		"NEW groupMultBus".warn;
		currentStage = StageDef(stageName.asSymbol);
		group = Group.new( RootNode (Server.default));
		groupMultBus = Bus.control(Server.default, 1);
		fadeOutTask = nil;

		this.fadeIn(fadeTime);

		playingGroup.put(group.nodeID.asSymbol, this);
	}

	removeGroup {|nodeID, fadeTime = 0|

		this.fadeOut(fadeTime);

		currentEnvironment.clock.sched(fadeTime, {
			"End of stop %".format(this).warn;
			clock.stop;
			clock = nil;
			nil;
		});
		currentEnvironment.clock.sched((fadeTime + 5), {
			currentStage.nodes.do({|oneNode| oneNode.free; });
			group.free;
			groupMultBus.free;
			playingGroup.removeAt(nodeID.asSymbol);
			"FREE of stop %".format(this).warn;
			nil;
		});
	}

	setGroupMultiplicationFactor {|targetValue, fadeTime = 0|
		if(hasInitSynthDefs)
		{
			if(fadeSynth.isPlaying) { fadeSynth.free; };

			groupMultSynthDef.name_("ComposionFade");
			fadeSynth =	groupMultSynthDef.play(
				target: group,
				args:
				[
					\bus: groupMultBus.index,
					\target: targetValue,
					\time, fadeTime
				]
			);
			fadeSynth.register;
		}
		{"Composition SynthDefs not load yet".warn} ;
	}
	fadeIn {|time|
		this.setGroupMultiplicationFactor(1,time);
		"Composition fadeIn ('%')".format(this).warn;
	}
	fadeOut {|time|
		this.setGroupMultiplicationFactor(0,time);
		"Composition fadeOut ('%')".format(this).warn;
	}

	prPlay {|stageName|

		if(fadeOutTask.isNil) {currentStage.nodes.do({|oneNode| oneNode.play; });};

		// if(clock.notNil) { this.stop; };
		clock = TempoClock.new(
			tempo: currentEnvironment.clock.tempo,
			beats: 0
		);

		if(currentStage.quant.notNil)
		{ clock.sched((currentStage.quant), { this.prPlay(stageName); nil; }); }
		{ clock.sched((currentStage.duration), { this.prPlay(stageName); nil; }); };

		clock.sched(0, { currentStage.trig(0, group, clock, groupMultBus); nil; });

	}

	printOn { |stream|
		if(group.notNil)
		{ stream << this.class.name << " (id:" << group.nodeID << ")"; }
		{ stream << this.class.name; }
	}

	/*
	*play {|startTime = 0, endTime = nil, loop = false|

	if(clock.notNil) { clock.stop; };
	clock = TempoClock.new(
	tempo: 127/60,
	beats: startTime
	);

	clock.schedAbs(startTime, {"Composition time tick: %".format(clock.beats).postln; 1 });
	clock.schedAbs(endTime, {
	"Composition end time now: %".format(clock.beats).postln;
	this.stop;
	if(loop) { this.play(startTime, endTime, loop); };
	nil;
	});

	timeline.items({|time, duration, item, key|
	if(item.isKindOf(StageDef))
	{
	"\nStage % :".format(item).postln;
	"at % to % -> key: % || %".format(time, (time + duration), key, item).postln;
	clock.schedAbs(time, { item.trig(0, clock); nil});
	};

	if(item.isKindOf(CycleDef))
	{
	"\nCycle % :".format(item).postln;
	"at % to % -> key: % || %".format(time, (time + duration), key, item).postln;
	};
	});
	*/
	/*
	timeline.schedToClock(clock, {|time, duration, item, key|
	"at % to % -> key: % || %".format(time, (time + duration), key, item).postln;
	});
	*/
	/*
	timeline.array.do({|bar|
	var time = bar[0];
	var item = bar[1];
	var duration = bar[2];
	var key = bar[3];
	"at % to % -> key: % || %".format(time, (time + duration), key, item).postln;
	case
	{ key.asSymbol == \nodePlay } {  clock.schedAbs(time, { item.play; nil; }); }
	{ key.asSymbol == \nodeStop } {  clock.schedAbs(time, { item.stop; nil; }); }
	{ key.asSymbol == \stageTrig } {  clock.schedAbs(time, { item.trig; nil; }); };
	});
	*/

}
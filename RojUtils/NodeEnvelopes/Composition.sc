Composition {

	classvar currentStage;

	// classvar clock;
	classvar <timeline;

	classvar playingGroup;
	// classvar isPlaying;

	var <group;
	var fadeOutTask;
	var clock;

	*test {|stageName, quant, fadeTime = 0|
		if(StageDef.exist(stageName))
		{
			var instance = super.new;
			var envirClock = currentEnvironment.clock;
			var time2quant = envirClock.timeToNextBeat(quant);
			instance.initGroup(fadeTime);
			envirClock.sched( time2quant, {
				instance.currentStagePlay(stageName);
				nil;
			})
			^instance;
		}
		{ ^nil; }
	}

	*initClass {
		playingGroup = IdentityDictionary.new;

		timeline = Timeline.new();
		// clock = nil;
		currentStage = \default;
		// isPlaying = false;
	}


	*stop {|fadeTime = 0|
		playingGroup.keysValuesDo({|nodeID, oldInstance|
			oldInstance.removeGroup(nodeID, fadeTime);
		});
	}

	initGroup { |fadeTime|
		group = Group.new( RootNode (Server.default));
		fadeOutTask = nil;

		playingGroup.keysValuesDo({|nodeID, oldInstance|
			oldInstance.removeGroup(nodeID, fadeTime);
		});
		playingGroup.put(group.nodeID.asSymbol, this);
	}

	removeGroup {|nodeID, time = 0|
		if(fadeOutTask.notNil) { fadeOutTask.stop; };
		fadeOutTask = {
			currentStage.nodes.do({|oneNode| oneNode.stop(time); });
			time.wait;
			clock.stop;
			clock = nil;
			group.free;
			playingGroup.removeAt(nodeID.asSymbol);
		}.fork;
	}

	currentStagePlay {|stageName|
		currentStage = StageDef(stageName.asSymbol);
currentStage.nodes.do({|oneNode| oneNode.play; });

		if(clock.notNil) { this.stop; };
		clock = TempoClock.new(
			tempo: currentEnvironment.clock.tempo,
			beats: 0
		);

		if(currentStage.quant.notNil)
		{ clock.sched((currentStage.quant), { this.currentStagePlay(stageName); nil; }); }
		{ clock.sched((currentStage.duration), { this.currentStagePlay(stageName); nil; }); };

		clock.sched(0, { currentStage.trig(0, group, clock); nil; });

	}
/*
	*playStage {|stageName, quant|
		if(StageDef.exist(stageName))
		{
			var envirClock = currentEnvironment.clock;
			var time2quant = 0;

			currentStage = StageDef(stageName.asSymbol);
			if(clock.isNil) { time2quant = envirClock.timeToNextBeat(quant); };

			if(clock.notNil) { this.stop; };
			clock = TempoClock.new(
				tempo: envirClock.tempo,
				beats: 0
			);

			if(currentStage.quant.notNil)
			{ clock.sched((currentStage.quant + time2quant), { this.playStage(stageName); nil; }); }
			{ clock.sched((currentStage.duration + time2quant), { this.playStage(stageName); nil; }); };

			clock.sched(time2quant, { currentStage.trig(0, clock); nil; });
			// clock.sched(0, {"% time tick: %".format(currentStage, clock.beats).postln; 1 });
			// });
		}
		{ "StageDef ('%') not found".format(stageName).warn; }
	}

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

/*
	}
	*stop {
		clock.stop;
		clock = nil;

	}
*/



	*putNode { |nodeName, from, to|
		var node = nodeName.asSymbol.envirGet;
		timeline.put(from, node, (to-from), \nodePlay);
		timeline.put(to, node, 0, \nodeStop);
	}

	*putStage {|stageName, from, to|
		timeline.put(from, StageDef(stageName.asSymbol), (to-from), \stageTrig);
	}

	*putCycle {|cycleName, from, to|
		timeline.put(from, CycleDef(cycleName.asSymbol), (to-from), \cycleTrig);
	}


}
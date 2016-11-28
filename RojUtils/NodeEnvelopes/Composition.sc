Composition {

	classvar currentStage;

	classvar clock;
	classvar <timeline;

	*initClass {
		timeline = Timeline.new();
		currentStage = \default;
		clock = nil;
	}

	*playStage {|stageName|
		if(StageDef.exist(stageName))
		{
			currentStage = StageDef(stageName.asSymbol);

			if(clock.notNil) { this.stop; };
			clock = TempoClock.new(
				tempo: 127/60,
				beats: 0
			);
			clock.sched(8, { this.playStage(stageName); nil; });
			clock.sched(0, {"% time tick: %".format(currentStage, clock.beats).postln; 1 });
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

	*stop {
		clock.stop;
		clock = nil;
	}

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
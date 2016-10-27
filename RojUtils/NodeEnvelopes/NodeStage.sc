NodeStage {

	var nodeName, stageName, library;
	var clock;
	var <timeline;

	var loopTask;
	var >loopCount;
	var >loopTime;

	*new {|nodeName, stageName = \default|
		var lib = nodeName.envirGet.nodeMap.get(\qMachine);
		var path = [\stages, stageName.asSymbol];
		var nStage = lib.atPath(path);

		if(nStage.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, stageName.asSymbol, lib).init(path); },
			{ ^nStage; }
		);
	}

	init {|path|
		clock = nil;
		timeline = Timeline.new();
		loopTask = nil;
		loopCount = 1;
		loopTime = 1;
		library.putAtPath(path, this);
	}


	add {|time, cycleName|
		var path = [\cycles, cycleName.asSymbol];
		var nCycle = library.atPath(path);

		if(nCycle.isNil,
			{ ("NodeCycle [\\" ++ cycleName ++ "] not found in map").warn;  ^nil; },
			{ this.schedCycle(time, nCycle); }
		);
	}

	schedCycle {|time, nodeCycle| timeline.put(time, nodeCycle, nodeCycle.duration, nodeCycle.cycleName); }

	cleanTimeline {
		loopTask.stop;
		clock.stop;
		timeline = Timeline.new();
	}

	duration { ^timeline.duration; }

	play { |loops = 1|
		loopCount = loops;
		// if(loopTask.no[tNil) {  };
		// ("loopTask.isNil" + loopTask.isNil).postln;
		// ("loopTask" + loopTask).postln;

		loopTask = Task({
			currentEnvironment.clock.timeToNextBeat(loopTime).wait;
			loopCount.do({
				if(clock.notNil) { clock.stop; };
				clock = TempoClock.new(currentEnvironment.clock.tempo);

				timeline.times.do({|oneTime|
					timeline.get(oneTime).asArray.do({|item|
						clock.sched(oneTime, { item.trig; } );
					});
				});
				// clock.sched(timeline.duration, { clock.stop; });
				("loopCount:" + loopCount).postln;
				loopCount = loopCount + 1;
				timeline.duration.wait;
			});
		}).play;
	}

	printOn { |stream| stream << this.class.name << " [\\"  << stageName << ", dur:" << this.duration << "]" }
}
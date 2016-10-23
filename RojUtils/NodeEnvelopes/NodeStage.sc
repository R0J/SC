NodeStage {

	var nodeName, stageName;
	var clock;
	var timeline;

	var loopTask;
	var >loopCount;
	var >loopTime;

	*new {|nodeName, stageName|
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var path = [\stages, stageName.asSymbol];
		var nStage = library.atPath(path);

		if(nStage.isNil,
			{
				("NodeStage" + stageName + "novy").postln;
				^super.newCopyArgs(nodeName.asSymbol, stageName.asSymbol).init;
			}, {
				("NodeStage" + stageName + "nalezen").postln;
				^nStage;
			}
		);
	}

	init {
		clock = nil;
		timeline = Order.new();
		loopTask = nil;
		loopCount = 1;
		loopTime = 1;
		this.storeToMap;
	}

	schedCycle {|time, nodeCycle| timeline.put(time, nodeCycle); }

	cleanTimeline {
		loopTask.stop;
		clock.stop;
		timeline = Order.new();
	}

	play {
		// if(loopTask.no[tNil) {  };
		// ("loopTask.isNil" + loopTask.isNil).postln;
		// ("loopTask" + loopTask).postln;
		loopTask = Task({
			currentEnvironment.clock.timeToNextBeat(loopTime).wait;
			loopCount.do({
				if(clock.notNil) { clock.stop; };
				clock = TempoClock.new(currentEnvironment.clock.tempo);

				timeline.keysValuesDo ({|time|
					var selectedNodeCycle = timeline[time];
					// ("timeLine time:" + time).postln;
					// ("timeLine selectedNodeCycle:" + selectedNodeCycle).postln;

					clock.sched(time, {	selectedNodeCycle.trig;	});
					clock.sched(loopTime, { clock.stop; })
				});
				loopTime.wait;
			});
		}).play;
	}

	printOn { |stream|
		stream << this.class.name << "[" << nodeName << "; " << stageName << "]";
	}

	storeToMap {
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		// var stage = \default;
		var path = [\stages, stageName.asSymbol];

		library.putAtPath(path, this);
		library.postTree;
	}
}
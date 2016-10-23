NodeCycle {

	var nodeName, cycleName;

	var clock;
	var timeline;
	var <duration;

	*new {|nodeName, cycleName|
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var path = [\cycles, cycleName.asSymbol];
		var nCycle = library.atPath(path);

		if(nCycle.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, cycleName.asSymbol).init; },
			{
				nCycle.cleanSched;
				^nCycle;
			}
		);
	}

	init {
		clock = nil;
		timeline = Order.new();
		this.storeToMap;
	}

	schedEnv {|time, nodeEnv|
		timeline.put(time, nodeEnv);
		duration = timeline.lastIndex + timeline[timeline.lastIndex].duration;
		this.storeToMap;
	}

	cleanSched {
		timeline = Order.new();
		duration = 0;
		this.storeToMap;
	}

	trigTimes {	^timeline.indices; }
	trigEnvs { ^timeline.array; }
	trigAt {|time| ^timeline[time]; }
	trigEndTime {|time| ^time + timeline[time].duration; }

	trig {
		if(clock.notNil) { clock.stop; };
		clock = TempoClock.new(currentEnvironment.clock.tempo);

		timeline.keysValuesDo ({|time|
			var selectedNodeEnv = timeline[time];
			// ("timeLine time:" + time).postln;
			// ("timeLine selectedNodeEnv:" + selectedNodeEnv).postln;

			clock.sched(time, { selectedNodeEnv.trig; });
		});

		clock.sched(duration, { clock.stop; });
	}

	printOn { |stream|
		stream << this.class.name << "[" << cycleName << ", " << timeline << "]";
	}

	storeToMap {
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var path = [\cycles, cycleName.asSymbol];
		library.putAtPath(path, this);
	}
}
NodeCycle {

	var nodeName, cycleName;

	var clock;
	var controlTimeline;
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
		// timeline = Order.new();
		controlTimeline = IdentityDictionary.new();
		this.storeToMap;
	}

	schedEnv {|time, nodeEnv|

		var controlName = nodeEnv.controlName.asSymbol;
		var timeline;
		if(controlTimeline.at(controlName).isNil) {
			// ("schedEnv -> nodeEnv.controlName:" + nodeEnv.controlName).warn;
			controlTimeline.put(controlName, Order.new());
		};
		timeline = controlTimeline.at(controlName);
		// ("timeline -> " + timeline).warn;
		timeline.put(time, nodeEnv);
		// duration = timeline.lastIndex + timeline[timeline.lastIndex].duration;
		this.storeToMap;
	}

	cleanSched {
		controlTimeline = IdentityDictionary.new();
		// timeline = Order.new();
		duration = 0;
		this.storeToMap;
	}

	storedControlNames { ^controlTimeline.keys.asArray; }

	trigTimes { |controlName|
		var timeline = controlTimeline.at(controlName.asSymbol);
		if(timeline.notNil, { ^timeline.indices; }, { ^nil; });
	}
	trigEnvs {|controlName|
		var timeline = controlTimeline.at(controlName.asSymbol);
		if(timeline.notNil, { ^timeline.array; }, { ^nil; });
	}
	trigAt {|controlName, time|
		var timeline = controlTimeline.at(controlName.asSymbol);
		if(timeline.notNil, { ^timeline[time]; }, { ^nil; });
	}
	trigEndTime {|controlName, time|
		var timeline = controlTimeline.at(controlName.asSymbol);
		if(timeline.notNil, { ^time + timeline[time].duration; }, { ^nil; });
	}

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
		stream << this.class.name << "[" << cycleName << ", " << this.storedControlNames << "]";
	}

	storeToMap {
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var path = [\cycles, cycleName.asSymbol];
		library.putAtPath(path, this);
	}
}
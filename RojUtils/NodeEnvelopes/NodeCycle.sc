NodeCycle {

	var nodeName, cycleName, library;

	var clock;
	var <timeline;
	var library;

	*new {|nodeName, cycleName = \default|
		var lib = nodeName.envirGet.nodeMap.get(\qMachine);
		var path = [\cycles, cycleName.asSymbol];
		var nCycle = lib.atPath(path);

		if(nCycle.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, cycleName.asSymbol, lib).init(path); },
			{ ^nCycle; }
		);
	}

	init { |path|
		clock = nil;
		timeline = Timeline.new();
		library.putAtPath(path, this);
	}

	addEnv {|time, controlName, envName|
		var path = [\envelopes, controlName.asSymbol, envName.asSymbol];
		var nEnv = library.atPath(path);

		if(nEnv.isNil,
			{ ("NodeEnv [\\" ++ controlName ++ "\\" ++ envName ++ "] not found in map").warn;  ^nil; },
			{ this.schedEnv(time, nEnv); }
		);
	}

	schedEnv {|time, nodeEnv|
		timeline.put(time, nodeEnv, nodeEnv.duration, nodeEnv.envName);
	}

	cleanSched {
		timeline = Timeline.new();
	}

	duration { ^timeline.duration; }

	trig {
		if(clock.notNil) { clock.stop; };
		clock = TempoClock.new(currentEnvironment.clock.tempo);

		timeline.times.do({|oneTime|
			timeline.get(oneTime).asArray.do({|item|
				clock.sched(oneTime, { item.trig; } );
			});
		});
		clock.sched(timeline.duration, { clock.stop; });
	}

	printOn { |stream|
		stream << this.class.name << " [\\" << cycleName << ", dur:" << this.duration << "]";
	}


}
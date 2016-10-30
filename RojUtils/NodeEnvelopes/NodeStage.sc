NodeStage {

	var nodeName, stageName, library;
	var <timeline;

	var clock;
	var loopTask;
	var >loopCount;

	*new {|nodeName, stageName = \default|
		var lib = Library.at(\qMachine);
		var path = [nodeName.asSymbol, \stages, stageName.asSymbol];
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
		library.putAtPath(path, this);
	}


	set {|time, cyclePattern|
		var stream = cyclePattern.asStream;
		var currentTrigTime = time;

		timeline = Timeline.new();
		loopCount = 1;

		case
		{ stream.isKindOf(Routine) } { stream = stream.all; } // Pseq([\aaa, \bbb], 3) ++ \ccc
		{ stream.isKindOf(Symbol) }	{ stream = stream.asArray; }
		{ stream.isKindOf(Integer) } { stream = stream.asSymbol.asArray; }
		{ stream.isKindOf(String) }	{ stream = stream.asSymbol.asArray; }
		;
		("stageName:" + stageName + "; stream:" + stream).postln;

		// remove old keys
		stream.do({|oneCycleName|
			timeline.times.do({|oneTime|
				timeline.take(oneTime, oneCycleName);
			});
		});

		// add new keys
		stream.do({|oneCycleName|
			var oneCyclePath = [nodeName.asSymbol, \cycles, oneCycleName.asSymbol];
			var oneCycle = library.atPath(oneCyclePath);

			this.schedCycle(currentTrigTime, oneCycle);
			currentTrigTime = currentTrigTime + oneCycle.duration;
		});
	}

	schedCycle {|time, nodeCycle| timeline.put(time, nodeCycle, nodeCycle.duration, nodeCycle.cycleName); }

	duration { ^timeline.duration; }

	play { |loops = inf|

		if(loopTask.notNil) { this.stop; };
		if(timeline.duration > 0)
		{
			loopTask = Task({
				currentEnvironment.clock.timeToNextBeat(timeline.duration).wait;
				loops.do({
					clock = TempoClock.new(currentEnvironment.clock.tempo);

					timeline.times.do({|oneTime|
						timeline.get(oneTime).asArray.do({|oneCycle|
							clock.sched(oneTime, { oneCycle.trig; } );
						});
					});
					// clock.sched(timeline.duration, { clock.stop; });
					("loopCount:" + loopCount).postln;
					loopCount = loopCount + 1;
					timeline.duration.wait;
				});
			}).play;
		};
	}

	stop {
		loopTask.stop;
		clock.stop;
		clock = nil;
		loopTask = nil;
		loopCount = 1;
	}

	printOn { |stream| stream << this.class.name << " [\\"  << stageName << ", dur:" << this.duration << "]" }
}
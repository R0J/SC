NodeCycle {

	var nodeName, <cycleName, library;

	var clock;
	var <timeline;

	*new {|nodeName, cycleName = \default|
		var lib = Library.at(\qMachine);
		var path = [nodeName.asSymbol, \cycles, cycleName.asSymbol];
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
		clock = TempoClock.new(currentEnvironment.clock.tempo);
	}

	set {|controlName, envPattern, time = 0|
		var path = [nodeName.asSymbol, \envelopes, controlName.asSymbol];
		var nEnv = library.atPath(path);
		var stream = envPattern.asStream;
		var currentTrigTime = time;

		if(nEnv.isNil,
			// { ("NodeEnv [\\" ++ nodeName ++ "\\" ++ controlName ++ "\\" ++ envPattern ++ "] not found in map").warn;  ^nil; },
			{ ("NodeEnv [\\" ++ controlName ++ "\\" ++ envPattern ++ "] not found in map").warn;  ^nil; },
			{
				case
				{ stream.isKindOf(Routine) } { stream = stream.all; } // Pseq([\aaa, \bbb], 3) ++ \ccc
				{ stream.isKindOf(Symbol) }	{ stream = stream.asArray; }
				{ stream.isKindOf(Integer) } { stream = stream.asSymbol.asArray; }
				{ stream.isKindOf(String) }	{ stream = stream.asSymbol.asArray; }
				;
				// ("controlName:" + controlName + "; stream:" + stream).postln;

				// remove old keys
				stream.do({|oneEnvelopeName|
					timeline.times.do({|oneTime|
						// ("oneTime, oneEnvelopeName:" + [oneTime, oneEnvelopeName]).postln;
						timeline.take(oneTime, oneEnvelopeName);
					});
				});

				// add new keys
				stream.do({|oneEnvelopeName|
					var oneEnvPath = [nodeName.asSymbol, \envelopes, controlName.asSymbol, oneEnvelopeName.asSymbol];
					var oneEnv = library.atPath(oneEnvPath);

					this.schedEnv(currentTrigTime, oneEnv);
					currentTrigTime = currentTrigTime + oneEnv.duration;
				});
			}
		);

	}

	schedEnv {|time, nodeEnv|
		timeline.put(time, nodeEnv, nodeEnv.duration, nodeEnv.envelopeName);
	}

	duration { ^timeline.duration; }

	trig {|targetGroup, targetBus|
		// if(clock.notNil) { clock.stop; };
		// clock = TempoClock.new(currentEnvironment.clock.tempo);
clock.beats = 0;
		timeline.times.do({|oneTime|
			timeline.get(oneTime).asArray.do({|oneEnv|
				clock.sched(oneTime, { oneEnv.trig(targetGroup, targetBus); } );
			});
		});
		// clock.sched(timeline.duration, { clock.stop; });
	}

	printOn { |stream|
		stream << this.class.name << " [\\" << cycleName << ", dur:" << this.duration << "]";
	}


}
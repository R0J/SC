NodeCycle {

	var <nodeName, <cycleName;
	var <envelopePattern;
	var clock;
	var <timeline;

	*new {|nodeName, cycleName = \default|
		var nCycle = NodeComposition.getCycle(nodeName, cycleName);

		if(nCycle.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, cycleName.asSymbol).init; },
			{ ^nCycle; }
		);
	}

	init {
		envelopePattern = IdentityDictionary.new;
		clock = TempoClock.new(currentEnvironment.clock.tempo);
		timeline = Timeline.new();
	}

	set {|controlName, envPattern, time = 0|

		var stream = envPattern.asStream;
		var currentTrigTime = time;

		// if(nEnv.isNil,
		// { ("NodeEnv [\\" ++ nodeName ++ "\\" ++ controlName ++ "\\" ++ envPattern ++ "] not found in map").warn;  ^nil; },
		// { ("NodeEnv [\\" ++ controlName ++ "\\" ++ envPattern ++ "] not found in map").warn;  ^nil; },
		// {
		case
		{ stream.isKindOf(Routine) } { envelopePattern.put(controlName.asSymbol, stream.all); } // Pseq([\aaa, \bbb], 3) ++ \ccc
		{ stream.isKindOf(Symbol) }	{ envelopePattern.put(controlName.asSymbol, stream.asArray); }
		{ stream.isKindOf(Integer) } { envelopePattern.put(controlName.asSymbol, stream.asSymbol.asArray); }
		{ stream.isKindOf(String) }	{ envelopePattern.put(controlName.asSymbol, stream.asSymbol.asArray); }
		;

		// ("controlName:" + controlName + "; stream:" + stream).postln;

		// remove old keys
		envelopePattern.at(controlName.asSymbol).do({|oneEnvelopeName|
			timeline.times.do({|oneTime|
				// ("oneTime, oneEnvelopeName:" + [oneTime, oneEnvelopeName]).postln;
				timeline.take(oneTime, oneEnvelopeName);
			});
		});

		// add new keys
		envelopePattern.at(controlName.asSymbol).do({|oneEnvelopeName|
			var oneEnv = NodeComposition.getEnvelope(nodeName, controlName, oneEnvelopeName);
			if(oneEnv.isNil,
				{ ("NodeEnv [\\" ++ controlName ++ "\\" ++ envPattern ++ "] not found in map").warn;  ^nil; },
				{
					timeline.put(currentTrigTime, oneEnv, oneEnv.duration, oneEnv.envelopeName);
					currentTrigTime = currentTrigTime + oneEnv.duration;
				};
			);
		});
		// }
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
NodeStage {

	classvar <currentStage = \default;

	var nodeName, stageName, library;
	var <timeline;

	var stageGroup;
	var stageMultBus;
	var stageMultSynth, fadeSynthName;

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

	*current {|stage| currentStage = stage;	}

	init {|path|
		stageGroup = Group.new(nodeName.envirGet.group);
		stageMultBus = BusPlug.control(Server.default, 1);

		fadeSynthName = stageName ++ "_fade";

		clock = nil;
		timeline = Timeline.new();
		loopTask = nil;
		loopCount = 1;
		library.putAtPath(path, this);

		this.prepareSynthDef;
	}

	isCurrentStage { if((currentStage == stageName), { ^true; }, { ^false; }); }

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

	setFactor {|targetValue, fadeTime = 0|
		// var fadeSynthName = stageName ++ "_fade";
		if(stageMultSynth.isPlaying) { stageMultSynth.free;	};

		stageMultSynth = Synth(fadeSynthName.asSymbol, [
			\bus: stageMultBus.index,
			\target: targetValue,
			\time, fadeTime
		], target: stageGroup);
		stageMultSynth.register;
	}

	fadeIn {|time| this.setFactor(1,time); }
	fadeOut {|time| this.setFactor(0,time); }

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
							clock.sched(oneTime, { oneCycle.trig(stageGroup, stageMultBus); } );
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

	prepareSynthDef {
		var envSynthDef = { |bus, target, time|
			var fadeGen = EnvGen.kr(
				envelope: Env([ In.kr(bus), [target]], time, \sin),
				timeScale: currentEnvironment.clock.tempo.reciprocal,
				doneAction: 2
			);
			ReplaceOut.kr(bus, fadeGen);
		};
		envSynthDef.asSynthDef(name:fadeSynthName.asSymbol).add;
		("SynthDef" + fadeSynthName + "added").postln;
	}
}
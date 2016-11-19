NodeStage {

	classvar <currentStage = \default;

	var <nodeName, <stageName;
	var <cyclePattern;
	var <timeline;

	var stageGroup;
	var stageMultBus;
	var stageMultSynth, fadeSynthName;

	var loopTask;
	var >loopCount;

	*new {|nodeName, stageName = \default|
		var nStage = NodeComposition.getStage(nodeName, stageName);

		if(nStage.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, stageName.asSymbol).init; },
			{ ^nStage; }
		);
	}

	init {
		stageGroup = Group.new(nodeName.envirGet.group);
		stageMultBus = BusPlug.control(Server.default, 1);

		fadeSynthName = stageName ++ "_fade";

		timeline = Timeline.new();
		loopTask = nil;
		loopCount = 1;

		this.prepareSynthDef;

		CmdPeriod.add(this);
	}

	cmdPeriod {
		"cmdPeriod stage".warn;
		("stageGroup.nodeID:" + stageGroup.nodeID).postln;
		stageGroup = Group.new(nodeName.envirGet.group);
	}

	isCurrentStage { if((currentStage == stageName), { ^true; }, { ^false; }); }

	set {|pattern, time = 0|
		var stream = pattern.asStream;
		var currentTrigTime = time;

		timeline = Timeline.new();
		loopCount = 1;

		case
		{ stream.isKindOf(Routine) } { cyclePattern = stream.all; } // Pseq([\aaa, \bbb], 3) ++ \ccc
		{ stream.isKindOf(Symbol) }	{ cyclePattern = stream.asArray; }
		{ stream.isKindOf(Integer) } { cyclePattern = stream.asSymbol.asArray; }
		{ stream.isKindOf(String) }	{ cyclePattern = stream.asSymbol.asArray; }
		;
		("stageName:" + stageName + "; stream:" + stream).postln;

		// remove old keys
		cyclePattern.do({|oneCycleName|
			timeline.removeKeys(oneCycleName);
		});

		// add new keys
		cyclePattern.do({|oneCycleName|
			var oneCycle = NodeComposition.getCycle(nodeName, oneCycleName);
			if(oneCycle.isNil,
				{ ("NodeCycle [\\" ++ oneCycleName ++ "] not found in map").warn;  ^nil; },
				{
					this.schedCycle(currentTrigTime, oneCycle);
					currentTrigTime = currentTrigTime + oneCycle.duration;
				}
			);
		});
	}

	setFactor {|targetValue, fadeTime = 0|
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

		var node = NodeComposition.getNode(nodeName);

		if(loopTask.notNil) { this.stop; };
		if(timeline.duration > 0)
		{
			loopTask = Task({
				loops.do({

					timeline.play({|item| item.trig(stageGroup, stageMultBus); });

					("stageName:" + stageName + "; loopCount:" + loopCount).postln;
					loopCount = loopCount + 1;
					timeline.duration.wait;
				});
			}).play;
		};
	}

	trig {

	}

	stop {|releaseTime = 0|
		Task({
			releaseTime.wait;
			loopTask.stop;
			loopTask = nil;
			loopCount = 1;
		}).play;
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
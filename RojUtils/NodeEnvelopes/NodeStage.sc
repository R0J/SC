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
		("nodeName:" + nodeName).postln;
		// ("stageGroup.nodeID:" + stageGroup.nodeID).postln;
		stageGroup.free;
		stageGroup = nil;
	}

	isCurrentStage { if((currentStage == stageName), { ^true; }, { ^false; }); }

	set {|cycleName, trigTimes = 0|
		var nCycle = NodeComposition.getCycle(nodeName, cycleName);

		if(nCycle.isNil,
			{ ("NodeCycle [\\" ++ cycleName ++ "] not found in map").warn;  ^this; },
			{
				timeline.removeKeys(cycleName);
				trigTimes.asArray.do({|oneTime|
					timeline.put(oneTime, nCycle, nCycle.duration, nCycle.cycleName);
				});
			}
		);
	}

	removeCycle { |cycleName| timeline.removeKeys(cycleName); }

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

	duration { ^timeline.duration; }

	play { |loops = inf|

		// var node = NodeComposition.getNode(nodeName);
		// node.play;

		if(loopTask.notNil) { this.stop; };
		if(timeline.duration > 0)
		{
			loopTask = Task({
				loops.do({
					var node = NodeComposition.getNode(nodeName);
					if(node.isNil) { node.play; };
					if(stageGroup.isNil) { stageGroup = Group.new(RootNode.new(Server.default)); };
					timeline.play({|item| item.trig(stageGroup, stageMultBus); });

					loopCount = loopCount + 1;
					timeline.duration.wait;
				});
			}).play;
		};
	}

	trig { timeline.play({|item| item.trig(stageGroup, stageMultBus); }); }

	stop {|releaseTime = 0|
		var node = NodeComposition.getNode(nodeName);
		Task({
			releaseTime.wait;
			stageGroup.free;
			node.free;
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

	/*
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
	// ("stageName:" + stageName + "; stream:" + stream).postln;

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
	timeline.put(time, oneCycle, oneCycle.cycleQuant, oneCycle.cycleName);
	currentTrigTime = currentTrigTime + oneCycle.cycleQuant;
	}
	);
	});
	}
	*/
}
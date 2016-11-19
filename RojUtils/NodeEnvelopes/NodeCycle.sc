NodeCycle {

	var <nodeName, <cycleName;
	var <cycleQuant;
	var <envelopePattern;
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
		timeline = Timeline.new();
		cycleQuant = nil;
	}

	quant {|quant| cycleQuant = quant; }

	set {|controlName, envPattern, time = 0|

		var stream = envPattern.asStream;
		var currentTrigTime = time;

		case
		{ stream.isKindOf(Routine) } { envelopePattern.put(controlName.asSymbol, stream.all); } // Pseq([\aaa, \bbb], 3) ++ \ccc
		{ stream.isKindOf(Symbol) }	{ envelopePattern.put(controlName.asSymbol, stream.asArray); }
		{ stream.isKindOf(Integer) } { envelopePattern.put(controlName.asSymbol, stream.asSymbol.asArray); }
		{ stream.isKindOf(String) }	{ envelopePattern.put(controlName.asSymbol, stream.asSymbol.asArray); }
		;

		// ("controlName:" + controlName + "; stream:" + stream).postln;

		// remove old keys
		envelopePattern.at(controlName.asSymbol).do({|oneEnvelopeName|
			timeline.removeKeys(oneEnvelopeName);
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
	}

	duration { ^timeline.duration; }

	trig {|targetGroup, targetBus|
		var timeToQuant = 0;
		if(cycleQuant.notNil) { timeToQuant = currentEnvironment.clock.timeToNextBeat(cycleQuant); };
		// ("NodeCycle.trig.timeToQuant:" + timeToQuant).postln;
		// ("NodeCycle.trig.currentEnvironment.clock.beats:" + currentEnvironment.clock.beats).postln;

		timeline.play({|item| item.trig(targetGroup, targetBus);})
	}

	printOn { |stream|
		stream << this.class.name << " [\\" << cycleName << ", qnt:" << this.cycleQuant << ", dur:" << this.duration << "]";
	}

	plot {|size = 400|
		var plotName = nodeName ++ "_" ++ cycleName;
		var windows = Window.allWindows;
		var plotWin = nil;
		var envList = List.new();
		var plotter;

		windows.do({|oneW|
			// ("oneW.name:" + oneW.name).postln;
			if(plotName.asSymbol == oneW.name.asSymbol) { plotWin = oneW; };
		});

		envelopePattern.sortedKeysValuesDo({|oneControlName|
			var controlEnvelopeStream = nil;
			// ("oneControlName:" + oneControlName).postln;
			envelopePattern.at(oneControlName.asSymbol).do({|oneEnvelopeName|
				var oneEnv = NodeComposition.getEnvelope(nodeName, oneControlName, oneEnvelopeName);
				if((controlEnvelopeStream.isNil),
					{
						controlEnvelopeStream = oneEnv.envelope;
					},{
						controlEnvelopeStream = controlEnvelopeStream.connect(oneEnv.envelope);
					}
				);
			});
			envList.add(controlEnvelopeStream.asSignal(size));
		});

		if(plotWin.isNil, {
			plotter = envList.asArray.plot(name:plotName.asSymbol);
			plotter.parent.alwaysOnTop_(true);
		},{
			plotWin.view.children[0].close;
			plotter = Plotter(plotName.asSymbol, parent:plotWin);
			plotter.value = envList.asArray;
		});
		plotter.domainSpecs = [[0, this.duration, 0, 0, "", " s"]];
		plotter.refresh;
	}


}
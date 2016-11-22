NodeCycle {

	var <nodeName, <cycleName;
	var <cycleQuant;
	// var <envelopePattern;
	var <timeline;

	*new {|nodeName, cycleName = \default|
		var nCycle = NodeComposition.getCycle(nodeName, cycleName);

		if(nCycle.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, cycleName.asSymbol).init; },
			{ ^nCycle; }
		);
	}

	init {
		// envelopePattern = IdentityDictionary.new;
		timeline = Timeline.new();
		cycleQuant = nil;
	}

	quant {|quant|
		cycleQuant = quant;
		timeline.removeKeys(\timeline_end);
		timeline.setEnd(quant);
	}

	set {|envelopeName, trigTimes = 0|
		var nEnv = NodeComposition.getEnvelope(nodeName, envelopeName);
		if(nEnv.isNil,
			{ ("NodeEnv [\\" ++ envelopeName ++ "] not found in map").warn;  ^this; },
			{
				timeline.removeKeys(envelopeName);
				trigTimes.asArray.do({|oneTime|
					timeline.put(oneTime, nEnv, nEnv.duration, nEnv.envelopeName);
				});
			}
		);
		// NodeComposition.updateTimes(nodeName, controlName);
	}

	removeEnvelope { |envelopeName|	timeline.removeKeys(envelopeName); }

	duration { ^timeline.duration; }

	trig {|targetGroup, targetBus| timeline.play({|item| item.trig(targetGroup, targetBus); });	}

	printOn { |stream|
		stream << this.class.name << " [\\" << cycleName << ", qnt:" << this.cycleQuant << ", dur:" << this.duration << "]";
	}

	//need to rewrite to timeline from envPattern
	/*
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
	*/

}
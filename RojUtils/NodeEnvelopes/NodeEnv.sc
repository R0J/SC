NodeEnv {

	var nodeName, <controlName, <envelopeName, library;

	var <>envelope;
	var synth, controlBusIndex;
	var <>setPlot = false;

	*new {|nodeName, controlName, envelopeName = \default|
		var lib = Library.at(\qMachine);
		var path = [nodeName.asSymbol, \envelopes, controlName.asSymbol, envelopeName.asSymbol];
		var nEnv = lib.atPath(path);

		if(nEnv.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, controlName.asSymbol, envelopeName.asSymbol, lib).init(path); },
			{ ^nEnv; }
		);
	}

	init { |path|
		var envSynthDef;
		var envSynthName = nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName;
		{
			envSynthDef = {|cBus|
				var envelope = EnvGen.kr(
					\env.kr(Env.newClear(200,1).asArray),
					gate: \envTrig.tr(0),
					timeScale: \tempoClock.kr(1).reciprocal,
					doneAction: 2
				);

				Out.kr( cBus, envelope * \multiplicationBus.kr(1));
			};
			envSynthDef.asSynthDef(name:envSynthName.asSymbol).add;
			("SynthDef" + envSynthName + "added").postln;

			Server.default.sync;
		}.fork;

		envelope = nil;
		synth = nil;

		library.putAtPath(path, this);
	}

	set {|env, fixDuration = nil|
		var node = library.atPath([nodeName.asSymbol, \node]);

		this.envelope = env;
		if(fixDuration.notNil) { this.fixDur(fixDuration); };

		controlBusIndex = library.atPath([nodeName.asSymbol, \buses, controlName.asSymbol]).index;
		node.set(controlName.asSymbol, BusPlug.for(controlBusIndex));

		if(this.setPlot) { this.plot; };
		^this;
	}

	remove {
		var lib = Library.at(\qMachine);
		var path = [nodeName.asSymbol, \envelopes, controlName.asSymbol, envelopeName.asSymbol];
		lib.removeEmptyAtPath(path);

		path = [nodeName.asSymbol, \envelopes, controlName.asSymbol];
		if(lib.atPath(path).isNil) {
			path = [nodeName.asSymbol, \buses, controlName.asSymbol];
			lib.atPath(path).free;
			lib.removeEmptyAtPath(path);
		};
		// lib.postTree;
		^nil;
	}

	duration {
		if(this.envelope.notNil,
			{ ^this.envelope.duration; },
			{ ^nil; }
		);
	}

	fixDur {|dur|
		if(this.envelope.notNil,
			{
				var envDur = this.duration;
				case
				{ dur < envDur } { this.set(this.envelope.crop(0, dur)); }
				{ dur.asSymbol == envDur.asSymbol } {  }
				{ dur > envDur } { this.set(this.envelope.extend(dur)); };

				if(this.setPlot) { this.plot; };
			},
			{ ("Envelope of" + nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName + "is not defined").warn; }
		);
		^this;
	}

	print { |cntTabs = 0|
		var txt = "";
		var tabs = "";
		cntTabs.do({tabs = tabs ++ "\t"});

		if(envelope.notNil, {
			txt = txt ++ tabs ++ "- levels:" + envelope.levels ++ "\n";
			txt = txt ++ tabs ++ "- times:" + envelope.times ++ "\n";
			txt = txt ++ tabs ++ "- curves:" + envelope.curves ++ "\n";
		}, {
			txt = tabs ++ "Env (nil)\n";
		});

		txt.postln;
	}

	printOn { |stream| stream << this.class.name << " [\\" << controlName << ", \\" << envelopeName << ", dur:" << this.duration << "]"; }

	plot {|size = 400|
		var plotName = nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName;
		if(envelope.notNil,
			{ envelope.plotNamedEnv(plotName.asSymbol, size); },
			{ ("Envelope of" + nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName + "is not defined").warn; }
		);
		^this;
	}

	trig {|targetGroup, targetBus|
		// ("Synth trig to controlBus_" ++ controlBusIndex + "by synth:" + synth.nodeID + "env:" + this.print(1)).postln;
		var envSynthName = nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName;
		var fTime = 0;

		synth = Synth(envSynthName.asSymbol, [
			\cBus: controlBusIndex,
			\env: [envelope],
			\envTrig, 1,
			\tempoClock, currentEnvironment.clock.tempo,
			\multiplicationBus, targetBus.asMap
		], targetGroup);
	}
}
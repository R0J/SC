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
		var node = library.atPath([nodeName.asSymbol, \node]);
		controlBusIndex = library.atPath([nodeName.asSymbol, \buses, controlName.asSymbol]).index;
		envelope = nil;
		synth = nil;

		node.set(controlName.asSymbol, BusPlug.for(controlBusIndex));
		this.prepareSynthDef;

		library.putAtPath(path, this);
	}

	set {|envName, env|
		var nEnv = super.class.new(nodeName, controlName, envName);
		nEnv.envelope = env;
		if(nEnv.setPlot) { nEnv.plot(envName); };
		^nEnv;
	}

	fixDur {|envName, dur|
		var nEnv = super.class.new(nodeName, controlName, envName);

		if(nEnv.envelope.notNil) {
			var envDur = nEnv.duration;
			case
			{ dur < envDur } { nEnv.set(envelopeName, nEnv.envelope.crop(0, dur)); }
			{ dur.asSymbol == envDur.asSymbol } {  }
			{ dur > envDur } { nEnv.set(envelopeName, nEnv.envelope.extend(dur)); };

			if(nEnv.setPlot) { nEnv.plot(envelopeName); };
		}
		^nEnv;
	}

	duration {
		if(envelope.notNil,
			{ ^envelope.duration; },
			{ ^nil; }
		);
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

	plot {|envName, size = 400|
		envelope.plotNamedEnv(envName.asSymbol, size);
		^this;
	}

	prepareSynthDef {
		var envSynthDef;
		var envSynthName = nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName;
		var fTime = 0;
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
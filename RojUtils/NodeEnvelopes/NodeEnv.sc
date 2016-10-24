NodeEnv {

	var nodeName, <controlName, <envName;
	var <envelope;

	var synth, controlBus;
	var >setPlot = false;

	*new {|nodeName, controlName, envName|
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var path = [\envelopes, controlName.asSymbol];
		var nEnv = library.atPath(path ++ envName.asSymbol);

		if(nEnv.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, controlName.asSymbol, envName).init; },
			{ ^nEnv; }
		);
	}

	init {
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);

		synth = nil;
		controlBus = library.at(\envelopes, controlName.asSymbol, \controlBus);
		node.set(controlName.asSymbol, controlBus.asMap);

		this.prepareSynthDef;

		this.storeToMap;
	}

	env {|env|
		envelope = env;
		if(setPlot) { this.plot; };

		this.post;
	}

	envCode { ^"Env ( " ++ envelope.levels ++ ", " ++ envelope.times ++ ", " ++ envelope.curves ++ " )"; }

	duration { ^envelope.duration; }

	printOn { |stream|
		stream << this.class.name << "[ \\" << controlName << ", " << this.envCode << " ]";
	}

	plot {|size| envelope.plotNamedEnv(envName.asSymbol); }

	prepareSynthDef {
		var envSynthDef;
		var envSynthName = nodeName ++ "_" ++ controlName ++ "_" ++ envName;
		var fTime = 0;

		envSynthDef = {|cBus|
			var envelope = EnvGen.kr(
				\env.kr(Env.newClear(200,1).asArray),
				gate: \envTrig.tr(0),
				timeScale: \tempoClock.kr(1).reciprocal,
				doneAction: 0
			);

			var fade = EnvGen.kr(
				Env([ \fromVal.kr(0), \toVal.kr(0)], \fadeTime.kr(fTime), \sin),
				gate:\fadeTrig.tr(0),
				timeScale: \tempoClock.kr(1).reciprocal
			);

			Out.kr( cBus, envelope * fade );
		};
		envSynthDef.asSynthDef(name:envSynthName.asSymbol).add;
		("SynthDef" + envSynthName + "added").postln;

		Server.default.sync;
		// this.prFadeOutSynths(control, envName, fTime);

		synth = Synth(envSynthName.asSymbol, [
			\cBus: controlBus,
			\env: [envelope],
			\fromVal, 0,
			\toVal, 1,
			\fadeTime, fTime,
			\fadeTrig, 1
		], nodeName.envirGet.group);
		// synthID = synth.nodeID;


		// library.putAtPath(path ++ \synths ++ synthID.asSymbol, synth);
		// });
	}

	trig {
		// synth.do({|selectedSynth| selectedSynth.set(\envTrig, 1, \tempoClock, currentEnvironment.clock.tempo); });
		// ("Synth trig to " + controlBus + "by synth:" + synth.nodeID + "env:" + this.envCode).postln;
		synth.set(
			\envTrig, 1,
			\tempoClock, currentEnvironment.clock.tempo,
			\env, [envelope]
		);
	}

	storeToMap {
		var node = nodeName.envirGet;
		var library = node.nodeMap.get(\qMachine);
		var path = [\envelopes, controlName.asSymbol];

		library.putAtPath(path ++ envName.asSymbol, this);
	}
}
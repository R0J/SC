NodeEnv {

	var nodeName, <controlName, library;
	var envelopes;

	var synth, controlBus;
	var >setPlot = false;

	*new {|nodeName, controlName|
		var lib = Library.at(\qMachine);
		var path = [nodeName.asSymbol, \envelopes, controlName.asSymbol];
		var nEnv = lib.atPath(path);

		if(nEnv.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, controlName.asSymbol, lib).init(path); },
			{ ^nEnv; }
		);
	}

	init { |path|
		var node = library.atPath([nodeName.asSymbol, \node]);
		envelopes = IdentityDictionary.new();
		synth = nil;
		controlBus = Bus.control(Server.default, 1);
		node.set(controlName.asSymbol, controlBus.asMap);

		this.prepareSynthDef;

		library.putAtPath(path, this);
	}

	envNames { ^envelopes.keys.asArray; }

	hasEnvName {|envName| if(envelopes.keys.asArray.indexOf(envName.asSymbol).notNil, { ^true; }, { ^false; }); }

	set {|envName, env|
		envelopes.put(envName.asSymbol, env);
		if(setPlot) { this.plot(envName); };
		library.postTree;
		^this;
	}

	get {|envName|
		if(this.hasEnvName(envName)) { ^envelopes.at(envName.asSymbol);	};
		^nil;
	}


	fixDur {|envName, dur|
		envName.asArray.do({|oneName|
			if(this.hasEnvName(oneName)) {
				var envDur = this.duration(oneName);

				if(envDur.notNil)
				{
					case
					{ dur < envDur } { this.set(oneName, envelopes[oneName].crop(0, dur)); }
					{ dur.asSymbol == envDur.asSymbol } {  }
					{ dur > envDur } { this.set(oneName, envelopes[oneName].extend(dur)); };

					if(setPlot) { this.plot(oneName); };
				};
			};
		});
		^this;
	}


	duration {|envName|
		envName.asArray.do({|oneName|
			if(this.hasEnvName(oneName)) { ^envelopes[oneName.asSymbol].duration; };
		});
		^nil;
	}


	// envCode {|key| ^"Env ( " ++ envelope.levels ++ ", " ++ envelope.times ++ ", " ++ envelope.curves ++ " )"; }

	printOn { |stream| stream << this.class.name << " [\\" << controlName << ", " << envelopes.keys.asArray << "]"; }

	plot {|envName, size = 400|
		if(this.hasEnvName(envName)) {
			envelopes[envName.asSymbol].plotNamedEnv(envName.asSymbol, size);
		};
		^this;
	}

	prepareSynthDef {
		var envSynthDef;
		var envSynthName = nodeName ++ "_" ++ controlName;
		var fTime = 0;
		{
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
				// \env: [envelope],
				\fromVal, 0,
				\toVal, 1,
				\fadeTime, fTime,
				\fadeTrig, 1
			], nodeName.envirGet.group);
			// synthID = synth.nodeID;
		}.fork;

		// library.putAtPath(path ++ \synths ++ synthID.asSymbol, synth);
		// });
	}

	trig {|envName|
		// synth.do({|selectedSynth| selectedSynth.set(\envTrig, 1, \tempoClock, currentEnvironment.clock.tempo); });
		// ("Synth trig to " + controlBus + "by synth:" + synth.nodeID + "env:" + this.envCode).postln;
		synth.set(
			\envTrig, 1,
			\tempoClock, currentEnvironment.clock.tempo,
			\env, [envelopes[envName.asSymbol]]
		);
	}
}
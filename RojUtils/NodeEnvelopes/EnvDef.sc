EnvDef {
	var <key;
	var <env;
	var <bus;
	var <duration;

	var <group;

	var <buffer;
	// var bufSynthName;

	classvar <>all;

	classvar hasInitSynthDefs;
	classvar bufferSynthDef, testSynthDef;

	*initClass {
		all = IdentityDictionary.new;
		hasInitSynthDefs = false;
	}

	*new { |key, item, dur = nil|
		var def = this.all.at(key);
		if(def.isNil)
		{ def = super.new.init.prStore(key, item, dur); }
		{ if(item.notNil) {	def.prStore(key, item, dur); }};
		^def;
	}

	init {
		Server.default.waitForBoot({

			bus = Bus.control(Server.default, 1);
			if(hasInitSynthDefs.not) { this.initSynthDefs; };

			/*
			SynthDef(this.synthName, { |cBus, bufnum, startTime = 0|
			var controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			var buf = PlayBuf.kr(
			numChannels: 1,
			bufnum: bufnum,
			startPos: startTime * controlRate,
			rate: \tempoClock.kr(1),
			loop: 0
			);
			FreeSelfWhenDone.kr(buf);
			Out.kr(cBus,buf * \multiplicationBus.kr(1));
			}).add;
			*/
		})
	}

	initSynthDefs{
		"initSynthDefs".warn;
		bufferSynthDef = { |cBus, bufnum, startTime = 0|
			var controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			var buf = PlayBuf.kr(
				numChannels: 1,
				bufnum: bufnum,
				startPos: startTime * controlRate,
				rate: \tempoClock.kr(1),
				loop: 0
			);
			FreeSelfWhenDone.kr(buf);
			Out.kr(cBus,buf * \multiplicationBus.kr(1));
		}.asSynthDef;

		testSynthDef = { |cBus, freq = 440| SinOsc.ar(freq, 0, mul: In.kr(cBus)) }.asSynthDef;

		hasInitSynthDefs = true;
	}

	free {
		bus.free;
		buffer.free;
		all.removeAt(key);
	}

	trig {
		if(buffer.notNil)
		{
			/*
			Synth(this.synthName, [
			\cBus: bus,
			\bufnum: buffer.bufnum,
			\startTime, 0,
			\tempoClock, currentEnvironment.clock.tempo,
			// \multiplicationBus, targetBus.asMap
			// ], targetGroup);
			]);
			*/
			bufferSynthDef.name_(this.synthName);
			bufferSynthDef.play(args:
				[
					\cBus: bus,
					\bufnum: buffer.bufnum,
					\startTime, 0,
					\tempoClock, currentEnvironment.clock.tempo,
					// \multiplicationBus, targetBus.asMap
					// ]);
				]
			);
		}
		{ "% buffer not found".format(this).warn; }
	}

	test {|freq = 120|
		var testSynth;
		currentEnvironment.clock.sched(0, {
			testSynthDef.name_("EnvDef_test_%".format(key));
			testSynth = testSynthDef.play(args:[\cBus: bus, \freq: freq]);
			this.trig;
			nil;
		});
		currentEnvironment.clock.sched(duration, {
			"End of test %".format(testSynth).postln;
			testSynth.free;
			nil;
		});
	}

	synthName {	^"Env_%".format(key).asSymbol; }

	prStore { |itemKey, item, dur|


		key = itemKey;
		env = item;

		case
		{ item.isKindOf(Env) }
		{
			if(dur.isNil)
			{ duration = item.duration; }
			{ duration = dur; };
			this.prRender;
		}
		{ item.isKindOf(Integer) } { "Item is kind of Integer".warn; }
		{ item.isKindOf(Number) } { "Item is kind of Number".warn;  }
		{ item.isKindOf(Pbind) } { "Item is kind of Pbind".warn;  }
		{ item.isKindOf(UGen) } { "Item is kind of UGen".warn; }
		;

		all.put(itemKey, this);
	}

	prRender {
		Server.default.waitForBoot({
			var controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			buffer = Buffer.alloc(
				server: Server.default,
				numFrames: (controlRate * this.duration).ceil,
				numChannels: 1,
			);
			// buffer.loadCollection(this.envelope.asSignal((controlRate * this.duration).ceil));
			buffer.loadCollection(env.asSignal(controlRate * duration));
		});
	}

	printOn { |stream|
		stream << this.class.name << "('" << key << "' | dur: " << duration << ")";
	}

}
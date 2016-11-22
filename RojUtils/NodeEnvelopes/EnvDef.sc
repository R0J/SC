EnvDef {
	var <key;
	var <env;
	var <bus;
	var <duration;

	var buffer;

	classvar <>all;

	*initClass {
		all = IdentityDictionary.new;
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
			var bufferSynthDef;
			var bufSynthName = \bufferTest;

			bus = Bus.control(Server.default, 1);

			bufferSynthDef = {|cBus, bufnum, startTime = 0|
				var controlRate = Server.default.sampleRate / Server.default.options.blockSize;
				var buf = PlayBuf.kr(
					// var buf = PlayBuf.ar(
					numChannels: 1,
					bufnum: bufnum,
					startPos: startTime * controlRate,
					rate: \tempoClock.kr(1),
					loop: 0
				);
				FreeSelfWhenDone.kr(buf);
				Out.kr(cBus,buf * \multiplicationBus.kr(1));
			};
			bufferSynthDef.asSynthDef(name:bufSynthName.asSymbol).add;
		})

	}

	free {
		bus.free;
		all.removeAt(key);
	}

	trig {
		var bufSynthName = \bufferTest;
		var synth = Synth(bufSynthName.asSymbol, [
			\cBus: bus,
			\bufnum: buffer.bufnum,
			\startTime, 0,
			\tempoClock, currentEnvironment.clock.tempo,
			// \multiplicationBus, targetBus.asMap
			// ], targetGroup);
		]);
	}

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
		// signal =
		var controlRate = Server.default.sampleRate / Server.default.options.blockSize;
		buffer = Buffer.alloc(
			server: Server.default,
			numFrames: (controlRate * this.duration).ceil,
			numChannels: 1,
		);
		// buffer.loadCollection(this.envelope.asSignal((controlRate * this.duration).ceil));
		buffer.loadCollection(env.asSignal(controlRate * duration));
	}

	printOn { |stream|
		stream << this.class.name << "('" << key << "' | dur: " << duration << ")";
	}

}
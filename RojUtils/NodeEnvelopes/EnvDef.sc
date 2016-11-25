EnvDef {
	var <key;
	var <env;
	var <bus;

	var <nodeName, <controlName;

	var <duration;
	var <minValue, <maxValue;

	var <buffer;

	classvar <>all;
	classvar <>busLibrary;
	classvar hasInitSynthDefs;
	classvar bufferSynthDef, testSynthDef;

	*initClass {
		all = IdentityDictionary.new;
		busLibrary = IdentityDictionary.new;
		hasInitSynthDefs = false;
	}

	*new { |key, item, dur = nil|
		var def = this.all.at(key);
		if(def.isNil)
		{ def = super.new.init.prStore(key, item, dur); }
		{ if(item.notNil) {	def.prStore(key, item, dur); }};
		^def;
	}

	*exist { |key| if(this.all.at(key).notNil) { ^true; } { ^false; } }

	init {
		bus = nil;
		nodeName = nil;
		controlName = nil;
		if(hasInitSynthDefs.not) { this.initSynthDefs; };
	}

	map {|nodeKey, controlKey|

		var busName = "%_%".format(nodeKey, controlKey).asSymbol;
		Server.default.waitForBoot({
			var targetBus = busLibrary.at(busName);
			nodeName = nodeKey;
			controlName = controlKey;
			if(targetBus.isNil)
			{
				bus = Bus.control(Server.default, 1);
				busLibrary.put(busName, bus);
				nodeKey.asSymbol.envirGet.map(controlKey.asSymbol, BusPlug.for(bus));
			}
			{
				bus = targetBus;
			}
		});
	}

	unmap {

	}

	initSynthDefs{
		Server.default.waitForBoot({
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

			testSynthDef = { |cBus, freq = 440, minRange = -1, maxRange = 1|
				SinOsc.ar(freq!2, 0, mul: In.kr(cBus).range(minRange, maxRange));
			}.asSynthDef;

			hasInitSynthDefs = true;
		});
	}

	free {
		// bus.free;
		buffer.free;
		// busLibrary.removeAt(key.asSymbol);
		all.removeAt(key);
	}

	trig { |startTime = 0, parentGroup = nil|
		if(buffer.notNil)
		{
			var group = RootNode(Server.default);
			if(parentGroup.notNil) { group = parentGroup; };
			bufferSynthDef.name_(this.synthName);
			bufferSynthDef.play(
				target: group,
				args:
				[
					\cBus: bus,
					\bufnum: buffer.bufnum,
					\startTime, startTime,
					\tempoClock, currentEnvironment.clock.tempo,
					// \multiplicationBus, targetBus.asMap
				]
			);
		}
		{ "% buffer not found".format(this).warn; }
	}

	test {|freq = 120, startTime = 0|
		{
			if(duration - startTime > 0)
			{
				var testSynth;
				currentEnvironment.clock.sched(0, {
					testSynthDef.name_("EnvDef_test_%".format(key));
					testSynth = testSynthDef.play(
						target: RootNode(Server.default),
						args:[
							\cBus: bus,
							\freq: freq,
							\minRange: minValue,
							\maxRange: maxValue
						]
					);
					this.trig(startTime);
					nil;
				});
				currentEnvironment.clock.sched((duration - startTime), {
					// "End of test %".format(testSynth).postln;
					testSynth.free;
					// testSynth.release(2);
					nil;
				});
			}
			{ "% is shorter than arg startTime(%)".format(this, startTime).warn; }
		}.defer(0.01);
	}

	plot {|size = 400|
		{
			if(env.notNil,
				{ env.plotNamedEnv(this.synthName, size); },
				{ "% envelope not found".format(this).warn; }
			);
		}.defer(0.01);
	}

	synthName {	^"EnvDef('%')".format(key).asSymbol; }

	prStore { |itemKey, item, dur|
		key = itemKey;

		case
		{ item.isKindOf(Env) }
		{
			env = item;
			if(dur.isNil)
			{ duration = item.duration; }
			{ duration = dur; };
		}
		{ item.isKindOf(Number) || item.isKindOf(Integer) }
		{
			if(dur.isNil) {
				env = Env([item, item], 1, \lin);
				duration = 1;
			} {
				env = Env([item, item], dur, \lin);
				duration = dur;
			};
		}
		{ item.isKindOf(Pbind) } { "Item is kind of Pbind".warn; ^this;}
		{ item.isKindOf(UGen) } { "Item is kind of UGen".warn; ^this;}
		;

		maxValue = env.levels[0];
		minValue = env.levels[0];
		env.levels.do({|lev|
			if(lev > maxValue) { maxValue = lev; };
			if(lev < minValue) { minValue = lev; };
		});
		// "EnvDef min: % | max: % ".format(minValue, maxValue).postln;

		Server.default.waitForBoot({
			var controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			buffer = Buffer.alloc(
				server: Server.default,
				numFrames: (controlRate * this.duration).ceil,
				numChannels: 1,
			);
			buffer.loadCollection(env.asSignal(controlRate * duration));
			// buffer.normalize;
		});

		all.put(itemKey, this);
	}

	printOn { |stream| stream << this.class.name << "('" << key << "' | dur: " << duration << ")"; }

}





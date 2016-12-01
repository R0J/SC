EnvDef {
	var <key;
	var <env;
	var <bus;

	var parentNode;
	var <nodeName, <controlName;

	var <duration;
	var <minValue, <maxValue;

	var <buffer;

	classvar <>all;
	classvar <>busLibrary;
	classvar hasInitSynthDefs;
	classvar bufferSynthDef, testSynthDef;

	*initClass {
		// all = IdentityDictionary.new;
		all = MultiLevelIdentityDictionary.new;
		busLibrary = IdentityDictionary.new;
		hasInitSynthDefs = false;
	}

	*new { |key, item, dur = nil| ^this.newForNode(nil, key, item, dur); }

	*newForNode { |node, key, item, dur ... args|
		var def = this.get(key, node);

		if(def.isNil)
		{
			if(item.notNil)
			{ def = super.new.init.init2(node, key, item, dur); }
			{ def = nil; }
		}
		{ if(item.notNil) {	def.init2(node, key, item, dur); }};
		^def;
	}

	*get { |key, node = nil|
		var def;
		if(this.exist(key, node))
		{
			if(node.isNil)
			{ def = this.all.atPath([key.asSymbol]); }
			{ def = this.all.atPath([node.envirKey, key.asSymbol]); };
		}
		{ def = nil; };
		("get:" + def).postln;
		^def;
	}

	*exist { |key, node = nil|
		var item;
		if(node.isNil)
		{
			item = this.all.atPath([key.asSymbol]);
			("item1: " + item).postln;
			if(item.isKindOf(EnvDef) || item.isKindOf(IdentityDictionary)) { ^true; } { ^false; };
			// if(this.all.atPath([key.asSymbol]).notNil) { ^true; } { ^false; } }
		}
		{
			// if(this.all.atPath([node.envirKey.asSymbol, key.asSymbol]).notNil) { ^true; } { ^false; }
			item = this.all.atPath([node.envirKey.asSymbol, key.asSymbol]);
			("item2: " + item).postln;
			case
			{ item.isNil } { ^false; }
			{ item.isKindOf(EnvDef) } { }
			{ item.isKindOf(IdentityDictionary) } { ^true; } { ^false; };
		};

	}

	*print {
		this.all.dictionary.sortedKeysValuesDo({ |key, oneNode|

			if(oneNode.isKindOf(IdentityDictionary))
			{
				"key: %".format(key).postln;
				oneNode.sortedKeysValuesDo({ |controlName, envDef|
					"\t- controlName: %, one: %".format(controlName, envDef).postln;
				});
			}
			{
				"key: %, one: %".format(key, oneNode).postln;
			}
		});
		// this.all.postln;
		// this.all.sortedKeysValuesDo({|envName, oneEnv| oneEnv.postln; })
	}

	init {
		bus = nil;
		nodeName = nil;
		controlName = nil;
		if(hasInitSynthDefs.not) { this.initSynthDefs; };
	}

	init2 { |node, itemKey, item, dur|

		if(hasInitSynthDefs.not) { this.initSynthDefs; };

		parentNode = node;

		key = itemKey.asSymbol;

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

		// all.put(itemKey, this);
		if(parentNode.isNil)
		{ all.putAtPath([itemKey.asSymbol], this); }
		{ all.putAtPath([parentNode.envirKey.asSymbol, itemKey.asSymbol], this); }

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
		// nodeKey.asSymbol.envirGet.unmap(controlKey.asSymbol);
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

	trig { |startTime = 0, parentGroup = nil, clock = nil|
		if(clock.isNil) { clock = currentEnvironment.clock; };

		// "% trig time: %".format(this, clock.beats).postln;

		if(buffer.notNil)
		{
			// var synth;
			var group = RootNode(Server.default);
			if(parentGroup.notNil) { group = parentGroup; };
			bufferSynthDef.name_(this.synthName);
			// bufferSynthDef.optimizeGraph;
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
			// synth
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
		key = itemKey.asSymbol;

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





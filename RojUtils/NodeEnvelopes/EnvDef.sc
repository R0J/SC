EnvDef {
	var <key;
	var <env;
	var <bus;

	var <parentNode;

	var <duration;
	var <minValue, <maxValue;

	var <buffer;

	var isSynthLoaded, isBufferRendered;

	classvar <>all;
	classvar <>busLibrary;
	classvar hasInitSynthDefs;
	classvar bufferSynthDef, testSynthDef;
	classvar isEnvDefReady;

	*initClass {
		all = MultiLevelIdentityDictionary.new;
		busLibrary = IdentityDictionary.new;
		hasInitSynthDefs = false;
	}

	*new { |key, item, dur = nil| ^this.newForNode(nil, key, item, dur); }

	*newForNode { |node, key, item, dur ... args|
		var def;
		// isEnvDefReady = Condition.new;
		// Routine {
			if(this.exist(key, node))
			{ def = this.get(key, node); }
			{ def = nil; };

			if(def.isNil)
			{
				if(item.notNil)
				{ def = super.new.init(node, key, item, dur); }
				{ def = nil; }
			}
			{ if(item.notNil) {	def.init(node, key, item, dur); }};
		// isEnvDefReady.hang;
	// }.play;
		^def;
	}

	*get { |key, node = nil|
		var path;
		if(node.isNil)
		{ path = [\default, key.asSymbol]; }
		{ path = [node.envirKey.asSymbol, key.asSymbol] };
		^this.all.atPath(path);
	}

	*exist { |key, node = nil| if(this.get(key, node).notNil) { ^true; } { ^false; } }

	*print {
		this.all.dictionary.sortedKeysValuesDo({ |key, oneNode|
			"key: %".format(key).postln;
			oneNode.sortedKeysValuesDo({ |controlName, envDef|
				"\t- controlName: %, one: %".format(controlName, envDef).postln;
			});
		});
	}

	init { |node, itemKey, item, dur|
		var controlRate;
		var renderTimeStart, renderTimeEnd;
		var bufferID, bufferFramesCnt;

		isSynthLoaded = Condition.new;
		isBufferRendered = Condition.new;

		if(hasInitSynthDefs.not) { this.initSynthDefs; };

		Routine {
			if(hasInitSynthDefs.not)
			{
				// "time % -> wainting for load synth".format(SystemClock.beats).warn;
				isSynthLoaded.hang;
				// "time % -> Synth loaded ".format(SystemClock.beats).warn;
			};

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

			renderTimeStart = SystemClock.beats;
			// "time % -> wainting for render buffer".format(SystemClock.beats).warn;
			controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			buffer = Buffer.alloc(
				server: Server.default,
				numFrames: (controlRate * this.duration).ceil,
				numChannels: 1,
			);
			buffer.loadCollection(
				collection: env.asSignal(controlRate * duration),
				action: {|buff|
					bufferID = buff.bufnum;
					bufferFramesCnt = buff.numFrames;
					isBufferRendered.unhang;
				}
			);
			isBufferRendered.hang;
			renderTimeEnd = SystemClock.beats;

			"Rendering of buffer ID(%) done \n\t- buffer duration: % sec \n\t- render time: % sec \n\t- frame count: %".format(
				bufferID,
				env.duration,
				(renderTimeEnd - renderTimeStart),
				bufferFramesCnt
			).postln;
			// buffer.normalize;
			// });

			if(node.isNil)
			{ all.putAtPath([\default, itemKey.asSymbol], this); }
			{ all.putAtPath([node.envirKey.asSymbol, itemKey.asSymbol], this); };

			CycleDef.update;
			// isEnvDefReady.unhang;
		}.play;
	}

	map {|nodeKey, controlKey|

		var busName = "%_%".format(nodeKey, controlKey).asSymbol;
		Server.default.waitForBoot({
			var targetBus = busLibrary.at(busName);
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

	free {
		var path;
		if(parentNode.isNil)
		{ path = [\default, key.asSymbol]; }
		{ path = [parentNode.envirKey.asSymbol, key.asSymbol] };
		this.all.removeEmptyAtPath(path);

		buffer.free;
	}

	initSynthDefs{
		Server.default.waitForBoot({
			bufferSynthDef = { |cBus, multBus, bufnum, startTime = 0|
				var controlRate = Server.default.sampleRate / Server.default.options.blockSize;
				var buf = PlayBuf.kr(
					numChannels: 1,
					bufnum: bufnum,
					startPos: startTime * controlRate,
					rate: \tempoClock.kr(1),
					loop: 0
				);
				FreeSelfWhenDone.kr(buf);
				// Out.kr(cBus,buf * In.kr(multBus));
				Out.kr(cBus,buf * \multiplicationBus.kr(1));
			}.asSynthDef;

			testSynthDef = { |cBus, freq = 440, minRange = -1, maxRange = 1|
				SinOsc.ar(freq!2, 0, mul: In.kr(cBus).range(minRange, maxRange));
			}.asSynthDef;

			hasInitSynthDefs = true;
			isSynthLoaded.unhang;
		});
	}

	trig { |startTime = 0, endTime = nil, parentGroup = nil, clock = nil, multBus = nil|
		if(clock.isNil) { clock = currentEnvironment.clock; };

		// "% trig time: %".format(this, clock.beats).postln;

		// multBus.warn;

		if(buffer.notNil)
		{
			var synth;
			var group = RootNode(Server.default);
			if(parentGroup.notNil) { group = parentGroup; };
			bufferSynthDef.name_(this.synthName);
			synth =	bufferSynthDef.play(
				target: group,
				args:
				[
					\cBus: bus,
					\bufnum: buffer.bufnum,
					\startTime, startTime,
					\tempoClock, currentEnvironment.clock.tempo,
					// \multBus, multBus.index
					\multiplicationBus, multBus.asMap
				]
			);
			// synth.set(\multiplicationBus, multBus);
			if(endTime.notNil)
			{
				clock.sched((endTime - startTime), { synth.free; nil; });
			}
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

	printOn { |stream| stream << this.class.name << "('" << key << "' | dur: " << duration << ")"; }

}





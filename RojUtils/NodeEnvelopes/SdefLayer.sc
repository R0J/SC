SdefLayer {

	classvar rate;

	var sDef, <index;
	var parents;
	var selector, arguments;
	var <signal, <duration;

	*new {|sDef, index| ^super.newCopyArgs(sDef, index).init }

	*initClass {
		// rate = 44100;
		rate = 44100 / 64;
		// rate = ControlRate.ir;
	}

	init {
		parents = Set.new;
		signal = Signal.newClear(rate);
		duration = 1;
	}

	storeArguments { |method ... args|
		selector = method.name.asSymbol;
		arguments = args;
	}

	// sources //////////////////////////

	env { |levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3], dur = nil|
		var envelope = Env(levels, times, curves);
		duration = dur ? envelope.duration;

		if(dur.notNil)
		{
			signal = Signal.newClear(dur * rate).fill(levels[levels.size-1]);
			signal.overWrite(envelope.asSignal(envelope.duration * rate), 0)
		}
		{
			signal = envelope.asSignal(envelope.duration * rate)
		};
		this.storeArguments(thisMethod, levels, times, curves, dur);
		this.update;
	}

	level { |level = #[1], time = #[1], dur = nil|
		var times = time.asArray.wrapExtend(level.asArray.size);
		var offset = 0;
		duration = dur ? times.sum;

		if(dur.notNil)
		{ signal = Signal.newClear(dur * rate).fill(level.asArray[level.asArray.size-1]) }
		{ signal = Signal.newClear(times.sum * rate) };

		level.asArray.do({|lev, i|
			var sigSize = times[i] * rate;
			signal.overWrite(Signal.newClear(sigSize).fill(lev), offset);
			offset = offset + sigSize;
		});

		this.storeArguments(thisMethod, level, time, dur);
		this.update;
	}

	ramp { |from = 1, to = 0, time = 1, dur = nil|
		this.env([from, to], time, \lin, dur);
	}

	freq { |octave = #[3], degree = #[4], time = #[1], dur = nil|
		var octaves = octave.asArray.wrapExtend(degree.asArray.size);
		var times = time.asArray.wrapExtend(degree.asArray.size);
		var offset = 0;
		duration = dur ? times.sum;

		if(dur.notNil)
		{ signal = Signal.newClear(dur * rate) }
		{ signal = Signal.newClear(times.sum * rate) };

		degree.asArray.do({|deg, i|
			var note = deg.degreeToKey([0, 2, 4, 5, 7, 9, 11], 12);
			var midNote = (note / 12 + octaves[i]) * 12;
			var freq = midNote.midicps;
			var sigSize = times[i] * rate;
			signal.overWrite(Signal.newClear(sigSize).fill(freq), offset);
			offset = offset + sigSize;
			// "freq: % Hz, midiNote: %".format(freq, midNote).postln;
			if(dur.notNil)
			{
				if(i == (degree.size-1))
				{
					sigSize = (dur - times[i]) * rate;
					signal.overWrite(Signal.newClear(sigSize).fill(freq), offset);
				}
			}
		});

		this.storeArguments(thisMethod, octave, degree, time, dur);
		this.update;
	}

	delete {
		parents.do({|parentLayer|
			parentLayer.removeParent(this);
		});

		if(index == 0)
		{
			signal = Signal.newClear(rate);
			duration = 1;
		}
		{ sDef.layers.removeAt(index) };

		this.update;
	}

	// editing //////////////////////////

	shift {|target, offset|
		var layer = sDef.layers.at(target);
		duration = layer.duration + offset;

		if(layer.notNil)
		{
			var offSize = offset * rate;
			signal = Signal.newClear(layer.size + offSize);
			signal.overWrite(layer.signal, offSize);
			layer.addParent(this);
		};
		this.storeArguments(thisMethod, target, offset);
		this.update;
	}

	dup { |target, n|
		var layer = sDef.layers.at(target);
		duration = layer.duration * n;

		if(layer.notNil)
		{
			signal = Signal.new;
			n.do({ signal = signal ++ layer.signal });
			layer.addParent(this);
		};
		this.storeArguments(thisMethod, target, n);
		this.update;
	}

	dupTime { |target, time, targetDur = nil|
		var layer = sDef.layers.at(target);
		var rest = time % targetDur ? layer.duration;
		var loopCnt = (time-rest)/targetDur;
		duration = time;

		if(layer.notNil)
		{
			signal = Signal.newClear(time * rate);
			loopCnt.do({|noLoop| signal.overWrite(layer.signal, noLoop * targetDur * rate) });
			if(rest != 0) { signal.overWrite(layer.signal, loopCnt * targetDur * rate) };
			layer.addParent(this);
		};
		this.storeArguments(thisMethod, target, time, targetDur);
		this.update;
	}

	fixTime { |target, time|
		var layer = sDef.layers.at(target);
		duration = time;

		if(layer.notNil)
		{
			signal = Signal.newClear(time * rate);
			signal.overWrite(layer.signal);
			layer.addParent(this);
		};
		this.storeArguments(thisMethod, target, time);
		this.update;
	}

	// merge //////////////////////////

	add { |...targets|
		signal = Signal.new;
		duration = 0;

		targets.flatten.do({|index|
			var layer = sDef.layers.at(index);
			if(layer.notNil)
			{
				duration = duration + layer.duration;
				if(layer.signal.size > signal.size) { signal = signal.extend(layer.signal.size, 0) };
				signal.overDub(layer.signal, 0);
				layer.addParent(this);
			}
		});
		this.storeArguments(thisMethod, targets.flatten);
		this.update;
	}

	over { |...targets|
		signal = Signal.new;
		duration = 0;

		targets.flatten.do({|index|
			var layer = sDef.layers.at(index);
			if(layer.notNil)
			{
				duration = duration + layer.duration;
				if(layer.signal.size > signal.size) { signal = signal.extend(layer.signal.size, 0) };
				signal.overWrite(layer.signal, 0);
				layer.addParent(this);
			}
		});
		this.storeArguments(thisMethod, targets.flatten);
		this.update;
	}

	chain { |...targets|
		signal = Signal.new;
		duration = 0;

		targets.flatten.do({|index|
			var layer = sDef.layers.at(index);
			if(layer.notNil)
			{
				duration = duration + layer.duration;
				signal = signal ++ layer.signal;
				layer.addParent(this);
			};
		});
		this.storeArguments(thisMethod, targets.flatten);
		this.update;
	}

	stutter { |pattern ... targets|

	}

	fade { |targetFrom, targetTo, fadeTime = 1|
		var layerFrom = sDef.layers.at(targetFrom);
		var layerTo = sDef.layers.at(targetTo);
		var fSize = fadeTime * rate;
		var xFade = Array.interpolation(fSize,0,1);
		duration = fadeTime;

		signal = Signal.fill(fSize, {|i|
			var iModA = i % layerFrom.size;
			var iModB = i % layerTo.size;
			var from = layerFrom.signal[iModA]*(1-xFade[i]);
			var to = layerTo.signal[iModB]*xFade[i];
			// "x: % i: % || a: % || b: %".format(i, from, to).postln;
			from + to
		});

		layerFrom.addParent(this);
		layerTo.addParent(this);

		this.storeArguments(thisMethod, targetFrom, targetTo, fadeTime);
		this.update;
	}

	// signal operations //////////////////////////

	*prSigChain { |...arrSig|
		var sig = Signal.new;
		arrSig.do({ |oneSig| if(oneSig.isKindOf(Collection)) { sig = sig ++ oneSig } });
		if(sig.isEmpty.not)	{ ^sig } { ^nil };
	}

	*prSigOffset { |sig, offsetSize, fillByFirst = true|
		if(sig.isKindOf(Collection))
		{
			var insertedSignal;
			if(fillByFirst)
			{ insertedSignal = Signal.fill(offsetSize, { sig.at(0) }) }
			{ insertedSignal = Signal.newClear(offsetSize) };
			^Signal.newFrom(insertedSignal ++ sig);
		}
		{ ^nil }
	}

	*prSigExtend { |sig, extendSize, fillByLast = true|
		if(sig.isKindOf(Collection))
		{
			var insertedSignal;
			if(fillByLast)
			{ insertedSignal = Signal.fill(extendSize, { sig.at(sig.size-1) }) }
			{ insertedSignal = Signal.newClear(extendSize) };
			^Signal.newFrom(sig ++ insertedSignal);
		}
		{ ^nil }
	}

	*prSigWrapAt { |sig, wrapSize|
		if(sig.isKindOf(Collection))
		{ ^Signal.fill( wrapSize, {|i| sig.wrapAt(i % wrapSize) }) }
		{ ^nil }
	}

	// references //////////////////////////

	perform { this.performList(selector, arguments)	}

	update {
		parents.do({|parentLayer|
			"SignalLayer.update [% -> %]".format(this.index, parentLayer.index).warn;
			parentLayer.perform;
		});
		if(parents.isEmpty) { sDef.render };
	}

	addParent { |target| parents.add(target); }
	removeParent { |target| parents.remove(target); }

	// informations //////////////////////////

	size { ^signal.size }

	printOn { |stream|	stream << this.class.name << "(id: " << index << " | dur: " << duration << ")"; }

	plot { sDef.plot }

}
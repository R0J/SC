SdefLayer {

	classvar rate;

	var sDef, <index;
	var parents;
	var selector, arguments;
	var <signal;

	*new {|sDef, index| ^super.newCopyArgs(sDef, index).init }

	*initClass {
		rate = 44100 / 64;
		// rate = 44100;
	}

	init {
		parents = Set.new;
		signal = Signal.newClear(rate);
	}

	storeArguments { |method ... args|
		selector = method.name.asSymbol;
		arguments = args;
	}

	// sources //////////////////////////

	env { |levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3]|
		var envelope = Env(levels, times, curves);
		signal = envelope.asSignal(envelope.duration * rate);
		this.storeArguments(thisMethod, levels, times, curves);
		this.update;
	}

	level { |level = 1, time = 1|
		signal = Signal.newClear(time * rate).fill(level);
		this.storeArguments(thisMethod, level, time);
		this.update;
	}

	ramp { |from = 1, to = 0, time = 1|
		this.env([from, to], time, \lin);
	}

	freq { |octave, degree|

	}

	delete {
		parents.do({|parentLayer|
			parentLayer.removeParent(this);
		});

		if(index == 0)
		{ signal = Signal.newClear(rate) }
		{ sDef.layers.removeAt(index) };

		this.update;
	}

	// editing //////////////////////////

	shift {|target, offset|
		var layer = sDef.layers.at(target);
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
		if(layer.notNil)
		{
			signal = Signal.new;
			n.do({ signal = signal ++ layer.signal });
			layer.addParent(this);
		};
		this.storeArguments(thisMethod, target, n);
		this.update;
	}

	dupTime { |target, time, targetDur|
		var rest = time % targetDur;
		var loopCnt = (time-rest)/targetDur;
		var layer = sDef.layers.at(target);
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
		targets.flatten.do({|index|
			var layer = sDef.layers.at(index);
			if(layer.notNil)
			{
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
		targets.flatten.do({|index|
			var layer = sDef.layers.at(index);
			if(layer.notNil)
			{
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
		targets.flatten.do({|index|
			var layer = sDef.layers.at(index);
			if(layer.notNil)
			{
				signal = signal ++ layer.signal;
				layer.addParent(this);
			};
		});
		this.storeArguments(thisMethod, targets.flatten);
		this.update;
	}

	stutter { |pattern ... targets|

	}

	fade { |fromTarget, toTarget|

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

	duration { ^signal.size / rate }
	size { ^signal.size }

	printOn { |stream|	stream << this.class.name << "(id: " << index << " | dur: " << this.duration << ")"; }

	plot { sDef.plot }

}
SignalLayer {

	classvar rate;

	var sDef, index;

	var parents;
	var selector, arguments;
	var <signal;

	*new {|sDef, index| ^super.newCopyArgs(sDef, index).init }

	*initClass { rate = 44100 / 64 }

	init {
		parents = Set.new;
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

	// editing //////////////////////////

	add {|...targets|
		var layer;
		var addSize = 0;
		// "targets: %".format(targets.flatten).postln;

		targets.flatten.do({|index|
			layer = sDef.layers.at(index);
			if(layer.notNil) { if(addSize < layer.size) { addSize = layer.size }}
		});
		signal = Signal.newClear(addSize);
		targets.flatten.do({|index|
			layer = sDef.layers.at(index);
			if(layer.notNil)
			{
				signal.overDub(layer.signal, 0);
				layer.addParent(this);
				// "ADD".warn;
			}
		});
		this.storeArguments(thisMethod, targets.flatten);
		this.update;
	}

	shift {|target, offset|
		var layer = sDef.layers.at(target);

		if(layer.notNil)
		{
			var offSize = offset * rate;
			signal = Signal.newClear(layer.size + (offset * rate));
			signal.overWrite(layer.signal, offSize);

			layer.addParent(this);
			// "SHIFT".warn;
		};

		this.storeArguments(thisMethod, target, offset);
		this.update;
	}

	// references //////////////////////////

	perform { this.performList(selector, arguments)	}

	update {
		parents.do({|parentLayer|
			"%.updateParents -> %".format(this, parentLayer).warn;
			parentLayer.perform;
			parentLayer.update;
		});
		sDef.update;
	}

	addParent { |target| parents.add(target); }
	removeParent { |target| parents.remove(target); }

	// informations //////////////////////////

	duration { ^signal.size / rate }
	size { ^signal.size }

	printOn { |stream|	stream << this.class.name << "(id: " << index << " | dur: " << this.duration << ")"; }

}
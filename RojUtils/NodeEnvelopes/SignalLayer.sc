SignalLayer {

	classvar rate;
	classvar <>library;

	var sDef, index;

	var parents, reference;
	var editType, editArgs;
	var <signal;

	*new {|sDef, index| ^super.newCopyArgs(sDef, index).init }

	*initClass { rate = 44100 / 64 }

	init {
		parents = Set.new;
		editArgs = IdentityDictionary.new;
	}

	duration { ^signal.size / rate }
	size { ^signal.size }

	// sources //////////////////////////

	env { |levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3]|
		var envelope = Env(levels, times, curves);
		signal = envelope.asSignal(envelope.duration * rate);
		this.update;
	}

	// editing //////////////////////////

	shift {|target, offset|
		var layer = sDef.layers.at(target);
		if(layer.notNil)
		{
			layer.addParent(this);
			editType = \shift;
			reference = RefCopy(layer);
			editArgs.put(\offset, offset);
		};
		this.update;
	}

	// references //////////////////////////

	update {
		case
		{ editType == \shift } {
			var	source = reference.next.signal;
			var srcSize = source.size;
			var offSize = editArgs.at(\offset) * rate;
			signal = Signal.newClear(srcSize + offSize);
			signal.overWrite(source, offSize);
			// "type: shift".warn;
		};

		parents.do({|parentLayer|
			"%.updateParents -> %".format(this, parentLayer).warn;
			parentLayer.update;
		});
		sDef.update;
	}

	addParent { |target| parents.add(target); }
	removeParent { |target| parents.remove(target); }

	/*
	*connectRefs {|parentKey, childKey|
	var parentDef = this.exist(parentKey);
	var childDef = this.exist(childKey);
	// "Sdef.connectRefs(parent:% | child:%)".format(parentDef, childDef).warn;
	if(parentDef.key.notNil && childDef.key.notNil)
	{
	parentDef.addChild(childDef);
	childDef.addParent(parentDef);
	}
	}

	*disconnectRefs {|parentKey, childKey|
	var parentDef = this.exist(parentKey);
	var childDef = this.exist(childKey);
	if(parentDef.key.notNil) { parentDef.removeChild(childDef) };
	if(childDef.key.notNil) { childDef.removeParent(parentDef) };
	}

	addChild { |target| children.add(target.key); }
	addParent { |target| parents.add(target.key); }

	removeChild { |target| children.remove(target.key); }
	removeParent { |target| parents.remove(target.key); }

	updateParents {
	parents.do({|parentKey|
	var sDef = Sdef.exist(parentKey);
	if(sDef.notNil) {
	"%.updateParents -> %".format(this, sDef).warn;
	sDef.update;
	};
	});
	}

	update { this.mergeLayers }
	*/

	printOn { |stream|	stream << this.class.name << "(id: " << index << " | dur: " << this.duration << ")"; }

}
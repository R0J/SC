Sdef2 {
	classvar <>library;
	classvar <version;
	classvar controlRate;
	classvar hasInitSynthDefs, bufferSynthDef;

	var <key, <path;
	var <duration, <size;
	var <bus, <buffer, <synth;
	var <parents, <children;
	var <layers, <>currentLayer;
	var <sigLayers;
	var <updatePlot;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
		controlRate = 44100 / 64;
		hasInitSynthDefs = false;
		version = 0.2;
	}

	*new { |name = nil, index = nil|
		var sDef;
		if(hasInitSynthDefs.not) { this.initSynthDefs; };

		if(name.asArray.notEmpty)
		{
			sDef = this.exist(name);
			if(sDef.isNil) { sDef = super.new.init(name).initBus };
		}
		{ sDef = super.new.init(nil) };

		sDef.currentLayer = index;
		if(index.isNil) { sDef.currentLayer = sDef.layerCount };

		if(index.notNil) { ^sDef.sigLayers.at(index) };

		^sDef;
	}

	*exist { |name|
		var path = name.asArray ++ \def;
		var sDef = this.library.atPath(path);
		if(sDef.notNil) { ^sDef; } { ^nil; }
	}

	*printAll { this.library.postTree; ^nil; }

	*frame { |time| ^controlRate * time }
	*time { |frame| ^frame / controlRate }

	// init //////////////////////////

	*initSynthDefs{
		if(Server.default.serverRunning.not) { Server.default.onBootAdd({ this.initSynthDefs }) }
		{
			bufferSynthDef = { |bus, bufnum, startTime = 0|
				var buf = PlayBuf.kr(
					numChannels: 1,
					bufnum: bufnum,
					startPos: startTime * controlRate,
					rate: \tempoClock.kr(1),
					loop: 1
				);
				Out.kr(bus, buf);
			}.asSynthDef;

			controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			"\nSdef initialization of SynthDefs done. Control rate set on %".format(controlRate).postln;
		};
		hasInitSynthDefs = true;
	}

	init { |name|
		this.key = name;
		this.updatePlot = false;

		parents = Set.new;
		children = Set.new;

		bus = nil;
		buffer = nil;
		synth = nil;

		layers = Order.new;
		currentLayer = 0;
		sigLayers = SignalLayer.new;
	}

	initBus {
		if(Server.default.serverRunning.not)
		{
			Server.default.onBootAdd({
				bus = Bus.control(Server.default, 1);
				"\t- Sdef(%) alloc control bus at index %".format(this.key, bus.index).postln;
			});
		}
		{ bus = Bus.control(Server.default, 1) }
	}

	// instance //////////////////////////

	key_ {|name|
		"rename def from % to %".format(key, name).postln;
		if(name.notNil)
		{
			var tempParents = parents.copy;
			var tempChildren = children.copy;

			parents.do({|parentKey| super.class.disconnectRefs(parentKey, key); });
			children.do({|childKey| super.class.disconnectRefs(key, childKey); });

			key = name;
			if(path.notNil) { library.removeEmptyAtPath(path) };
			path = key.asArray ++ \def;

			library.putAtPath(path, this);

			tempParents.do({|parentKey| super.class.connectRefs(parentKey, key); });
			tempChildren.do({|childKey| super.class.connectRefs(key, childKey); });
		}
	}

	duration_ {|dur|
		if(duration != dur)
		{
			if(buffer.notNil) { buffer.free; };
			duration = dur;
			size = super.class.frame(duration);
			// signal = Signal.newClear(super.class.frame(duration));

			if(Server.default.serverRunning)
			{
				buffer = Buffer.alloc(
					server: Server.default,
					numFrames: size,
					numChannels: 1,
				);
				"new buffer init (%)".format(buffer).warn;
			}

			// if(layers.lines.size > 0) { this.mergeLayers };
			// this.play;
		}
	}

	at { |index| ^layers.at(index) }

	layerCount { ^layers.lastIndex }

	layerDuration { ^super.class.time(layers.at(this.currentLayer).size) }

	signal {
		var lastIndex = layers.lastIndex;
		if(lastIndex.isNil) { ^nil } { ^layers.at(lastIndex) };
	}

	update {

		if(updatePlot) { this.plot };
	}

	delete {
		layers.removeAt(this.currentLayer);
		this.update;
	}

	env { |levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3]|
		var envelope = Env(levels, times, curves);
		this.duration = envelope.duration;
		layers.put(this.currentLayer, envelope.asSignal(size));
		this.update;
	}

	// editing //////////////////////////

	add { |...targets|
		var addSize = 0;
		var addSignal;
		targets.do({|index|
			if(layers.at(index).notNil)
			{
				var srcSize = layers.at(index).size;
				if(addSize < srcSize) { addSize = srcSize };
			}
		});
		addSignal = Signal.newClear(addSize);
		targets.do({|index|
			if(layers.at(index).notNil)
			{
				var source = layers.at(index);
				addSignal.overDub(source, 0);
			}
		});
		layers.put(this.currentLayer, addSignal);
		this.update;
	}

	shift { |layer, offset|
		var source = layers.at(layer);
		var srcSize = source.size;
		var offSize = super.class.frame(offset);
		var shiftSignal = Signal.newClear(srcSize + offSize);
		shiftSignal.overWrite(source, offSize);
		layers.put(this.currentLayer, shiftSignal);
		this.update;
	}

	// references //////////////////////////
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
	var sDef = this.exist(parentKey);
	if(sDef.notNil) {
	"%.updateParents -> %".format(this, sDef).warn;
	sDef.update;
	};
	});
	}

	update { this.mergeLayers }
	*/

	// info //////////////////////////

	printOn { |stream|	stream << this.class.name << "('" << this.key << "' | " << this.currentLayer << " | dur: " << this.layerDuration << ")"; }

	printName {
		var txtPath = "";
		path.do({|oneFolder|
			if(txtPath.isEmpty)
			{ txtPath = "%%".format("\\", oneFolder); }
			{
				if(oneFolder != \def)
				{ txtPath = "%%%".format(txtPath,"\\", oneFolder); }
			}
		});
		^txtPath;
	}

	plot {|update|
		this.updatePlot = update;

		if(this.signal.notNil)
		{
			var winName = "Sdef(%)".format(this.printName);
			var windows = Window.allWindows;
			var plotWin = nil;
			var plotter;

			windows.do({|oneW|
				if(winName.asSymbol == oneW.name.asSymbol) { plotWin = oneW; };
			});

			if(plotWin.isNil)
			{
				plotter = this.signal.plot(
					name: winName.asSymbol,
					bounds: Rect(700,680,500,300)
				);
				plotter.parent.alwaysOnTop_(true);
				plotter.parent.view.background_(Color.new255(30,30,30)).alpha_(0.9);
			}
			{
				plotWin.view.children[0].close;
				plotter = Plotter(
					name: winName.asSymbol,
					parent: plotWin
				);
				plotWin.view.children[0].bounds_(Rect(8,8,plotWin.view.bounds.width-16,plotWin.view.bounds.height-16));
				plotter.value = this.signal;
			};

			plotter.domainSpecs = [[0, super.class.time(layers.at(layers.lastIndex).size), 0, 0, "", " s"]];
			plotter.setProperties (
				\backgroundColor, Color.new255(30,30,30),
				\plotColor, Color.new255(30,190,230),
				\fontColor, Color.new255(90,90,90),
				\gridColorX, Color.new255(60,60,60),
				\gridColorY, Color.new255(60,60,60),
				\gridLinePattern, FloatArray[2,4],
			);
			plotter.refresh;
		}
		{ "% signal is empty".format(this).warn; };
	}

	updatePlot_ {|bool|	if(bool.isKindOf(Boolean)) { updatePlot = bool } }
}
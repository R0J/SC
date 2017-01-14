Sdef3 {

	classvar <>library;
	classvar controlRate;
	classvar hasInitSynthDefs, bufferSynthDef;

	var <key, <path;
	var <bus, <buffer, <synth;
	var <layers;
	var <updatePlot;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
		controlRate = 44100 / 64;
		hasInitSynthDefs = false;
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

		if(index.notNil)
		{
			var layer =	sDef.layers.at(index);
			if(layer.isNil) {
				layer = SignalLayer(sDef, index);
				sDef.layers.put(index, layer);
			}
			^layer;
		}
		{ ^sDef }
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

		// parents = Set.new;
		// children = Set.new;

		bus = nil;
		buffer = nil;
		synth = nil;

		layers = Order.new;
		// currentLayer = 0;
		// sigLayers = SignalLayer.new;
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
			key = name;
			if(path.notNil) { library.removeEmptyAtPath(path) };
			path = key.asArray ++ \def;
			library.putAtPath(path, this);
		}
	}

	layerCount { ^layers.lastIndex }

	signal {
		var lastIndex = layers.lastIndex;
		if(lastIndex.isNil) { ^nil } { ^layers.at(lastIndex).signal };
	}

	// informations //////////////////////////

	printOn { |stream|	stream << this.class.name << "('" << this.key << "' | cnt: " << this.layerCount << ")"; }

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

	update { if(updatePlot) { this.plot } }

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

			plotter.domainSpecs = [[0,  super.class.time(this.signal.size), 0, 0, "", " s"]];
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
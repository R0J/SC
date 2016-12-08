SignalDef {
	var <path;
	var <duration;
	var <signal;

	var autoPlot;

	classvar <>library;
	classvar controlRate;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
	}

	*new { |...path|
		var def;
		if(this.exist(path))
		{ def = this.get(path); }
		{ def = super.new.init(path); };
		^def;

	}

	*exist { |path|	if(this.get(path).notNil) { ^true; } { ^false; } }

	*get { |path| ^this.library.atPath(path); }

	*print { this.library.postTree; }

	init { |pathKey|

		path = pathKey;

		autoPlot = false;
		library.putAtPath(pathKey, this);

	}

	env { |envelope|
		Server.default.waitForBoot({
			controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			duration = envelope.duration;
			signal = envelope.asSignal(controlRate * duration);
			if(autoPlot) { this.plot };
		});
	}

	plot {|updatePlot|
		if(signal.notNil)
		{
			var windows = Window.allWindows;
			var plotWin = nil;
			var plotter;

			if(updatePlot.notNil) { autoPlot = updatePlot };

			windows.do({|oneW|
				if(this.path2txt.asSymbol == oneW.name.asSymbol) { plotWin = oneW; };
			});

			if(plotWin.isNil)
			{
				plotter = signal.plot(
					name: this.path2txt.asSymbol,
					bounds: Rect(800,500,500,300)
				);
				plotter.parent.alwaysOnTop_(true);
				plotter.parent.view.background_(Color.new255(30,30,30));
			}
			{
				plotWin.view.children[0].close;
				plotter = Plotter(
					name: this.path2txt.asSymbol,
					parent: plotWin
				);
				plotWin.view.children[0].bounds_(Rect(8,8,plotWin.view.bounds.width-16,plotWin.view.bounds.height-16));
				plotter.value = signal;
			};

			plotter.domainSpecs = [[0, duration, 0, 0, "", " s"]];
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
		{ "% envelope not found".format(this).warn; };
	}

	printOn { |stream|	stream << this.class.name << " (dur: " << duration << ")"; }
	// printOn { |stream|	stream << this.class.name << " ( " << this.path2txt << " )"; }

	path2txt {
		var txtPath = "";
		path.do({|oneFolder|
			if(txtPath.isEmpty)
			{ txtPath = "%%".format("\\", oneFolder); }
			{ txtPath = "%, %%".format(txtPath,"\\", oneFolder); }
		});
		^txtPath;
	}

}
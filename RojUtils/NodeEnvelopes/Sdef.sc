Sdef {
	var <path;
	var <duration;
	var <signal;
	var <setOrder;
	var <references;

	var autoPlot;

	classvar <>library;
	classvar controlRate;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
		controlRate = 44100 / 64;
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
		references = Set.new;
		autoPlot = false;
		library.putAtPath(pathKey, this);
	}

	addRef { |target| references.add(target); }
	updateRefs {
		references.do({|oneRef|
			"UPDATE %".format(oneRef).postln;
			// oneRef.setOrder
		})
	}

	fill {|dur, value|
		signal = Signal.newClear(controlRate * dur).fill(value);
		duration = dur;
		this.updateRefs;
	}

	set { |item, shift = 0|
		var itemSignal;
		var itemDur = 0;
		var fillValue = 0;

		case
		{ item.isKindOf(Env) }
		{
			itemSignal = item.asSignal(controlRate * item.duration);
			duration = item.duration + shift;
			signal = Signal.newClear(controlRate * duration);
		};

		signal.overDub( itemSignal, shift * controlRate);
		// signal.overWrite(itemSignal, shift * controlRate);
		this.updateRefs;
		if(autoPlot) { this.plot };
	}

	setn {  |...pairsTimeItem|
		// var order = Order.new;
		var totalDuration = 0;
		setOrder = Order.new;

		if(pairsTimeItem.size % 2 != 0)
		{
			"Arguments of time and items are't set in pairs. ArgExample: (0, Env(), 1.2, Env())".warn;
			^this;
		};

		pairsTimeItem.pairsDo({|time, item|
			var endTime;

			case
			{ item.isKindOf(Env) }
			{
				endTime = time + item.duration;
				if(totalDuration < endTime) { totalDuration = endTime };
				setOrder.put(time, item.asSignal(controlRate * item.duration));
				// "time: % | dur: % | total: %".format(time, envelope.duration, totalDuration).postln;
			}
			{ item.isKindOf(Sdef) }
			{
				endTime = time + item.duration;
				if(totalDuration < endTime) { totalDuration = endTime };
				setOrder.put(time, item.signal);
				item.addRef(this);
			};
		});

		signal = Signal.newClear(controlRate * totalDuration);
		duration = totalDuration;

		setOrder.indicesDo({|oneSignal, time|
			// "time: %".format(time).postln;
			// signal.overWrite(oneSignal, time * controlRate);
			signal.overDub(oneSignal, time * controlRate);
		});

		this.updateRefs;
		if(autoPlot) { this.plot };
	}

	plot {
		if(signal.notEmpty)
		{
			var windows = Window.allWindows;
			var plotWin = nil;
			var plotter;

			windows.do({|oneW|
				if(this.path2txt.asSymbol == oneW.name.asSymbol) { plotWin = oneW; };
			});

			if(plotWin.isNil)
			{
				plotter = signal.plot(
					name: this.path2txt.asSymbol,
					bounds: Rect(700,680,500,300)
				);
				plotter.parent.alwaysOnTop_(true);
				plotter.parent.view.background_(Color.new255(30,30,30)).alpha_(0.9);
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

	updatePlot {|bool|
		bool.isKindOf(Boolean).postln;
		if(bool.isKindOf(Boolean)) { autoPlot = bool; };

	}

	// printOn { |stream|	stream << this.class.name << " (key: " << this.path2txt << " | dur: " << duration << ")"; }
	printOn { |stream|	stream << this.class.name << " (dur: " << duration << ")"; }

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
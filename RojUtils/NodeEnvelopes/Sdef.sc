Sdef {
	var <path;
	var <duration;
	var <>signal;
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
		if(path.notEmpty)
		{
			if(this.exist(path))
			{ def = this.get(path); }
			{ def = super.new.init(path); };
			^def;
		}
		{^super.new.init(nil)}
	}

	*exist { |path|	if(this.get(path).notNil) { ^true; } { ^false; } }

	*get { |path| ^this.library.atPath(path); }

	*print { this.library.postTree; }

	init { |pathKey|
		path = pathKey;
		references = Set.new;
		autoPlot = false;
		if(pathKey.notNil) { library.putAtPath(pathKey, this); }
	}

	addRef { |target| references.add(target); }
	updateRefs {
		references.do({|oneRef|
			oneRef.prSetSignal(oneRef.setOrder);
		})
	}

	fill {|dur, value, shift = 0|
		var inOrder = Order.new;
		var item = Env([value, value], dur, \lin);
		inOrder.put(shift, item);
		this.prSetSignal(inOrder);
	}

	set { |item, shift = 0|
		var inOrder = Order.new;
		inOrder.put(shift, item);
		this.prSetSignal(inOrder);
	}

	setn {  |...pairsTimeItem|
		var inOrder = Order.new;

		pairsTimeItem.pairsDo({|time, item|
			inOrder.put(time, item);
		});
		this.prSetSignal(inOrder);

		/*
		if(times.size != items.size)
		{
		"Arguments of time and items are't set in pairs. ArgExample: (0, Env(), 1.2, Env())".warn;
		^this;
		};
		*/
	}

	prSetSignal {|inOrder|
		if(inOrder.isKindOf(Order)) {
			var totalDuration = 0;
			var sigOrder = Order.new;
			// var itemSignal;
			setOrder = Order.new;

			inOrder.keysValuesDo({|time, item|
				var endTime;
				// ("time:" + time).postln;
				// ("item:" + item).postln;

				case
				{ item.isKindOf(Env) }
				{
					var sDef = Sdef();
					endTime = time + item.duration;
					if(totalDuration < endTime) { totalDuration = endTime };
					sDef.signal = item.asSignal(controlRate * item.duration);
					setOrder.put(time, sDef);
					// sigOrder.put(time, signal);
					// "time: % | dur: % | total: %".format(time, envelope.duration, totalDuration).postln;
				}
				{ item.isKindOf(Sdef) }
				{
					endTime = time + item.duration;
					if(totalDuration < endTime) { totalDuration = endTime };
					// signal = Signal.newClear(controlRate * totalDuration);
					setOrder.put(time, item);
					item.addRef(this);
				};
			});
/*
			if(inOrder.indices.size > 1)
			{

				"multiSet".warn;
				signal = Signal.newClear(controlRate * totalDuration);
			};
			*/
			signal = Signal.newClear(controlRate * totalDuration);
			duration = totalDuration;

			setOrder.indicesDo({|sdef, time|
				"time: % | sig: %".format(time, sdef.duration).postln;
				// signal.overWrite(oneSignal, time * controlRate);
				signal.overDub(sdef.signal, time * controlRate);
			});

			this.updateRefs;
			if(autoPlot) { this.plot };
		};
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

	printOn { |stream|	stream << this.class.name << " (key: " << this.path2txt << " | dur: " << duration << ")"; }
	// printOn { |stream|	stream << this.class.name << " (dur: " << duration << ")"; }

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
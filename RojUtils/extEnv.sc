+ Env {
	connect {|env|
		var connectedEnv = super.class.new(this.levels, this.times, this.curves);
		env.do({|oneEnv|
			connectedEnv.levels = connectedEnv.levels.asArray ++ oneEnv.levels[1..oneEnv.levels.size-1].asArray;
			connectedEnv.times = connectedEnv.times.asArray ++ oneEnv.times.asArray;
			connectedEnv.curves = connectedEnv.curves.asArray ++ oneEnv.curves.asArray;
		});
		^connectedEnv;
	}

	crop {|atFrom = nil, atTo = nil|
		var cropedEnv = nil;

		if(atFrom.isNil) { atFrom = 0; };
		if(atFrom < 0) { atFrom = 0; };
		if(atTo.isNil) { atTo = this.duration; };
		if(atTo > this.duration) { atTo = this.duration; };

		this.segmentCount.do({|i|
			case
			{(this.segmentStart(i) < atFrom) && (this.segmentEnd(i) > atFrom) && (this.segmentEnd(i) < atTo)} {
				var cropAtStart = atFrom - this.segmentStart(i);
				var interEnv = this.prInterpolatedEnv(this.segment(i), cropAtStart, this.segment(i).duration);
				if((cropedEnv.isNil),
					{ cropedEnv = interEnv; },
					{ cropedEnv = cropedEnv.connect(interEnv); }
				);
			}
			{(this.segmentStart(i) >= atFrom) && (this.segmentEnd(i) <= atTo)} {
				if((cropedEnv.isNil) ,
					{ cropedEnv = this.segment(i); },
					{ cropedEnv = cropedEnv.connect(this.segment(i)); }
				);
			}
			{(this.segmentStart(i) >= atFrom) && (this.segmentStart(i) < atTo) && (this.segmentEnd(i) > atTo)} {
				var cropAtEnd = atTo - this.segmentStart(i);
				var interEnv = this.prInterpolatedEnv(this.segment(i), 0, cropAtEnd);
				if((cropedEnv.isNil),
					{ cropedEnv = interEnv; },
					{ cropedEnv = cropedEnv.connect(interEnv); }
				);
			}
			{(this.segmentStart(i) < atFrom) && (this.segmentEnd(i) > atTo)} {
				var cropAtStart = atFrom - this.segmentStart(i);
				var cropAtEnd = atTo - this.segmentStart(i);
				var interEnv = this.prInterpolatedEnv(this.segment(i), cropAtStart, cropAtEnd);
				if((cropedEnv.isNil),
					{ cropedEnv = interEnv; },
					{ cropedEnv = cropedEnv.connect(interEnv); }
				);
			};
		})
		^cropedEnv;
	}

	extend {|extendTime, maxLimit = 60|
		var extendedEnv = super.class.new(this.levels, this.times, this.curves);
		var envDur = this.duration;

		if(extendTime < envDur) {
			("Env.extend(" ++ extendTime ++ ") is lower than evelope duration (" ++ envDur ++ ")").postln;
			^this;
		};
		if(maxLimit < extendTime) {
			("Env.extend(" ++ extendTime ++ ") extendTime is higher than 2nd argument maxLimit (" ++ maxLimit ++ ")").postln;
			extendTime = maxLimit;
		};

		while ({ envDur < extendTime }, {
			envDur = extendedEnv.duration;
			envDur = envDur + this.duration;
			extendedEnv = extendedEnv.connect(this);
		});
		^extendedEnv.crop(0, extendTime);
	}

	// mensi pro zapis array ale horsi vysledky fadu
	fade_noInterpolation {|targetEnv, loops = 6|
		var fadedEnv = nil;
		var steps = (0, 1/(loops-1) .. 1);

		steps.do { |fadeIndex|
			if((fadedEnv.isNil),
				{ fadedEnv = super.class.new(this.levels, this.times, this.curves); },
				{ fadedEnv = fadedEnv.connect( blend(this, targetEnv, fadeIndex) ); }
			);
		};
		^fadedEnv;
	}

	fade {|targetEnv, loops = 6, interpolSeg = 20|
		var fadedEnv = nil;
		var interFromEnv = this.prInterpolatedEnv(this, 0, this.duration, interpolSeg);
		var interTargetEnv = this.prInterpolatedEnv(targetEnv, 0, targetEnv.duration, interpolSeg);
		var steps = (0, 1/(loops-1) .. 1);

		steps.do { |fadeIndex|
			if((fadedEnv.isNil),
				{ fadedEnv = super.class.new(this.levels, this.times, this.curves); },
				{ fadedEnv = fadedEnv.connect( blend(interFromEnv, interTargetEnv, fadeIndex) ); }
			);
		};
		^fadedEnv;
	}

	segment {|index| ^super.class.new([this.levels[index],this.levels[index+1]], this.times[index], this.curves[index]); }
	segmentStart {|index| ^this.times[-1..index-1].sum; }
	segmentEnd {|index| ^this.times[0..index].sum; }
	segmentCount { ^this.levels.size - 1 }

	plotNamedEnv {|envName, size = 400|
		var windows = Window.allWindows;
		var plotWin = nil;

		windows.do({|oneW|
			if(envName.asSymbol == oneW.name.asSymbol) { plotWin = oneW; };
		});

		if(plotWin.isNil, {
			this.plot( size:size, name:envName.asSymbol ).parent.alwaysOnTop_(true);
		},{
			var plotter;
			plotWin.view.children[0].close;
			plotter = Plotter(envName.asSymbol, parent:plotWin);
			plotter.value = this.asSignal(size);
			plotter.domainSpecs = [[0, this.duration, 0, 0, "", " s"]];
			plotter.refresh;
		});
		^this;
	}

	prInterpolatedEnv {|env, from, to, segments = 20|
		var segLevels = List.new;
		var segTimes = List.new;
		var segCurves = List.new;
		var cropEnvDur = to - from;
		var segmentsDur = cropEnvDur / segments;
		var currentPosition = from;

		segments.do({
			segLevels.add(env.at(currentPosition));
			segTimes.add(segmentsDur);
			segCurves.add(\lin);
			currentPosition = currentPosition + segmentsDur;
		});
		segLevels.add(env.at(currentPosition));

		^super.class.new(segLevels, segTimes, segCurves);
	}
}
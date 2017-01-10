Timeline2 {
	var timeline;

	*new { ^super.new.init(); }

	init { timeline = Order.new(); }

	put { |time, function, duration = 0|
		if(timeline[time].isNil, {
			timeline.put(time, Timebar2(time, function, duration));
		},{
			var arr = timeline[time].asArray;
			arr = arr ++ Timebar2(time, function, duration);
			timeline.put(time, arr);
		});
	}

	get {|time|
		var oneTime = timeline[time];
		var items = List.new();
		oneTime.asArray.do({|oneTimebar|
			items.add(oneTimebar.function);
		});

		case
		{items.size < 1 } { ^nil; }
		{items.size == 1 } { ^items[0].asArray; }
		{items.size > 1 } { ^items.asArray; }
		^nil;
	}

	getAtTime {|time|
		var items = List.new();
		timeline.indicesDo({|oneTime|
			oneTime.asArray.do({|oneTimebar|
				if(oneTimebar.isAtTime(time)) { items.add(oneTimebar.function); };
			});
		});
		case
		{items.size < 1 } { ^nil; }
		{items.size == 1 } { ^items[0].asArray; }
		{items.size > 1 } { ^items.asArray; };
	}

	array {
		var items = List.new();
		this.times.do({|oneTime|
			var arrTimebar = timeline[oneTime];
			arrTimebar.asArray.do({|oneTimebar|
				items.add([oneTimebar.from, oneTimebar.function, oneTimebar.duration]);
			});
		});
		^items.asArray;
	}

	timeBars {
		var items = List.new();
		this.times.do({|oneTime|
			var arrTimebar = timeline[oneTime];
			arrTimebar.asArray.do({|oneTimebar|	items.add(oneTimebar) });
		});
		^items.asArray;
	}

	times { ^timeline.indices; }

	duration {
		var endDuration = 0;
		timeline.array.do({|oneTime|
			oneTime.asArray.do({|oneTimebar|
				var end = oneTimebar.from + oneTimebar.duration;
				if(end > endDuration) { endDuration = end; };
			});
		})
		^endDuration;
	}

	print { |cntTabs = 0|
		var txt = "";
		var tabs = "";
		cntTabs.do({tabs = tabs ++ "\t"});
		timeline.indicesDo({|oneArray, oneTime|
			txt = txt ++  tabs ++ "- time" + oneTime;
			oneArray.asArray.do({|timeBar|
				if(timeBar.function.isKindOf(Function))
				{ txt = txt ++ "\n\t" ++ tabs ++ "-" + timeBar.function.def.sourceCode; }
				{ txt = txt ++ "\n\t" ++ tabs ++ "-" + timeBar.function; }
			});
			txt = txt ++ "\n";
		});
		txt.postln;
		^nil;
	}

	play {|startTime = 0|  // -> example of function -> {|item| item.postln; };

		if(startTime < this.duration)
		{
			var clock = TempoClock.new;
			if(currentEnvironment[\tempo].notNil) { clock = currentEnvironment.clock };

			timeline.array.do({|oneTime, no|
				oneTime.asArray.do({|oneTimebar|

					if((oneTimebar.from >= startTime))
					{
						clock.sched((oneTimebar.from - startTime), {
							oneTimebar.function.value;
							nil;
						});
					};

				});
			});

			clock.sched((this.duration - startTime), {clock.stop; "Timeline end".postln; nil;});
		}
	}

	schedToClock { |clock, function = nil| // -> example of function -> timline.schedToClock( clock, {|time, duration, item, key| ... });
		if(function.isNil) {
			function = {|time, duration, item, key|
				"\t- Timeline function call -> time: % || dur: % || key: % || item: %".format(time, duration, key, item).postln;
			}
		};

		this.array.do({|bar|
			var time = bar[0];
			var item = bar[1];
			var duration = bar[2];
			var key = bar[3];
			clock.sched(time, { function.value(time, duration, item, key); nil; });
		});
	}

	items { |function = nil| // -> example of function -> timline.items({|time, duration, item| ... });
		if(function.isNil) {
			function = {|time, duration, fnc|
				"\t- Timeline function call -> time: % || dur: % || item: %".format(time, duration, fnc).postln;
			}
		};

		this.array.do({|bar|
			var time = bar[0];
			var fnc = bar[1];
			var duration = bar[2];
			function.value(time, duration, fnc);
		});
	}
}

Timebar2 {
	var <from;
	var <function;
	var <>duration;

	*new {|from, function, duration = 0| ^super.newCopyArgs(from, function, duration); }

	isAtTime {|time|
		case
		{(time < from) && ( time < (from + duration))} { ^false; }
		{(time >= from) && ( time <= (from + duration))} { ^true; }
		{(time > from) && ( time > (from + duration))} { ^false; };
	}

	printOn { |stream|
		stream << this.class.name << "[" << from << "; " << (from + duration) << "; " << function.def.sourceCode << "]";
	}
}

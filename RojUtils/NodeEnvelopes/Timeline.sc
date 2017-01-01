Timeline {
	var timeline;

	*new { ^super.new.init(); }

	init { timeline = Order.new(); }

	put { |time, item, duration, key|
		if(duration.isNil) { duration = 0; };
		if(key.isNil) { key = \default; };

		if(timeline[time].isNil, {
			timeline.put(time, Timebar(time, duration, key, item));
		},{
			var arr = timeline[time].asArray;
			arr = arr ++ Timebar(time, duration, key, item);
			timeline.put(time, arr);
		});
	}

	setEnd {|endTime|
		this.removeKeys(\timeline_end);
		this.put(endTime, nil, 0, \timeline_end);
	}

	atTime {|time|
		var items = List.new();
		timeline.indicesDo({|oneTime|
			oneTime.asArray.do({|oneTimebar|
				if(oneTimebar.isAtTime(time)) { items.add(oneTimebar.item); };
			});
		});
		case
		{items.size < 1 } { ^nil; }
		{items.size == 1 } { ^items[0]; }
		{items.size > 1 } { ^items.asArray; };
	}

	get {|time, key = nil|
		var oneTime = timeline[time];
		var items = List.new();
		oneTime.asArray.do({|oneTimebar|
			if(key.notNil,
				{ if(oneTimebar.key.asSymbol == key.asSymbol) { items.add(oneTimebar.item); }; },
				{ items.add(oneTimebar.item); }
			);
		});
		case
		{items.size < 1 } { ^nil; }
		{items.size == 1 } { ^items[0]; }
		{items.size > 1 } { ^items.asArray; }
	}

	removeKeys {|key = nil|
		var rest = List.new();
		if(key.notNil)
		{
			this.times.do({|oneTime|
				var arrTimebar = timeline[oneTime];
				arrTimebar.asArray.do({|oneTimebar|
					if(oneTimebar.key.asSymbol != key.asSymbol) { rest.add(oneTimebar); }
				});
			});
		};

		timeline = Order.new();
		rest.do({|oneRest| this.put(oneRest.from, oneRest.item, oneRest.duration, oneRest.key); });
	}

	array {
		var items = List.new();
		this.times.do({|oneTime|
			var arrTimebar = timeline[oneTime];
			arrTimebar.asArray.do({|oneTimebar|
				items.add([oneTimebar.from, oneTimebar.item, oneTimebar.duration, oneTimebar.key]);
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
			oneArray.asArray.do({|item| txt = txt ++ "\n\t" ++ tabs ++ "-" + item; });
			txt = txt ++ "\n";
		});
		txt.postln;
	}

	play {|function = nil, startTime = 0|  // -> example of function -> {|item| item.postln; };
		// var timeToQuant = 0;
		var clock = TempoClock.default;
		if(currentEnvironment[\tempo].notNil) { clock = currentEnvironment.clock };
		if(function.isNil) { function = {|item| item.postln }};
		// if(startQuant > 0) { timeToQuant = clock.timeToNextBeat(startQuant) };

		timeline.array.do({|oneTime, no|
			oneTime.asArray.do({|oneTimebar|
				// ("oneTimebar" + oneTimebar).postln;
				// ("at % -> item: %").format(oneTimebar.from, oneTimebar.item).postln;
				if(oneTimebar.key.asSymbol != \timeline_end)
				{
					if((oneTimebar.from >= startTime))
					{
						clock.sched((oneTimebar.from - startTime), {
							function.value(oneTimebar.item);
							nil;
						});
					};
				};
			});
		})
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

	items { |function = nil| // -> example of function -> timline.items({|time, duration, item, key| ... });
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
			function.value(time, duration, item, key);
		});
	}
}

Timebar {
	var <from, <>duration;
	var <key;
	var <item;

	*new {|from, duration, key, item| ^super.newCopyArgs(from, duration, key.asSymbol, item); }

	isAtTime {|time|
		case
		{(time < from) && ( time < (from + duration))} { ^false; }
		{(time >= from) && ( time <= (from + duration))} { ^true; }
		{(time > from) && ( time > (from + duration))} { ^false; };
	}

	printOn { |stream|
		stream << this.class.name << "[" << from << "; " << (from + duration) << "; " << key << "; " << item << "]";
	}
}
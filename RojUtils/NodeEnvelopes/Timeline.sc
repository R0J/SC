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
				items.add([oneTimebar.from, oneTimebar.item]);
			});
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

	play {|function = nil, startQuant = 0|  // -> example of function -> {|item| item.postln; };
		var clock = TempoClock.default;
		var timeToQuant = 0;
		if(currentEnvironment[\tempo].notNil) { clock = currentEnvironment.clock };
		if(function.isNil) { function = {|item| item.postln }};
		if(startQuant > 0) { timeToQuant = clock.timeToNextBeat(startQuant) };

		timeline.array.do({|oneTime, no|
			oneTime.asArray.do({|oneTimebar|
				// ("oneTimebar" + oneTimebar).postln;
				// ("at % -> item: %").format(oneTimebar.from, oneTimebar.item).postln;
				clock.sched((oneTimebar.from + timeToQuant), {
					function.value(oneTimebar.item);
					nil;
				});
			});
		})
	}
}

Timebar {
	var <from, <duration;
	var <key;
	var <item;

	*new {|from, duration, key, item| ^super.newCopyArgs(from, duration, key, item); }

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
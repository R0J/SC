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
				if(oneTimebar.isAtTime(time)) { items.add( oneTimebar.item); };
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

	take {|time, key = nil|
		var oneTime = timeline[time];
		var items = List.new();
		var rest = List.new();
		oneTime.asArray.do({|oneTimebar|
			if(key.notNil,
				{
					if((oneTimebar.key.asSymbol == key.asSymbol),
						{ items.add(oneTimebar.item); },
						{ rest.add(oneTimebar); }
					);
				},
				{ items.add(oneTimebar.item); }
			);
		});

		case
		{rest.isEmpty} { timeline.removeAt(time); }
		{rest.notEmpty} { timeline.put(time, rest); };

		case
		{items.size < 1 } { ^nil; }
		{items.size == 1 } { ^items[0]; }
		{items.size > 1 } { ^items.asArray; };
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
			txt = txt ++ "\n" ++ tabs ++ "- time" + oneTime;
			oneArray.asArray.do({|item| txt = txt ++ "\n\t" ++ tabs ++ "-" + item; });
		});
		txt.postln;
	}

	play{ |clock, function|
		timeline.indicesDo({|oneArray, oneTime|
			oneArray.asArray.do({|item|
				clock.sched(item.from, {item;} );
			});
		});
		clock.sched(this.duration, { clock.stop; "clock stoped".postln; });
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
		stream << this.class.name << "[" << from << "; " << duration << "; " << key << "; " << item << "]";
	}
}
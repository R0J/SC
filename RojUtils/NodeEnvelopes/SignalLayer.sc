SignalLayer[slot] {

	classvar controlRate;

	var slot;
	var dur, size;
	var fnc;
	var sig;

	*new { ^super.new.init }

	*initClass { controlRate = 44100 / 64 }

	init {
		slot = 0;
		dur = Order();
		size = Order();
		sig = Order();
	}
	at { |index| slot = index }

	duration_ {|time|
		dur.put(slot, time);
		size.put(slot, time * controlRate);
	}
	duration { ^dur.at(slot) }
	frame { ^size.at(slot) }

	signal_ {|data| sig.put(slot, data) }
	signal { ^sig.at(slot) }

	env { |levels = #[0,1,0], times = #[0.15,0.85], curves = #[5,-3]|
		var envelope = Env(levels, times, curves);
		this.duration = envelope.duration;
		this.signal = envelope.asSignal(this.frame);
	}

	printOn { |stream|	stream << this.class.name << "(id: " << slot << " | dur: " << this.duration << ")"; }

}
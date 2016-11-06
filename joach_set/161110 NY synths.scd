(

SynthDef(\kick, { |vol = 1, amp = 0, freq = 63, fplus = 0, noise = 0.6|
	var num = 4;
	var sig = Mix.fill(num,{|i|
		SinOsc.ar(
			freq: freq + fplus,
			phase: amp**(200+(i/200))*8pi) + BrownNoise.ar(noise)
	});
	sig = sig * amp * vol;
	Out.ar(0, Splay.ar(sig, 0, center:\stereo.kr(0)));
}, [8]).add;


SynthDef(\lpf, { |bus, flpf = 600|
	var sig = In.ar(bus,2);
	ReplaceOut.ar(bus, LPF.ar(sig, flpf));
}).add;

)

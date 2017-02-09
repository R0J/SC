(
// INSTRUMENS ///////////////////////////////////////////

ProxySynthDef(\kick, { |amp = 0, freq = 63, fplus = 0, noise = 0|
	var bus = \out.kr;
	var num = 4;
	var sig = Mix.fill(num,{|i|
		SinOsc.ar(
			freq: freq!2 + fplus,
			// phase: amp**(200+(i/200))*8pi,
			mul: num.reciprocal
		)
	});
	var noiseGen = BrownNoise.ar(noise);
	Out.ar(bus, (sig + noiseGen) * amp);
}).store;

// FILTERS ///////////////////////////////////////////
// \out.kr, \gate.kr, \fadeTime.kr - inner ControlNames of NodeProxy

ProxySynthDef(\lpf, { |lpf = 600|
	var bus = \out.kr;
	var sig = LPF.ar(In.ar(bus,2), lpf);
	// var fadeEnv = Line.kr(0,1,\fadeTime.kr, doneAction:0);
	// XOut.ar(bus, fadeEnv, LPF.ar( In.ar(bus,2), flpf) );
	ReplaceOut.ar(bus, sig);
}).store;

ProxySynthDef(\pan, { |pan = 0|
	var bus = \out.kr;
	var sig = Pan2.ar(In.ar(bus), pan);
	// var sig = Splay.ar(In.ar(bus,2), 0, center: stereo);
	ReplaceOut.ar(bus, sig);
}).store;

ProxySynthDef(\limiter, {
	var bus = \out.kr;
	var sig = Limiter.ar(In.ar(bus,2), 1, 0.1);
	ReplaceOut.ar(bus, sig);
}).store;

ProxySynthDef(\volume, { ReplaceOut.ar( \out.kr, In.ar(\out.kr,2) * \vol.kr(1)) }).store;

/*
SynthDef(\darkCloud, { |vol = 0.9, amp = 0, /*gate = 1,*/ lpf = 180, poleFreq = 0.9|
var env, tone;
var sig;
// env = EnvGen.ar(Env([0,1,1,0],[0.5,1,0.5],\step), gate, doneAction:2);
sig = LFSaw.ar([1,2,4,8],mul:amp) * LFSaw.ar([55,55.1,55.2,55.3]) * amp;
sig = OnePole.ar(sig,SinOsc.ar(0.1).range(-0.9,0.9));
sig = LPF.ar(sig,lpf);
sig = AllpassC.ar(sig,0.133,0.133,1);
sig = FreeVerb.ar(sig,0.133,1);

Out.ar(0, Splay.ar(sig));
}).store;
*/
)

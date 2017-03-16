({ var n = PinkNoise.ar(), f = (0.01, 0.015 ..0.07);
v = LFTri.kr(f.scramble[5..7]).range(0.0001, 0.003);
Splay.ar( MembraneHexagon.ar(n, f.scramble[1..3], mul: v), SinOsc.kr(f.choose))
}.play)


(
ProxySynthDef(\hex, {
	arg  sustain,  amp, f;
	var n,v, sig, env;
	n = PinkNoise.ar();
	f = (0.01, 0.015 ..0.07);
	v = LFTri.kr(f.scramble[5..7]).range(0.0001, 0.003);

	env=EnvGen.kr(Env([0,1,0],[8,8],\lin),timeScale:sustain,doneAction:2);
	sig=Splay.ar( MembraneHexagon.ar(n, f.scramble[1..3], mul: v), SinOsc.kr(f.choose));
	sig=sig*env*amp;

}).add;
)


~hex.clear;
~hex.stop(freeTime:60);

(
~hex.play(vol:0.6, fadeTime:4, numChannels:2,);
~hex.quant_(16);
);
(~hex[0]=Pbind(
	\instrument, \hex,
	\amp, 0.5,
	\legato, Prand([0.1,0.5,0.7],inf),
	\dur, 16,
);
)
(
~hex[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf3.kr(200,2)),0.7)};
~hex[4]=\set->Pbind(\args, [\lpf3], \dur,16,\lpf3, Pseq([820,600,400,1200,400,600,820],inf));
~hex[5]=\filter->{arg in; GVerb.ar(in,[50,40],[8,6],mul:0.2,maxroomsize:200)+(in*2)};
)
~hex[5]=nil;
7
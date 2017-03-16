
ProxySynthDef(\kickfill,{
	arg  sustain,  amp, pan=0, top=40;
	var sig, env, fenv;

	env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,2.5],[3,-3],curve:'exp'),timeScale:sustain, doneAction:2);

	sig = SinOsc.ar(520!2);
	sig = Pan2.ar(sig * env*amp,pan);
	Splay.ar(sig,0.75,0.25);
}).add;


p = NetProxy.connect;
p.metro(4);


~kickg.clear;
~kickg.stop(freeTime:10);

~kickg.play(vol:0.8, fadeTime:1, numChannels:2).quant_(4);
(~kickg[0]=Pbind(
	\instrument, \kickfill,
	\amp, 1,
	\legato,0.2,
	\pan, 0,
	\freq, 195,
	\dur,4,

);)
~kickg[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,SinOsc.kr(1/32).range(420,620)),0.7)};
~kickg[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,2,mul:0.1)+in,0.7)};
~kick[3]=nil;



~tss8082.clear;
~tss8082.stop(fadeTime:40);
~tss8082.play(vol:0.4, fadeTime:1, numChannels:2).quant_(4);
(~tss8082[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([1,0.5,0.1,2],inf)*6,
	\dur,4,
	\legato, 0.4,
	\hpf,2000,

););
~tss8082[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,10,SinOsc.kr(1/64).range(0.8,1),mul:0.8,add:ts*2),0.9)};
~tss8082[7]=nil;

~tss8082[6]=nil;
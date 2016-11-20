(
ProxySynthDef(\melo, {
	arg  sustain,  amp,f;
	var sig, env, sig2;
	env=EnvGen.kr(Env([0,1,0],[0.01,0.1],\welch),timeScale:sustain,doneAction:2);
	f=LFSaw.kr(1/64).range(\freq.kr, \freq.kr+800);
	sig=SinOsc.ar(f!2,env*\top.kr(4));
	sig=CompanderD.ar(BPF.ar(sig,f,/*LFNoise0.kr(0.125).range(220,400)*/0.5),0.8,0.5);
	sig2=BPF.ar(GrayNoise.ar(0.7),500,2)*(env/4);
	sig=(sig+sig2)*amp*env;

}).add;
)


~test1.clear;
~test1.stop(fadeTime:40);

(
~test1.quant_(4);
~test1.play(vol:0.9, fadeTime:4, numChannels:2);)
(~test1[0]=Pbind(
	\instrument, \melo,
	\amp,Pseq([Prand([10,[0.5!2,0.9,0.3!2,0,0.8,0.4!2].flatten,10].convertRhythm),
		Pseq([10,[0.5!2,0.9,0.3!2,0.2,0,0.4!2].flatten,1].convertRhythm),
		Pseq([10,[0.5!2,0.5,0.3!2,0.2,0,0.4!2].flatten,1].convertRhythm)
	],inf),
	\freq,Pseq([Pn(200,4),Pn(400,4),Pn(45,4)],inf)-100,
	\dur, 0.25,
	\legato, 0.8,
	\top, 2,

);
~test1[4]=\filter->{arg in; in*Decay2.kr(Impulse.kr(1/2),8,0.01)};
~test1[6]=\filter->{arg in; GVerb.ar(in,roomsize:80,revtime:0.5)*Decay2.kr(Impulse.kr(1/4),8,0.01)};
~test1[7]=\filter->{arg in; GVerb.ar(in,roomsize:60,revtime:2,earlyreflevel:0.8,mul:SinOsc.kr(1/32).range(0.1,0.3))+(in*0.5)};
~test1[8]=\filter->{arg in; HPF.ar(in,\lpf10.kr(400))};
~test1[9]=\filter->{arg in; Limiter.ar(in,0.9)};

);
~test1[6]=nil;

p.gui;
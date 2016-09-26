currentEnvironment.free;
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;

p.clock.tempo = 60/60;
p.gui;

AutoIDE.run;


ProxySynthDef(\tss, {
	arg  sustain,  amp, pan, hpf, noise;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,1,0],[0.01,0.1,0.01]),timeScale:sustain,doneAction:2);
	fenv=EnvGen.kr(Env([\freq.kr,\freq.kr*4,\freq.kr],[0.01,0.2],\welch),timeScale:sustain,doneAction:2);
	sig=WhiteNoise.ar(noise)+GrayNoise.ar(noise);
	sig=Splay.ar(sig);
	sig=Pan2.ar(HPF.ar(sig,hpf+fenv), pan);
	sig=sig*env*amp;
}).add;

ProxySynthDef(\piano1, { |out=0, freq=440, gate=1,sustain=1, amp=1|
	var sig = MdaPiano.ar(freq, gate,decay:sustain*0.5,release:sustain*0.4, release: 0.9, stereo: 0.3, sustain: 0);
	DetectSilence.ar(sig, 0.01, doneAction:2);
	Out.ar(out, sig * 0.1);
}).add;



//////////////////////////////piano stuff
~piano1.clear;
~piano1.stop(fadeTime:40);
~piano1.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~piano1[0]=Pbind(
	\instrument, \piano1,
	\amp,1,
	\octave, Pseq([[7,5],6,4,2,5],inf),
	\degree, Pseq([4,2,1],inf), //or rand
	\dur,Pseq([Pseq([4,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm)],inf),
	\noise, 0.03,
	\legato, 0.3,

););
~piano1[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[180,120],20,mul:1,add:(-1)*in),0.9)};
~piano1[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/16).range(1800,740))};
//~piano1[8]=\filter->{arg in; HPF.ar(in,12000)};
~piano1[8]=nil;


~piano2.clear;
~piano2.stop(fadeTime:40);
~piano2.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~piano2[0]=Pbind(
	\instrument, \piano1,
	\amp,1,
	\octave, Pseq([[7,5],6,4,2,5],inf),
	\degree, Pseq([4,2,1],inf), //or rand
	\dur,Pseq([Pseq([4,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm)],inf),
	\noise, 0.03,
	\legato, 0.3,

););
~piano2[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[90,80],20,mul:0.1),0.9)};
~piano2[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/16).range(1800,740))};
~piano2[7]=nil;


~bum1.clear;
~bum1.stop(fadeTime:40);
~bum1.play(vol:0.9, fadeTime:4, numChannels:2).quant_(4);
(~bum1[0]=Pbind(
	\instrument, \bum,
	\amp,1,
	\freq, [44,45],
	\dur,1,
	\noise, 0.03,
	\legato, 0.3,

););
~bum1[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,20,1,mul:0.1,add:in*0.9),0.9)};
~bum1[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,240))};
~bum1[6]=nil;



~ts1.clear;
~ts1.stop(fadeTime:40);
~ts1.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~ts1[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, 1200+Prand([1000,200,400,(-500),400],inf),
	\dur, 1,
	\legato, 0.2,
	\pan, 0,//Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 1000,

););
~ts1[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/32).range(2000,14000))};
~ts1[7]=nil;


~kick.clear;
~kick.stop(fadeTime:40);
~kick.play(vol:0.9, fadeTime:4, numChannels:2).quant_(4);
(~kick[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, [44,45],
	\dur,1,
	\top, 40,
	\legato, 0.8,

););
~kick[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.5),0.6)};
~kick[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,120))};
~kick[6]=nil;


/////////////////////////////////////////


~ts1.clear;
~ts1.stop(fadeTime:40);
~ts1.play(vol:0.1, fadeTime:4, numChannels:2).quant_(4);
(~ts1[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, 50,
	\dur, Pseq([Pseq([4,[0.25,-0.25,0.5,-0.5,0.5,-2].flatten,1].convertRhythm)
	],inf),
	\legato, 0.5,
	\pan,0,// Pseq([-1,1],inf),
	\noise, Prand([0.6,0.2,0.1,0.7],inf),
	\hpf, 11000,
););
~ts1[8]=\filter->{arg in; CombN.ar(in,1,0.8,2)};
~ts1[8]=nil;


~kick2.clear;
~kick2.stop(fadeTime:40);
~kick2.play(vol:0.1, fadeTime:4, numChannels:2).quant_(4);
(~kick2[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\octave, Pseq([[3,1]!8,[1,2]].flatten,inf),
	\degree, [2,4,8],
	\dur, Pseq([Pseq([4,[0.25!2,-0.5,0.5,0.5,-2].flatten,1].convertRhythm)
	],inf),
	\top, 20,
	\legato, 0.2,

););
~kick2[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:SinOsc.kr(1/32).range(0.01,0.2),add:in*0.5),0.6)};
~kick2[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,120))};
~kick2[7]=nil;




~kick.clear;
~kick.stop(fadeTime:40);
~kick.play(vol:0.9, fadeTime:4, numChannels:2).quant_(4);
(~kick[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, [44,45],
	\dur, Pseq([Pseq([4,[0.25!2,-1,0.5,-2].flatten,1].convertRhythm)!4,
		//Pseq([4,[0.25!4,-1,0.5,-2].flatten,1].convertRhythm)
			].flatten	,inf),
	\top, 40,
	\legato, 0.7,

););
~kick[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.5),0.6)};
~kick[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,120))};
~kick[6]=nil;


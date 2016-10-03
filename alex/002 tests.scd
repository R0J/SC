currentEnvironment.free;
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;

p.clock.tempo = 60/60;
p.gui;

AutoIDE.run;

(

ProxySynthDef(\ping, {
	arg  sustain,  amp;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,0],[0.01,0.3,0.01]),timeScale:sustain,doneAction:2);
		sig=SinOsc.ar(\freq.kr!2);
	sig=sig*env*amp;
}).add;

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

ProxySynthDef(\piano, { |out=0, freq=440, gate=1,sustain=1, amp=1|
	var sig = MdaPiano.ar(freq, gate,decay:sustain*0.5,release:sustain*0.4, release: 0.9, stereo: 0.3, sustain: 0);
	DetectSilence.ar(sig, 0.01, doneAction:2);
	Out.ar(out, sig * 0.1);
}).add;

)

//////////////////////////////piano stuff
f=Group.new;
~piano1.clear;
~piano1.stop(fadeTime:10);

~piano1.play(vol:0.1, fadeTime:4, numChannels:2, group:f).quant_(4);

(~piano1[0]=Pbind(
	\instrument, \piano1,
	\amp,1,
	\octave, Pseq([[7,5],6,4,2,5],inf),
	\degree, Pseq([4,2,1],inf),
	\dur,Pseq([Pseq([2,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm),
		Pseq([8,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm),
		Pseq([4,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm),
		Pseq([32,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm)
	],inf),
	\legato, 0.3,

);)
~piano1[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[180,120],20,mul:0.9,add:(-1)*in),0.9)};
~piano1[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,220))};
~piano1[6]=nil;



~piano2.clear;
~piano2.stop(fadeTime:40);
~piano2.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
(~piano2[0]=Pbind(
	\instrument, \piano1,
	\amp,1,
	\octave, Pseq([[3,5],6,3,2,5],inf),
	\degree, Pseq([4,2,1],inf), //or rand
	\dur,Pseq([Pseq([16,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm)],inf),
	\legato, 0.3,

););
~piano2[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[90,80],20,mul:0.1),0.9)};
~piano2[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/16).range(1800,440))};
~piano2[7]=nil;



~piano4.clear;
~piano4.stop(fadeTime:40);
(~piano4[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, 8,
	\degree, [6,2,8],
	\dur,Pseq([Pseq([1,[1,-1,1,-1],1].convertRhythm)],inf),
	\legato, 0.5,

););
~piano4[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[90,80],2,mul:0.1),0.9)};
~piano4[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/16).range(1800,440))};
~piano4[7]=nil;


~piano5.clear;
~piano5.stop(fadeTime:40);
(~piano5[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, 5,
	\degree, [1,3,4],
	\dur,Pseq([Pseq([1,[1,1,-1,1],1].convertRhythm)],inf),
	\legato, 0.5,

););

~piano5[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[90,80],2,mul:0.1),0.9)};
~piano5[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/16).range(1800,440))};
~piano5[7]=nil;
(
~piano5.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
~piano4.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
)

////////////////////////SHORT BURSTS
~dis4.clear;
~dis4.stop(fadeTime:6);
~dis4.play(vol:0.5, fadeTime:2, numChannels:2).quant_(2);
(
~dis4[0]={
	var sig, env, fenv, noise;

	noise=EnvGen.kr(Env([0.01,0.05,0.09,0.01],[0.1,0.02,0.1],curve:\welch),\trig.tr);
	env=EnvGen.kr(Env(times:[1.4,0.01],curve:\welch),\trig.tr);
	fenv=EnvGen.kr(Env([\freq.kr,\freq.kr*6,\freq.kr,\freq.kr+(fib(10)*2)], [0.001, 0.04,0.8] ,curve:\welch),\trig.tr);

	sig=LFSaw.ar(fenv+fib(10)).lag([0.01,0.02,0.03,0.04,0.05]);
	sig=(sig+ClipNoise.ar(noise*0.8))*env;
};
)

~dis4[1]=\set-> Pbind(\args, [\trig], \dur,Prand([2, Rest(16)],inf), \trig, 1);
(~dis4[2]=\set-> Pbind(\args, [\freq], \dur, 4, \octave, Pseq([
	Pseq([[4,5],5,[6,4],[4,3][5,7],[6,2],Prand([[6,5],[7,6]],1)],inf),
	Pseq([[4,2],[5,3]],inf)]+3,1),
\degree, [4,6,8]);)
~dis4[29]=nil;
~dis4[29]=\filter-> {arg in; CombN.ar(in,1,0.01)};
~dis4[30]=\filter-> {arg in; GVerb.ar(Limiter.ar(in,0.1), [120,140],[8,12,4],earlyreflevel:0.2,mul:0.2)};
~dis4[33]=\filter->{arg in; LPF.ar(in,\lpf.kr(900))};
(~dis4[35]=\set->Pbind(\args, [\lpf], \dur, 16, \lpf, Pseq([Pseq([4000,3400,2800,2000,1200],60),
	Pseq([800,900,700],inf)],1);)
);
~dis4[36]=\filter->{arg in; Limiter.ar(in,0.3)};



~bum1.clear;
~bum1.stop(fadeTime:40);
~bum1.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~bum1[0]=Pbind(
	\instrument, \bum,
	\amp,1,
	\freq, [44,45],
	\dur,Prand([0.5,0.25],inf),
	\noise, 0.9,
	\legato, 0.1,

););
~bum1[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,18,1,mul:0.1,add:in*2),0.9)};
~bum1[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(180,240))};
~bum1[6]=nil;


~m.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(
~m[0]=Pbind(\instrument, \onion,
	\dur, 8,
	\sustain,4,
	\freq, 65,
	\amp, 0.2,
	\fsaw, Prand([2,4,10,80,60],inf),
);)
~m[30]=\filter->{arg in; GVerb.ar(in,[80,60],[4,3],maxroomsize:120,add:in)};





~ts1.clear;
~ts1.stop(fadeTime:40);
~ts1.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~ts1[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, 1200+Prand([1000,200,400,(-500),400],inf),
	\dur, 0.25,
	\legato, 0.2,
	\pan, 0,//Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 1800,

););
~ts1[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/8).range(2000,14000))};
~ts1[7]=nil;


~tshi.clear;
~tshi.stop(fadeTime:40);
~tshi.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~tshi[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, 200+Pseq([1000,200],inf),
	\dur, 0.2,
	\legato, 0.1,
	\pan, 0,//Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 800,

););
~tshi[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/8).range(2000,4000))};
~tshi[7]=nil;


~kick.clear;
~kick.stop(fadeTime:40);
~kick.play(vol:0.9, fadeTime:4, numChannels:2).quant_(4);
(~kick[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, [44,45],
	\dur,Pseq([Pseq([2,[0.25,-0.25,0.5,-0.5,0.5,-2].flatten,1].convertRhythm)],inf),
	\top, 70,
	\legato, 0.6,

););
~kick[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.5),0.6)};
~kick[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,520))};
~kick[6]=nil;



~kickf.clear;
~kickf.stop(fadeTime:40);
~kickf.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~kickf[0]=Pbind(
	\instrument, \kickfill,
	\amp,1,
	\freq, [46,48],
	\dur,2,
	\legato, 0.4,

););
~kickf[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.5),0.6)};
~kickf[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,220))};
~kickf[6]=nil;

/////////////////////////////////////////


~ts1.clear;
~ts1.stop(fadeTime:40);
~ts1.play(vol:0.1, fadeTime:4, numChannels:2).quant_(4);
(~ts1[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, 500,
	\dur, Pseq([Pseq([4,[0.25!2,-0.5,0.5,-0.5,-2].flatten,1].convertRhythm)
	],inf),

	\legato, 0.2,
	\pan,0,// Pseq([-1,1],inf),
	\noise, Prand([0.6,0.2,0.1,0.7],inf),
	\hpf, 9000,
););
~ts1[8]=\filter->{arg in; CombN.ar(in,1,0.8,2)};
~ts1[8]=nil;


//////////////////////nice rhm these two
~kick2.clear;
~kick2.stop(fadeTime:40);
~kick2.play(vol:0.8, fadeTime:4, numChannels:2).quant_(4);
(~kick2[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, 45,
	\dur, Pseq([Pseq([4,[0.25!2,-0.5,0.5,0.5,-2].flatten,1].convertRhythm)
	],inf),
	\top, 40,
	\legato, 0.8,

););
~kick2[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:SinOsc.kr(1/32).range(0.01,0.2),add:in*0.5),0.6)};
~kick2[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,120))};
~kick2[7]=nil;




~kick.clear;
~kick.stop(fadeTime:40);
~kick.play(vol:0.9, fadeTime:4, numChannels:2).quant_(4);
(~kick[0]=Pbind(
	\instrument, \kickfill,
	\amp,1,
	\freq, [44,45],
	\dur, Pseq([Pseq([4,[0.25!2,-1,0.5,-2].flatten,1].convertRhythm)!4,
		//Pseq([4,[0.25!4,-1,0.5,-2].flatten,1].convertRhythm)
			].flatten	,inf),
	\top, 40,
	\legato, 0.2,

););
~kick[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.5),0.6)};
~kick[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,120))};
~kick[6]=nil;


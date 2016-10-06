currentEnvironment.free;
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;

p.clock.tempo = 120/60;
p.gui;

AutoIDE.run;


(
ProxySynthDef(\ping, {
	arg  sustain,  amp;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,0],[0.01,0.3,0.01]),timeScale:sustain,doneAction:2);
	fenv=EnvGen.kr(Env([\freq.kr,\freq.kr*\top.kr,\freq.kr],[0.01,0.2],\welch),timeScale:sustain,doneAction:2);
	sig=SinOsc.ar(fenv!2);
	sig=sig*env*amp;
}).add;

)

/////////////2 to 3

~ping2.clear;
~ping2.stop(fadeTime:40);
(~ping2[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, 2,
	\degree, [6,2,8],
	\dur,Pseq([Pseq([1,[1,-1,1,-1],1].convertRhythm)],inf),
	\legato, 0.5,
	\top, 4,

););
~ping2[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[20,40],2,mul:0.1),0.9)};
~ping2[7]=nil;


~ping3.clear;
~ping3.stop(fadeTime:40);
(~ping3[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, 4,
	\degree, [1,3,4],
	\dur,Pseq([Pseq([1,[1,1,-1,1],1].convertRhythm)],inf),
	\legato, 0.5,
	\top, 4,


););

~ping3[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[30,32],2,mul:0.5),0.9)};
~ping3[7]=nil;
(
~ping3.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
~ping2.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
)



/////////////////////////////// 3 to 4

~ping34.clear;
~ping34.stop(fadeTime:40);
(~ping34[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, [2,3],
	\degree, [6,2,8],
	\dur,Pseq([Pseq([4,[1,1,1,1],1].convertRhythm)],inf),
	\legato, 0.7,
	\top, 4,


););
~ping34[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[20,40],2,mul:0.1),0.9)};
~ping34[7]=nil;


~ping4.clear;
~ping4.stop(fadeTime:40);
(~ping4[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, [4,6],
	\degree, [1,3,4],
	\dur,Pseq([Pseq([4,[1,1.25,1.5,-0.25],1].convertRhythm)],inf),
	\legato, 0.5,
	\top, 1,


););

~ping4[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[30,32],2,mul:0.5),0.9)};
~ping4[7]=nil;
(
~ping34.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
~ping4.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
)



/////////////////////////////// 5 to 4
(
~ping5.clear;
~ping45.clear;
)
~ping45.stop(fadeTime:40);
(~ping45[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, [3,2],
	\degree, [6,2],
	\dur,Pseq([Pseq([4,[4!4].flatten,1].convertRhythm)],inf),
	\legato, 0.5,
	\top, 4,


););
~ping45[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[20,40],2,mul:0.1),0.9)};
~ping45[7]=nil;


~ping5.stop(fadeTime:40);
(~ping5[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, [4,6],
	\degree, [8,2],
	\dur,Pseq([Pseq([4,[0.8,0.8,0.8,0.8,0.8,-0.8],1].convertRhythm)],inf),
	\legato, 0.5,
	\top, 1,


););

~ping5[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[30,32],2,mul:0.5),0.9)};
~ping5[7]=nil;
(
~ping5.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
~ping45.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
)



(
~t[0]={
	var a,b;
	a=2;
	b=3;

~ping45.stop(fadeTime:40);
(~ping45[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, [3,2],
	\degree, [6,2],
	\dur,Pseq([Pseq([a,[a!a].flatten,1].convertRhythm)],inf),
	\legato, 0.5,
	\top, 4,


););
~ping45[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[20,40],2,mul:0.1),0.9)};


~ping5.stop(fadeTime:40);
(~ping5[0]=Pbind(
	\instrument, \ping,
	\amp,1,
	\octave, [4,6],
	\degree, [8,2],
		\dur,Pseq([Pseq([a,[(a/b)!(b-1),(-1)*a/b, a/b].flatten,1].convertRhythm)],inf),
	\legato, 0.5,
	\top, 1,


););

~ping5[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[30,32],2,mul:0.5),0.9)};

})

~t.play;
~t.clear;
(
~ping5.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
~ping45.play(vol:0.3, fadeTime:4, numChannels:2).quant_(4);
)
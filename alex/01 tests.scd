currentEnvironment.free;
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;

p.clock.tempo = 127/60;
p.gui;

AutoIDE.run;




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


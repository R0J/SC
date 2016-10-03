currentEnvironment.free;
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;

p.clock.tempo = 127/60;
p.gui;

AutoIDE.run;

~m.play(vol:0.7, fadeTime:4, numChannels:2).quant_(4);
(
~m[0]=Pbind(\instrument, \onion,
	\dur, Prand([16,32],inf),
	\sustain,1,
	\freq, 65,
	\amp, 0.2,
	\fsaw, Prand([2,4,10,80,60],inf),
);)
~m[30]=\filter->{arg in; GVerb.ar(in,[80,60],[4,3],maxroomsize:120,add:in)};


g=Group.new;
~comb2.clear;
~comb2.stop(freeTime:60);
~comb2.play(vol:0.2, fadeTime:4, numChannels:2, group:g).quant_(4);
(~comb2[0]=Pbind(
	\instrument, \string,
	\amp, 1,
	\legato, 1.2,
	\pan, 0,
	//\octave,Pseq([[5,3],[3,4]],inf),
	//\degree,1,
	\freq, Pseq([Pseq([3200,[0.6!2,-0.5,0.4,0.8,0.6,-1].flatten,4].convertRhythm),
		Pseq([1600,[0.6!2,-0.5,0.4,0.8,0.6,-1].flatten,4].convertRhythm)	],inf),
	\dur,Pseq([Pseq([16,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm),
		Pseq([32,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm)
	],inf),

);)
(
~comb2[3]=\filter->{arg in; HPF.ar(in,60)};
~comb2[6]=\filter->{arg in; LPF.ar(in,\lpfc.kr(800,2))};
~comb2[7]=\set->Pbind(\args, [\lpfc], \dur, 8, \lpfc, Pseq([280,420,400,440,480,800,620],inf));
~comb2[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[40,60],[6,8],mul:0.2)+in,0.2)};
)
~comb2[9]=nil;

~tshi.clear;
~tshi.stop(fadeTime:40);
~tshi.play(vol:0.1, fadeTime:4, numChannels:2).quant_(4);
(~tshi[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, 200+Pseq([1000,200],inf),
	\dur,2,
	\legato, 0.1,
	\pan, Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 8000,

););
~tshi[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/8).range(2000,4000))};
~tshi[7]=nil;



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


currentEnvironment.free;
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;

p.clock.tempo = 127/60;
p.gui;

AutoIDE.run;

~imp.clear;
~imp.stop(freeTime:60);
~imp.play(vol:0.1, fadeTime:4, numChannels:2, group:t).quant_(4);
(~imp[0]=Pbind(
	\instrument, \imp,
	\amp, 2,
	\legato, Pseq([0.1,0.2],inf),
	\den,26,
	\hpf,280,
	\dur, 32,
);
t=Group.new;
)
~imp[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(800,2)),0.7)};
~imp[4]=\set->Pbind(\args, [\lpf4], \dur,4,\lpf4, Pseq([820,600,400,1200,400,600,820]+200,inf));
~imp[5]=\filter->{arg in; GVerb.ar(in,[180,120],[4,6],mul:0.8,maxroomsize:200)+in};
~imp[5]=nil;


~tsn.clear;
~tsn.stop(fadeTime:40);
~tsn.play(vol:0.002, fadeTime:4, numChannels:2).quant_(4);
(~tsn[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, Pseq([1000,200,400,400],inf),
	\dur, 0.25,
	\legato, Pseq([0.02,0.05],inf),
	\pan, 0,//Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 8000,

););
~tsn[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/8).range(1200,14000))};
~tsn[7]=\filter->{arg in; FreeVerb.ar(in,room:0.2)};



~bum1.clear;
~bum1.stop(fadeTime:40);
~bum1.play(vol:0.05, fadeTime:4, numChannels:2).quant_(4);
(~bum1[0]=Pbind(
	\instrument, \bum,
	\amp,1,
	\freq, [44,45]+Pseq([Pseries(20,1,40),Pseries(120,-1,40)],inf),
	\dur,Prand([0.5,0.25],inf),
	\noise, 0.9,
	\legato, 0.1,

););
~bum1[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,10,0.5,mul:0.1,add:in*2),0.9)};
~bum1[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(880,240))};
~bum1[6]=nil;


~tsh.clear;
~tsh.stop(fadeTime:40);
~tsh.play(vol:0.1, fadeTime:4, numChannels:2).quant_(4);
(~tsh[0]=Pbind(
	\instrument, \tss,
	\amp,0.5,
	\freq, 1000+Pseq([1000,200,400,400],inf),
	\dur, 0.25,
	\legato, Pseq([0.02,0.05],inf),
	\pan, Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 8000,

););
~tsh[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/16).range(12000,14000))};


~kick.clear;
~kick.stop(freeTime:10);
~kick.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~kick[0]=Pbind(
	\instrument, \kick,
	\amp, 1,
	\legato, 0.7,
	\pan, Pseq([-1,1],inf),
	\freq, [45,47]+10,
	\dur, 1,
);)
~kick[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(120,2)),0.7)};
~kick[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[2,1],mul:0.2)+in,0.7)};




~kickf.clear;
~kickf.stop(fadeTime:40);
~kickf.play(vol:0.05, fadeTime:4, numChannels:2).quant_(4);
(~kickf[0]=Pbind(
	\instrument, \kickfill,
	\amp,1,
	\freq, [46,48],
	\dur,2,
	\legato, 0.5,

););
~kickf[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.5),0.6)};
~kickf[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,220))};
~kickf[6]=nil;



~ts1.clear;
~ts1.stop(fadeTime:40);
~ts1.play(vol:0.01, fadeTime:4, numChannels:2).quant_(4);
(~ts1[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, 1200+Prand([1000,200,400,(-500),400],inf),
	\dur, Pseq([Pseq([4,[0.25!2,-0.5,0.5,0.5,-2].flatten,1].convertRhythm)
	],inf),
	\legato, Pseq([0.1,0.4],inf),
	\pan, 0,//Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 12000,

););
~ts1[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/8).range(12000,14000))};
~ts1[7]=nil;


//////////////////////nice rhm these two
~kick2.clear;
~kick2.stop(fadeTime:40);
~kick2.play(vol:0.5, fadeTime:4, numChannels:2).quant_(4);
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
~kick2[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,420))};
~kick2[7]=nil;




~kick3.clear;
~kick3.stop(fadeTime:4);
~kick3.play(vol:0.1, fadeTime:4, numChannels:2).quant_(4);
(~kick3[0]=Pbind(
	\instrument, \kickfill,
	\amp,1,
	\freq, [44,45],
	\dur, Pseq([Pseq([4,[0.25!2,-1,0.5,-2].flatten,1].convertRhythm)!4,
		//Pseq([4,[0.25!4,-1,0.5,-2].flatten,1].convertRhythm)
	].flatten	,inf),
	\top, 40,
	\legato, 0.2,

););
~kick3[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.5),0.6)};
~kick3[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,220))};
~kick3[6]=nil;


~ts1.clear;
~ts1.stop(fadeTime:40);
~ts1.play(vol:0.1, fadeTime:4, numChannels:2).quant_(4);
(~ts1[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, 1200+Prand([1000,200,400,(-500),400],inf),
	\dur, Pseq([Pseq([4,[0.25!2,-0.5,0.5,0.5,-2].flatten,1].convertRhythm)
	],inf),
	\legato, Pseq([0.1,0.4],inf),
	\pan, 0,//Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 12000,

););
~ts1[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/8).range(9000,14000))};
~ts1[7]=\filter->{arg in; FreeVerb.ar(in)};

~ts1[7]=nil;

/////////////////////////////////////////


~comb3.clear;
~comb3.stop(freeTime:60);
~comb3.play(vol:0.01, fadeTime:4, numChannels:2).quant_(4);
(~comb3[0]=Pbind(
	\instrument, \string,
	\amp, 1,
	\legato, 1.7,
	\pan, 0,
	\freq,Pseq([Pseq([800,[0.6!2,0.2,0.6,0.8].flatten,4].convertRhythm),],inf),
	\dur,Prand([8,4],inf),//Pseq([Pseq([8,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm)],inf),

);)
(
~comb3[3]=\filter->{arg in; HPF.ar(in,60)};
~comb3[6]=\filter->{arg in; LPF.ar(in,\lpfc.kr(240,2))};
~comb3[7]=\set->Pbind(\args, [\lpfc], \dur, 8, \lpfc, Pseq([280,420,400,440,480]-100,inf));
~comb3[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in.softclip),[40,60],[6,8],mul:0.2)+in.softclip,0.2)};
)
~comb3[9]=nil;

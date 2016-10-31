currentEnvironment.free;
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;

p.clock.tempo = 127/60;
p.gui;

AutoIDE.run;


///////////////////////////////////////////////////////////// bow long


~bow1.clear;
~bow1.play(vol:0.01, fadeTime:4, numChannels:2);
~bow1.quant_(4)
(
~bow1[0]=Pbind(
	\instrument, \bow,
	\dur, Pseq([6,Rest(2)],inf),
	\legato, 2,
	\octave, Pseq([[3,1],[4,1],[2,3],[1,2],[4,2]],inf),
	\degree, [2,1],
	\amp, 0.8,
);)
(
~bow1[3]=\filter->{arg in; LPF.ar(in,\lpf6.kr(400))+(in*0.01)};
~bow1[7]=\set->Pbind(\args, [\lpf6], \dur,12, \lpf6, Pseq([600,800,200],inf)+200);


~bow1[30]=\filter->{arg in; GVerb.ar(in,[80,60],[6,4],maxroomsize:120,add:in,mul:0.2)+in};
~bow1[32]=\filter->{arg in; Limiter.ar(in,0.7)};
)

///////////////////////////////////////////////////////////// bow short


~bow2.clear;
~bow2.play(vol:0.04, fadeTime:4, numChannels:2);
~bow2.quant_(4);
(
~bow2[0]=Pbind(
	\instrument, \bow,
	\dur,0.5,
	\sustain,0.4,
	\octave, Pseq([Pn([1,3],16),Pn([2,3],16)],inf),
	\degree, [2,1],
	\amp, 0.4,
);)
(
~bow2[3]=\filter->{arg in; LPF.ar(in,\lpf6.kr(400))+(in*0.01)};
~bow2[7]=\set->Pbind(\args, [\lpf6], \dur,12, \lpf6, Pseq([600,800,200],inf));


~bow2[30]=\filter->{arg in; GVerb.ar(in,[80,60],[6,4],maxroomsize:120,add:in,mul:0.2)+in};
~bow2[32]=\filter->{arg in; Limiter.ar(in,0.7)};
)

///////////////////////////////////////////////////////////// long tss
~v.clear;
~v.play(vol:0.01, fadeTime:4, numChannels:2);
~v.quant_(4);
(
~v[0]=Pbind(\instrument, \tss,
	\dur, Prand([0.05!4,Rest(8)].flatten,inf),
	\sustain,0.2,
	\freq, 1850,
	\amp, 0.8,
	\noise, 0.5,
	\hpf,80,
);
~v[30]=\filter->{arg in; GVerb.ar(in,[80,60],[24,23],maxroomsize:120,add:in)};)

////////////////////////////////////////////////////////////////////////////////// aural

~aural1.clear;
~aural1.stop(fadeTime:60);
~aural1.play(vol:0.01, fadeTime:4, numChannels:2);
~aurall.quant_(4);
(~aural1[0]=Pbind(
	\instrument, \aural,
	\amp,Prand([0.8,0.4,0.6],inf)/8,
	\legato, 0.2,
	\octave, Prand([[5,6],[4,3],[5,4],[3,6]],inf)-1,
	\degree, Prand([[1,5],[4,6],[6,2],[2,4],[2,6],[8,2]],inf),
	\dur, Pseq([Pn(8,20),Pn(2,20),Pn(0.5,20)],inf),
);)
(
~aural1[5]=\filter->{arg in; GVerb.ar(in,[40,60],[18,14],mul:0.4)+in};
~aural1[6]=\filter->{arg in; LPF.ar(in,\lpf3.kr(2000,4))};
~aural1[7]=\set->Pbind(\args, [\lpf], \dur,1, \lpf, Pseq([1000,600,800,200],inf));)
// ~aural1[7]=nil;



//////////////////////////////////////////////////////////////////////////////////// onion

~m.clear;
~m.play(vol:0.06, fadeTime:4, numChannels:2);
~m.quant_(4);
(
~m[0]=Pbind(\instrument, \onion,
	\dur, Prand([16,32],inf)/2,
	\sustain,0.5,
	\freq, 65,
	\amp, 0.2,
	\fsaw, Prand([2,4,10,80,60],inf),
);
~m[30]=\filter->{arg in; GVerb.ar(in,[80,60],[4,3],maxroomsize:120,add:in)};)

//////////////////////////////////////////////////////////////////////////////////// imp


~imp.clear;
~imp.stop(freeTime:60);
(t=Group.new;
~imp.play(vol:0.9, fadeTime:4, numChannels:2, group:t)
~imp.quant_(4));
(~imp[0]=Pbind(
	\instrument, \imp,
	\amp, 3,
	\legato, Pseq([0.2,0.3],inf),
	\den,15,
	\dur, 16,
);


~imp[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(800,2)),0.7)};
~imp[4]=\set->Pbind(\args, [\lpf4], \dur,4,\lpf4, Pseq([820,600,400,1200,400,600,820]+200,inf));
~imp[5]=\filter->{arg in; GVerb.ar(in,[180,120],[4,6],mul:0.8,maxroomsize:200)+in};
)
~imp[5]=nil;

//////////////////////////////////////////////////////////////////////////////////////////////////////////quick tss

~tsn.clear;
~tsn.stop(fadeTime:40);
~tsn.play(vol:0.02, fadeTime:4, numChannels:2);
~tsn.quant_(4);
(~tsn[0]=Pbind(
	\instrument, \tss,
	\amp,Pseq([1,0.5],inf),
	\freq, Pseq([200,400,400],inf),
	\dur, 0.25,
	\legato, Pseq([0.02,0.05],inf),
	\pan, 0,//Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 4000,

);
~tsn[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/16).range(6200,14000))};
)
~tsn[7]=nil;

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////drum

~tum1.clear;
~tum1.stop(fadeTime:40);
(f=Group.new;
~tum1.play(vol:0.005, fadeTime:4, numChannels:2, group:f);
~tum1.quant_(4)
)
(~tum1[0]=Pbind(
	\instrument, \tum,
	\amp,1,
	\octave, Pseq([4!16,3!16].flatten,inf),
	\degree, [4,2],
	\dur, Pseq([Pseq([4,[-0.25,-0.75,-0.5,0.25!2,-2].flatten,1].convertRhythm),Rest(12)],inf),
	\rq, 1,
	\sustain, 1,

);
~tum1[6]=\filter->{arg in; GVerb.ar(in,earlyreflevel:0.1,mul:SinOsc.kr(1/32).range(0.01,0.1))+in};
);
~tum1[6]=nil;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////random bum

~bum1.clear;
~bum1.stop(fadeTime:40);
~num1.quant_(4);
~bum1.play(vol:0.6, fadeTime:4, numChannels:2);
(~bum1[0]=Pbind(
	\instrument, \bum,
	\amp,1,
	\freq, [44,45]+Pseq([Pseries(20,1,400),Pseries(1200,-1,40)],inf),
	\dur,Prand([0.5,0.25],inf),
	\noise, 0.9,
	\legato, 0.1,

);
~bum1[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,10,1,mul:0.1,add:in*2),0.9)};
~bum1[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1880,440))};
);
~bum1[6]=nil;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////tss looong

~tslong.clear;
~tslong.stop(fadeTime:40);
~tslong.play(vol:0.0041, fadeTime:4, numChannels:2);
~tslong.quant_(4);
(~tslong[0]=Pbind(
	\instrument, \tss,
	\amp,0.5,
	\freq, Pseq([1000,20,140,400],inf),
	\dur, Pseq([Pn(0.01,50),Prand([Rest(15.5),Rest(7.5)],1)],inf),
	\legato, Pseq([0.02,0.05],inf),
	\pan, Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 80,

);

~tslong[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(6).range(90,11000))};
~tslong[7]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,0.2,mul:0.9,add:in*2),0.9)};
~tslong[8]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,4,mul:0.4,add:in*2),0.9)};
)

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////every sec kick

~kick.quant_(4)
~kick.clear;
~kick.stop(freeTime:10);
~kick.play(vol:0.7, fadeTime:4, numChannels:2);
(~kick[0]=Pbind(
	\instrument, \kick,
	\amp, 1,
	\legato, 0.7,
	\pan, Pseq([-1,1],inf),
	\freq, [45,47]+10,
	\dur, 1,
);
~kick[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(120,2)),0.7)};
~kick[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[2,1],mul:0.2)+in,0.7)};

)

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////2 sec kickfill


~kickf.quant_(4);
~kickf.clear;
~kickf.stop(fadeTime:40);
~kickf.play(vol:1, fadeTime:4, numChannels:2);
(~kickf[0]=Pbind(
	\instrument, \kickfill,
	\amp,1,
	\freq, [66,68],
	\dur,2,
	\legato, 0.7,

);
~kickf[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.5),0.6)};
~kickf[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,220))};
)
~kickf[6]=nil;



///////////////////////////////////////////////////////////////////////////////////////nice rhm these two
~kick2.clear;
~kick2.stop(fadeTime:40);
~kick2.play(vol:0.7, fadeTime:4, numChannels:2);
~kick2.quant_(4);
(~kick2[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, 45,
	\dur, Pseq([Pseq([4,[0.25!2,-0.5,0.5,0.5,-2].flatten,1].convertRhythm)
	],inf),
	\top, 40,
	\legato, 0.8,

);
~kick2[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:SinOsc.kr(1/32).range(0.01,0.2),add:in*0.5),0.6)};
~kick2[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,420))};
)
~kick2[7]=nil;





~ts1.clear;
~ts1.stop(fadeTime:40);
~ts1.play(vol:0.1, fadeTime:4, numChannels:2);
~ts1.quant_(4);
(~ts1[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, 1200+Prand([1000,200,400,400],inf),
	\dur, Pseq([Pseq([4,[0.25!2,-0.5,0.5,0.5,-2].flatten,1].convertRhythm)],inf),
	\legato, Pseq([0.1,0.4],inf),
	\pan, 0,//Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 1200,

);
~ts1[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/8).range(9000,14000))};
~ts1[7]=\filter->{arg in; FreeVerb.ar(in)};
~ts1[9]=\filter->{arg in; in*SinOsc.kr(1/32).range(0.1,0.5)};
)
~ts1[7]=nil;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////END

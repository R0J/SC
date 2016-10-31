///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////every sec kick


~kick1sec.clear;
~kick1sec.stop(freeTime:10);
(
~kick1sec.play(vol:0.7, fadeTime:4, numChannels:2);
~kick1sec.quant_(4);)
(~kick1sec[0]=Pbind(
	\instrument, \kick,
	\amp, 1,
	\legato, 0.7,
	\pan, 0,
	\freq, [45,47]+10,
	\dur, 1,
);
~kick1sec[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(120,2)),0.7)};
~kick1sec[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[2,1],mul:0.2)+in,0.7)};

)

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////2 sec kickfill


~kick2sec.clear;
~kick2sec.stop(fadeTime:40);
(
~kick2sec.play(vol:0.3, fadeTime:4, numChannels:2);
~kick2sec.quant_(4);)
(~kick2sec[0]=Pbind(
	\instrument, \kickfill,
	\amp,1,
	\freq, [46,48],
	\dur,2,
	\legato, 0.2,

);
~kick2sec[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.5),0.6)};
~kick2sec[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1800,220))};
)
~kick2sec[6]=nil;


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////random bum

~bumrand.clear;
~bumrand.stop(fadeTime:40);
(
~bumrand.play(vol:0.02, fadeTime:4, numChannels:2);
~bumrand.quant_(4);)
(~bumrand[0]=Pbind(
	\instrument, \bum,
	\amp,1,
	\freq, [44,45]+Pseq([Pseries(20,1,400),Pseries(1200,-1,40)],inf),
	\dur,Prand([0.5,0.25],inf),
	\noise, 0.9,
	\legato, 0.1,

);
~bumrand[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,10,1,mul:0.1,add:in*2),0.9)};
~bumrand[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(880,440))};
);
~bumrand[6]=nil;




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// single bum noise

~bumnoise1.clear;
~bumnoise1.stop(fadeTime:60);
(
~bumnoise1.play(vol:0.1, fadeTime:4, numChannels:2);
~bumnoise1.quant_(4);)
(~bumnoise1[0]=Pbind(
	\instrument, \bum,
	\amp,1,
	\legato, 0.4,
	\freq,[65,67]-15,
	\noise, Pseq([Pn(0.1,6),Pn(0.02,6),Pn(0.2,6)],inf),
	\dur, Pseq([Pn(4,8),Pseq([1,0.5,Rest(2.5)],1)],inf),
);)
(
~bumnoise1[6]=\filter->{arg in; GVerb.ar(in,120,damping:SinOsc.kr(10).range(0.3,0.8),earlyreflevel:SinOsc.kr(10).range(1.3,0.2),mul:0.1,add:in*0.9)};
~bumnoise1[7]=\filter->{arg in; Limiter.ar(in, 0.7)};
)
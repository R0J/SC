///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////every sec kick


~kick1sec.clear;

~kick1sec.stop(freeTime:10);
(
~kick1sec.play(vol:0.4, fadeTime:4, numChannels:2);
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
~kick1sec[8]=\filter->{arg in; HPF.ar(in,30)};


)

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////2 sec kickfill  VERY SEXY


~kick2sec.clear;
~kick2sec.stop(fadeTime:40);
(
~kick2sec.play(vol:0.2, fadeTime:4, numChannels:2);
~kick2sec.quant_(4);)
(~kick2sec[0]=Pbind(
	\instrument, \kickfill,
	\amp,1,
	\freq, [46,48]+20,
	\dur,Pseq([Pseq([2,[0.5,0.2,-1],1].convertRhythm),Rest(2)],inf),
	\legato, 0.2,

);
~kick2sec[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,2,mul:0.2,add:in*0.5),0.9)};
~kick2sec[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(3800,720))};
~kick2sec[9]=\filter->{arg in; HPF.ar(in,80)};
)
~kick2sec[6]=nil;


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////random bum

~bumrand.clear;
~bumrand.stop(fadeTime:40);
(
~bumrand.play(vol:0.8, fadeTime:4, numChannels:2);
~bumrand.quant_(4);)
(~bumrand[0]=Pbind(
	\instrument, \bum,
	\amp,1,
	\freq, [44,45]+Pseq([Pseries(20,1,400),Pseries(1200,-1,40)],inf),
	\dur,Prand([0.5,0.25],inf),
	\noise, 0.9,
	\legato, 0.4,

);
~bumrand[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,10,1,mul:0.1,add:in*2),0.9)};
~bumrand[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(880,440))};
);
~bumrand[6]=nil;




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// single bum noise

~bumnoise2.clear;
~bumnoise2.stop(fadeTime:60);
(
~bumnoise2.play(vol:0.2, fadeTime:4, numChannels:2);
~bumnoise2.quant_(4);)
(~bumnoise2[0]=Pbind(
	\instrument, \bum,
	\amp,1,
	\legato, 0.4,
	\freq,[65,67],
	\noise, Pseq([Pn(0.1,6),Pn(0.02,6),Pn(0.2,6)],inf),
	\dur, 4,
);)
(
~bumnoise2[6]=\filter->{arg in; GVerb.ar(in,120,damping:SinOsc.kr(10).range(0.3,0.8),earlyreflevel:SinOsc.kr(10).range(1.3,0.2),mul:0.1,add:in*0.9)};
~bumnoise2[7]=\filter->{arg in; Limiter.ar(in, 0.6)};
)
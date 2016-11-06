
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// aural

~aural1.clear;
~aural1.stop(fadeTime:60);
(
~aural1.play(vol:0.041, fadeTime:4, numChannels:2);
~aural1.quant_(4);)
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




/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////BLOW long


(
~blow.play;
~blow.fadeTime=1;
~blow.quant=4;
)
(~blow[1]=Pbind(
	\instrument, \blow,
	\dur, Pseq([4,6],inf),
	\legato,2,
	\octave, Prand([[4,6],[4,2],3,[1,2]],inf),
	\degree,Pseq([[4,2,8],[4,8],[4,2],[2,6]],inf),
	\amp, 0.3

););
(
~blow[4]=\filter->{arg in; GVerb.ar(in,[40,80],[4,6],mul:0.4,add:in)*4};
~blow[5]=\filter->{arg in; LPF.ar(in,\lpf6.kr(800))};
// ~blow[5]=nil;
)


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////bells long



~oto.clear;
~oto.stop(fadeTime:60);
(
~oto.play(vol:0.1, fadeTime:4, numChannels:2, group:g);
~oto.quant_(4);)
(~oto[0]=Pbind(
	\instrument, \oto,
	\amp,Pseq([0.7,0.4,0.3],inf),
	\legato, 3,
	\dur, 8,
	\octave, Pseq([Pn([3,2],4),Pn([1,2],4),Pn([6,4],2)],inf)+2,
	\degree, [2,3]+Pseq([5,3,2,1],inf),
	\dist, Pseq([Pseries(2,1,40),Pseries(400,(-1),20)],inf),

);
g=Group.new;
);
~oto[3]=\filter->{arg in; LPF.ar(in,2800)};
~oto[4]=\filter->{arg in; Rotate2.ar(Mix.new(in),Mix.new(DelayN.ar(HPF.ar(in,\hpf.kr(2000)),1,0.5)),SinOsc.kr(0.25))};
~oto[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[60,80],[10,8],mul:0.6,add:in*0.2))};
~oto[7]=nil;


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////string long  random

~combdrone.clear;
~combdrone.stop(freeTime:60);
(
~combdrone.play(vol:0.01, fadeTime:4, numChannels:2);
~combdrone.quant_(4);)
(~combdrone[0]=Pbind(
	\instrument, \string,
	\amp, 1,
	\legato, 1.4,
	\pan, 0,
	\freq,Pseq([Pseries(80,(-4),20),Pseries(1,4,8),Pseries(60,(-4),5)],inf),
	\dur,12,
);)
(
~combdrone[3]=\filter->{arg in; HPF.ar(in,60)};
~combdrone[6]=\filter->{arg in; LPF.ar(in,\lpfc.kr(240,2))};
~combdrone[7]=\set->Pbind(\args, [\lpfc], \dur, 8, \lpfc, Pseq([280,420,400,440,480],inf));
~combdrone[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[2,4,6],[6,8],mul:0.1)+in.softclip,0.7)};
)
~combdrone[9]=nil;



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////tum2


~tum2.clear;
~tum2.stop(fadeTime:40);

(b=Group.new;
~tum2.quant_(4);
~tum2.play(vol:0.02, fadeTime:4, numChannels:2, group:b);)
(~tum2[0]=Pbind(
	\instrument, \tum,
	\amp,1,
	\octave, Pseq([2!16,1!16].flatten,inf),
	\degree, 4,
	\dur, Pseq([Pseq([2,[0.5,-0.25,0.25,0.25,-0.25,0.25!2].flatten,4].convertRhythm),Pseq([2,[0.5,-0.25,-0.25,0.25,-0.25,0.25!2].flatten,1].convertRhythm)],inf),
	\rq, Pseq([2!32,0.01!64].flatten,inf),
	\sustain, 1,

);
~tum2[6]=\filter->{arg in; GVerb.ar(in,roomsize:20,revtime:2,earlyreflevel:0.8,mul:SinOsc.kr(1/32).range(0.01,0.4))+in};
);
~tum2[6]=nil;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////tum3


~tum3.clear;
~tum3.stop(fadeTime:40);

(f=Group.new;
~tum3.quant_(4);
~tum3.play(vol:0.4, fadeTime:4, numChannels:2, group:f);)
(~tum3[0]=Pbind(
	\instrument, \tum,
	\amp,Pseq([Pseq([2,[0.25!4, -0.25,0.25!2, -0.5,1,-0.5].flatten,1].convertRhythm)],inf),
	\octave, Pseq([2!16,1!16].flatten,inf),
	\degree, 4,
	\dur,0.25,
	\rq, Pseq([2!32,0.01!64].flatten,inf),
	\sustain, 1,

);
~tum3[6]=\filter->{arg in; GVerb.ar(in,roomsize:60,revtime:SinOsc.kr(1/64).range(3,0.4),mul:SinOsc.kr(1/64).range(0.01,0.4))+in};
);
~tum3[6]=nil;



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////RHM drum



~rhmdrum.clear;
~rhmdrum.stop(fadeTime:40);
~rhmdrum.play(vol:0.4, fadeTime:4, numChannels:2).quant_(4);
(~rhmdrum[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, [44,45],
	\dur, Pseq([Pseq([4,[0.1!4,-1,0.1,-0.1,0.1!3,-0.1].flatten,3].convertRhythm),
		Pseq([4,[0.1!4,-1,0.1,0.1,0.1!3,-0.1].flatten,3].convertRhythm)
	],inf),
	\top, 40,

););
~rhmdrum[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.5,add:in*0.5),0.6)};
~rhmdrum[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(400,200))};
~rhmdrum[6]=nil;
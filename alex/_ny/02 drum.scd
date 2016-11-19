

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////tum2


~tum01.clear;
~tum01.stop(fadeTime:40);

(
~tum01.quant_(4);
~tum01.play(vol:0.02, fadeTime:4, numChannels:2);)
(~tum01[0]=Pbind(
	\instrument, \tum,
	\amp,Pseq([
		Pseq([ 1,0,0.4,1,  1,0.4,0,1,  0,1, 1, 0,  0,0,0,1],2),
		Pseq([ 1,0,0.6,1,  1,0.6,0,1,  0,0.5,1,0,  0,0,0,1],2),
		Pseq([ 1, 1, 1,1,  1,0.6,0,1,  0,0.5,1,1,  0,0,0,1],2),

	],inf),
	\octave,1,
	\degree, 1,
	\dur, 0.25,
	\rq, 0.02,
	\sustain, 1.2,

);
~tum01[6]=\filter->{arg in; GVerb.ar(in,roomsize:40,revtime:4,earlyreflevel:0.8,mul:SinOsc.kr(1/32).range(0.01,0.4))+in};
~tum01[12]=\filter->{arg in; LPF.ar(in, SinOsc.ar(1/64).range(2420,600))};
);
~tum01[6]=nil;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////tum2


~tum02.clear;
~tum02.stop(fadeTime:40);

(
~tum02.quant_(4);
~tum02.play(vol:0.02, fadeTime:4, numChannels:2);)
(~tum02[0]=Pbind(
	\instrument, \tum,
	\amp,Pseq([
		Pseq([ 1,1,0, 1,   1,0.7,1,1,  0,1,0.1,0,  0,0.8,0,1],4),
		Pseq([ 1,0,0.6,1,  1,0.6,0,1,  0,0.5,1,0,  0,0,0,1],2),

	],inf),
	\octave,1,
	\degree, 1,
	\dur, 0.25,
	\rq, 0.02,
	\sustain, 1.2,

);
~tum02[6]=\filter->{arg in; GVerb.ar(in,roomsize:40,revtime:4,earlyreflevel:0.8,mul:SinOsc.kr(1/32).range(0.01,0.4))+in};
~tum02[12]=\filter->{arg in; LPF.ar(in, SinOsc.ar(1/64).range(2420,600))};
);
~tum02[6]=nil;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////tum2


~tum2.clear;
~tum2.stop(fadeTime:40);

(
~tum2.quant_(4);
~tum2.play(vol:0.02, fadeTime:4, numChannels:2);)
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

(
~tum3.quant_(4);
~tum3.play(vol:0.4, fadeTime:4, numChannels:2);)
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


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////tum6

~tum6.free;
~tum6.clear;
~tum6.stop(fadeTime:40);

(
~tum6.quant_(4);
~tum6.play(vol:0.1, fadeTime:4, numChannels:2);)
(~tum6[0]=Pbind(
	\instrument, \tum,
	\amp,1,
	\octave, 2,
	\degree, 1,
	\dur,2,
	\rq, 0.02,
	\sustain, 0.4,

);
~tum6[6]=\filter->{arg in; GVerb.ar(in,roomsize:60,revtime:SinOsc.kr(1/64).range(3,0.4),mul:SinOsc.kr(1/64).range(0.01,0.4))+in};
);
~tum6[6]=nil;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////tum3
p.gui;

~tum12.clear;
~tum12.stop(fadeTime:40);

(
~tum12.quant_(4);
~tum12.play(vol:0.4, fadeTime:4, numChannels:2);)
(~tum12[0]=Pbind(
	\instrument, \tum,
	\amp,0.5,
	\octave, 3,
	\degree, 2,○
	\dur,Pseq([Pseq([2,[0.25,0.15,-2].flatten,1].convertRhythm),Rest(6)],inf),
	\rq, 2,
	\sustain, 1,

);
~tum12[6]=\filter->{arg in; GVerb.ar(in,roomsize:60,revtime:SinOsc.kr(1/64).range(3,0.4),mul:SinOsc.kr(1/64).range(0.01,0.4))+in};
);
~tum12[6]=nil;


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////RHM drum
p.gui;


~rhmdrum.clear;
~rhmdrum.stop(fadeTime:40);
~rhmdrum.play(vol:0.4, fadeTime:4, numChannels:2);
~rhmdrum.quant_(4);
(~rhmdrum[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, [44,45]+20,
	\dur, Pseq([Pseq([4,[0.1!4,-1,0.1,-0.1,0.1!3,-0.1].flatten,3].convertRhythm),
		Pseq([4,[0.1!4,-1,0.1,0.1,0.1!3,-0.1].flatten,3].convertRhythm)
	],inf),
	\top, 40,

););
~rhmdrum[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.5,add:in*0.5),0.6)};
~rhmdrum[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(400,200))};
~rhmdrum[6]=nil;
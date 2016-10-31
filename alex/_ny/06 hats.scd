
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TS 808 longer

~tss808long.clear;
~tss808long.stop(fadeTime:40);

(
~tss808long.play(vol:0.4, fadeTime:1, numChannels:2);
~tss808long.quant_(4))
(~tss808long[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([1,0.5,0.1,2],inf),
	\dur,0.25,
	\legato, 0.6,
	\hpf,11000,

););
~tss808long[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,10,SinOsc.kr(1/64).range(0.8,1),mul:0.8,add:ts*2),0.9)};
~tss808long[7]=nil;




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TS 808

~tss808short.clear;
~tss808short.stop(fadeTime:40);
(
~tss808short.play(vol:0.4, fadeTime:4, numChannels:2);
~tss808short.quant_(4);)
(~tss808short[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([1,0.2],inf),
	\dur,0.25,
	\legato, 0.8,
	\hpf,10000,

););
~tss808short[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,10,SinOsc.kr(1/64).range(8,1),mul:0.8,add:in*2),0.9)};

~tss808short[6]=nil;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TS 808 2


~tss8082.clear;
~tss8082.stop(fadeTime:40);
(
~tss8082.play(vol:0.8, fadeTime:4, numChannels:2);
~tss8082.quant_(4);)
(~tss8082[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([0.1,0.5,1,0.1,0.25,0.5,0.4,0.5],inf),
	\dur,0.25,
	\legato, 0.4,
	\hpf,5000,
);
~tss8082[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,10,SinOsc.kr(1/64).range(2,0.1),mul:0.8,add:in*2),0.9)};
)
~tss8081[6]=nil;



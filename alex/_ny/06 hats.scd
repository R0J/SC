
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TS 808 longer

~tss808long.clear;
~tss808long.stop(fadeTime:40);

(
~tss808long.play(vol:0.4, fadeTime:1, numChannels:2);
~tss808long.quant_(4))
(~tss808long[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([1,0.5,0.1,0],inf),
	\dur,0.25,
	\legato, 0.4,
	\hpf,Pseq([ 500,5000,1500,200,  500,4000,500,6000, 120,60,5000,4200, 120,400,800,4500],inf),

););
~tss808long[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,10,SinOsc.kr(1/64).range(0.8,1),mul:0.8,add:ts*2),0.9)};
~tss808long[7]=nil;

p.gui;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TS 808

~tss808short.clear;
~tss808short.stop(fadeTime:40);
(
~tss808short.play(vol:0.6, fadeTime:4, numChannels:2);
~tss808short.quant_(4);)
(~tss808short[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([
		Pseq([ 1,0,0.4,0,  0,0.4,0,1, 0,0,1,0, 0,0,0,1],4),
		Pseq([ 1,0,0.6,0,  0,0.6,0,1, 0,0.5,0,0, 0,0,0,1],2)
	],inf),
	\dur,0.2,
	\legato, 0.8,
	\hpf, [180,2000],//Pseq([4000,2000],inf),

););
~tss808short[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,SinOsc.kr(1/64).range(8,1),mul:0.8,add:in*2),0.9)};

~tss808short[6]=nil;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TS 808 2


~tss8082.clear;
~tss8082.stop(fadeTime:40);
(
~tss8082.play(vol:0.2, fadeTime:4, numChannels:2);
~tss8082.quant_(4);)
(~tss8082[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([0.1,0.5,1,0.1,0.25,0.5,0.4,0.5],inf),
	\dur,0.25,
	\legato, 0.7,
	\hpf,[1100,40],
);
~tss8082[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,10,SinOsc.kr(1/64).range(2,0.1),mul:0.8,add:in*2),0.9)};
)
~tss8081[6]=nil;



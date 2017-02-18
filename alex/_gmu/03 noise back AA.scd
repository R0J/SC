/////////////////////////
/////////////////////////
/////////////////////////
/////////////////////////
////////NOISE


////////////////////////////////////////////////////////////////////////////////////////////////////SNAKE


~snake.clear;

~snake.stop(freeTime:10);
(
~snake.play(vol:0.02, fadeTime:4, numChannels:2);
~snake.quant=16;)
(~snake[0]=Pbind(
	\instrument,\snakes,
	\amp, 5,
	\legato, 1,
	\freq, Pseq([Pseries(3500,50,40),Pseries(5600,(-50),40)],inf),
	\dur, Pseq([Rest(4),4,4,4],inf),
	\f2, Prand([12,12,12,13,8,16,4],inf),
);
~snake[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,SinOsc.kr(1/64).range(200,420)),0.7)};
~snake[9]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[1,2],mul:SinOsc.kr(1/64).range(0.1,0.4))+in,0.7)};
)
~snake[8]=\filter->{arg in; HPF.ar(in,3400,5)};
~snake[8]=nil;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////DRUJBA



~drujba.clear;
~drujba.stop(fadeTime:20);
(
~drujba.play(vol:0.01, fadeTime:4, numChannels:2);
~drujba.quant_(16);)
(~drujba[0]=Pbind(
	\instrument, \zit,
	\amp,1,
	\legato, 0.5,
	\freq,80,//Prand([200,400,1200,100,65,200,400],inf),
	\noise, 0.9,
	\dur, Pseq([Pn(0.2,40),Pn(0.2,80),Rest(4)],inf),
);


~drujba[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[40,80],revtime:8,mul:0.2,add:in*0.5),0.9)};
~drujba[7]=\filter->{arg in; LPF.ar(in, SinOsc.kr(1/32).range(200,1400))};
)
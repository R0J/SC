/////////////////////////
/////////////////////////
/////////////////////////
/////////////////////////
////////NOISE


////////////////////////////////////////////////////////////////////////////////////////////////////SNAKE

p.gui;
~snake.clear;

~snake.stop(freeTime:10);
(
<<<<<<< HEAD
~snake.play(vol:0.01, fadeTime:4, numChannels:2);
=======
~snake.play(vol:0.2, fadeTime:4, numChannels:2);
>>>>>>> master
~snake.quant=16;)

(~snake[0]=Pbind(
	\instrument,\snakes,
	\amp, 5,
	\legato, 0.5,
	\freq, Pseq([Pseries(3500,50,40),Pseries(5600,(-50),40)],inf),
	\dur, Pseq([Rest(8),4,4],inf),
	\f2, Prand([12,12,12,13,8,16,4],inf),
);
<<<<<<< HEAD
~snake[47]=\filter->{arg in; Limiter.ar(LPF.ar(in,SinOsc.kr(1/64).range(200,420)),0.7)};
~snake[48]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[1,2],mul:SinOsc.kr(1/64).range(0.1,0.4))+in,0.7)};
=======
~snake[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,SinOsc.kr(1/64).range(200,420)),0.7)};
~snake[9]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[4,8],mul:SinOsc.kr(1/64).range(0.1,0.4))+in,0.7)};
>>>>>>> master
)
~snake[49]=\filter->{arg in; HPF.ar(in,4400,5)};
~snake[49]=nil;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////DRUJBA



~drujba.clear;
~drujba.stop(fadeTime:20);
(
~drujba.play(vol:0.001, fadeTime:4, numChannels:2);
~drujba.quant_(16);)
(~drujba[0]=Pbind(
	\instrument, \zit,
	\amp,1,
	\legato, 0.5,
	\freq,800,//Prand([200,400,1200,100,65,200,400],inf),
	\noise, 0.9,
	\dur, 16,
);


~drujba[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[40,80],revtime:18,mul:0.2,add:in*0.5),0.9)};
~drujba[7]=\filter->{arg in; LPF.ar(in, SinOsc.kr(1/32).range(200,1400))};
)

p.gui;


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// LEAKY NOISE LONG

~leaky.clear;
~leaky.stop(fadeTime:10);
(
~leaky.play(vol:0.04, numChannels:2);
~leaky.quant=16;)
(
~leaky[0]=Pbind(
	\instrument, \leaky,
	\dur,8,
	\octave,Prand([3,7,6,8],inf),
	\degree, [2,9,1,-3],
	\strum, 0.02,
	\amp, 0.2,
	\legato, Prand([0.02,0.04,0.08],inf),
);

~leaky[4]=\filter->{arg in; FreeVerb.ar(in,room:2)+in};
//~leaky[5]=\filter->{arg in; FreeVerb.ar(in,room:1,damp:0.1)};
~leaky[6]=\filter->{arg in; Limiter.ar(in, 0.8)};
)
~leaky[5]=nil;


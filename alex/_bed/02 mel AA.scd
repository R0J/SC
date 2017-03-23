///////////////////
//////////////////
/////////////////
/////MELODY


///


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////MELODY THREE

~mel3.clear;
~mel3.stop(freeTime:20

);

(
~mel3.play(vol:0.01, fadeTime:4, numChannels:2,);
~mel3.quant=16;
);
(~mel3[0]=Pbind(
	\instrument, \mel,
	\amp, 0.5,//Pseq([ 0,0,0.4,1,  1,1,0,1,  0,1,1,0.5,  0,0.2,0.3,1],inf),
	\legato, 2,
	\octave, Pseq([
		Pseq([4,6,5,8, 6,4,2,7, 4,8,4,3, 4,8,8,3],4),
		Pseq([4,6,5,8, 6,4,2,7, 4,8,4,3, 4,8,8,3],4)]
		,inf),

	\degree, Pseq([4,6,5,8, 6,4,2,7, 4,8,2,8, 4,8,2,8],inf),
	\noise, Pseq([Pn(0.5,16),Pn(0.05,32),Pn(0.2,16),Pn(0.9,32)],inf),
	\dur, 0.25,
);

~mel3[5]=\filter->{arg in; GVerb.ar(in,[50,40],[8,10],mul:0.1,maxroomsize:200)+(in*2)};
~mel3[7]=\filter-> {arg in; CombN.ar(in,2,0.1,0.2)};
~mel3[8]=\filter->{arg in; LPF.ar(in, 3200)};
)
~mel3[5]=nil;




//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////mel

~mel01.clear;
~mel01.stop(freeTime:60);

(
~mel01.play(vol:0.1, fadeTime:4, numChannels:2,);
~mel01.quant_(16);
);
(~mel01[0]=Pbind(
	\instrument, \mel,
	\amp, 3,
	\legato, 0.7,
	\octave, Prand([4,3,5],inf),
	\degree, [4,2,8,6],
	\strum, 0.5,
	\noise, Prand([Pn(0.8,13),Pn(0.5,13*3),Pn(0.2,13),Pn(1,26)],inf),
	\dur, Pseq([Rest(2),0.25!8].flatten,inf),
);
~mel01[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf3.kr(200,2)),0.7)};
~mel01[4]=\set->Pbind(\args, [\lpf3], \dur,16,\lpf3, Pseq([820,600,400,1200,400,600,820],inf));
~mel01[5]=\filter->{arg in; GVerb.ar(in,[50,40],[8,6],mul:0.2,maxroomsize:200)+(in*2)};
)
~mel01[5]=nil;


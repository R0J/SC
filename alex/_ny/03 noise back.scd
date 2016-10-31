

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////DRUJBA



~drujba.clear;
~drujba.stop(fadeTime:20);
(
~drujba.play(vol:0.02, fadeTime:4, numChannels:2);
~drujba.quant_(4);)
(~drujba[0]=Pbind(
	\instrument, \zit,
	\amp,1,
	\legato, 0.5,
	\freq,80,//Prand([200,400,1200,100,65,200,400],inf),
	\noise, 0.9,
	\dur, Pseq([0.2],inf),
);

);
(
~drujba[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,[40,80],revtime:LFSaw.kr(1/16).range(4,40),damping:SinOsc.kr(0.125).range(0.6,0.9),mul:0.1,add:in*0.5),0.7)};
~drujba[7]=\filter->{arg in; LPF.ar(in, SinOsc.kr(1/32).range(6400,400))};
)


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// CHEAP high

~chaap.stop(fadeTime:20);
(
~cheap.play(vol:1, numChannels:2);
~cheap.quant_(4);)
(
~cheap[0]=Pbind(
	\instrument, \cheap,
	\dur,Pseq([Pseq([2,[0.25!7,-0.25,0.25,0.1,-1].flatten,1].convertRhythm)],inf),
	\dec, 2,
	\octave, [4,2]+4,
	\degree, [4,2],
	\amp, 0.1,
	\strum, 0.15,//Pseq([Pn(0.15,11),Pn(0.1,22)],inf),

);
)
~cheap[4]=\filter->{arg in; CheapVerb.ar(in,4)};
~cheap[4]=nil;



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// IMP continous


~impcont.clear;
~impcont.stop(freeTime:60);

(t=Group.new;
~impcont.play(vol:1, fadeTime:4, numChannels:2, group:t);
~impcont.quant_(4);
);
(~impcont[0]=Pbind(
	\instrument, \imp,
	\amp, 3,
	\legato, Pseq([4,3],inf),
	\den,Prand([10,18,12],inf),
	\dur, 16,
);


~impcont[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf3.kr(200,2)),0.7)};
~impcont[4]=\set->Pbind(\args, [\lpf3], \dur,16,\lpf3, Pseq([820,600,400,1200,400,600,820],inf));
~impcont[5]=\filter->{arg in; GVerb.ar(in,[180,120],[4,6],mul:0.8,maxroomsize:200)+in};
)
~impcont[5]=nil;



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// BOW LONG continous


~bowcont.clear;
~bowcont.stop(freeTime:30);
(
~bowcont.play(vol:0.01, fadeTime:4, numChannels:2);
~bowcont.quant_(4);)
(~bowcont[0]=Pbind(
	\instrument, \bow,
	\dur, Pseq([6,Rest(2)],inf),
	\legato, 2,
	\octave, Pseq([[3,1],[4,1],[2,3],[1,2],[4,2]],inf),
	\degree, [2,1],
	\amp, 0.8,
);)
(
~bowcont[3]=\filter->{arg in; LPF.ar(in,\lpf6.kr(400))+(in*0.01)};
~bowcont[7]=\set->Pbind(\args, [\lpf6], \dur,12, \lpf6, Pseq([600,800,200],inf)+200);
~bowcont[30]=\filter->{arg in; GVerb.ar(in,[80,60],[6,4],maxroomsize:120,add:in,mul:0.2)+in};
~bowcont[32]=\filter->{arg in; Limiter.ar(in,0.7)};
)
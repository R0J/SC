//////////////////////////////////////////////
//////////////////////////////////////////////
//////////////////////////////////////////////
////////////////////KICKS AN TUMS



currentEnvironment.print;
currentEnvironment.free;
//p.arProxyNames.collect({arg n; var synth=(p[n.asSymbol]); synth.stop(30)});
(
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;
p.clock.tempo = 127/60;
)
p.gui;

/////////////////////////////////////////////////////////////////////////////////////////////////////////kick group
~acid.clear;

~acid.stop(freeTime:10);
(
~acid.play(vol:0.4, fadeTime:4, numChannels:2);
~acid.quant=16;
)
(~acid[0]=Pbind(
	\instrument, \acid,
	\dur,Pseq([Rest(0.1),3,0.2,0.7],inf),
	\amp,Pseq([Rest(0.1),1,0.4,0.7],inf),
	\sustain, 1,
	\freq, 90

);

~acid[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,4,mul:0.2)+in,0.7)};
~acid[8]=\filter->{arg in; HPF.ar(in,30)};

)

~acid[4]=nil;

/////////////////////////////////////////////////////////////////////////////////////////////////////////kick group
~acid2.clear;

~acid.stop(freeTime:10);
(
~acid2.play(vol:0.4, fadeTime:4, numChannels:2);
~acid2.quant=16;
)
(~acid2[0]=Pbind(
	\instrument, \acid,
	\dur,0.25,
	\amp,Pseq([ 0,0.2,0.4,1,    1,1,0,1,  0,1,1, 0,    0,0, 0.8,1],inf),
	\sustain, 0.8,
	\freq, 1200
	);

~acid2[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,1,mul:0.2)+in,0.7)};
~acid2[8]=\filter->{arg in; HPF.ar(in,30)};

)

~acid[4]=nil;

/////////////////////////////////////////////////////////////////////////////////////////////////////////KICK 1 SEC
~kick1sec.clear;

~kick1sec.stop(freeTime:0.1);
(
~kick1sec.play(vol:1, fadeTime:0.1, numChannels:2);
~kick1sec.quant=16;
)
(~kick1sec[0]=Pbind(
	\instrument, \kick,
	\amp, 2,
	\legato, 0.2,
	\top, 20,
	\pan, 0,
	\freq, [45,47],
	\dur,1,//Pseq([Rest(3),1],inf),
);
~kick1sec[33]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(220,2)),0.4)};
~kick1sec[44]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,[2,1],mul:0.2)+in,0.7)};

//~kick1sec[8]=\filter->{arg in; HPF.ar(in,5)};
~kick1sec[5]=\filter->{arg in; Compander.ar(in,thresh:0.2,slopeBelow:0.1,slopeAbove:1,relaxTime:0.01)};
//~kick1sec[6]=\filter-> {arg in; Clip.ar(in,-0.4,0.4)};



)

~kick1sec[4]=nil;

p.gui;
/////////////////////////////////////////////////////////////////////////////////////////////////////////KICK 4 SEC
~kick4sec.clear;

~kick4sec.stop(freeTime:0.1);
(
~kick4sec.play(vol:1, fadeTime:0.1, numChannels:2);
~kick4sec.quant=16;
)
(~kick4sec[0]=Pbind(
	\instrument, \kick,
	\amp, 4,
	\legato, 0.1,
	\top, 40,
	\pan, 0,
	\freq, 45,
	\dur,4,//Pseq([Rest(3),1],inf),
);
~kick4sec[33]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(320,2)),0.4)};
~kick4sec[44]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[3,5],mul:0.2)+in,0.7)};

//~kick1sec[8]=\filter->{arg in; HPF.ar(in,5)};
~kick4sec[5]=\filter->{arg in; Compander.ar(in,thresh:0.2,slopeBelow:0.1,slopeAbove:1,relaxTime:0.01)};
//~kick1sec[6]=\filter-> {arg in; Clip.ar(in,-0.4,0.4)};



)

~kick1sec[4]=nil;

p.gui;


p.gui;

////////////////////////////////////////////////////////////////////////////////////////////////////////THIS IS FINE

~tum02.clear;
~tum02.stop(fadeTime:40);

(
~tum02.quant=16;
~tum02.play(vol:0.2, fadeTime:4, numChannels:2);)
(~tum02[0]=Pbind(
	\instrument, \tum,
	\amp,Pseq([0.5,1, Rest(4)],inf),                                                       /////////ADD HERE AMP FOR ALL THE TIME BEAT
	\freq, Pseq([Pn(85,64),Pn(65,64),Pn(185,64)],inf),
	\pan, Pseq([-1,0,1],inf),
	\dur, 0.25,
	\rq,0.001,
	\sustain, 0.8,

);
~tum02[6]=\filter->{arg in; GVerb.ar(in,roomsize:180,revtime:6,mul:0.02)+(in*2)};
~tum02[12]=\filter->{arg in; HPF.ar(in,\hp4.kr)};
~tum02[14]=\set->Pbind(\args, [\hp4], \dur, 64, \hp4, Pseq([180,340,240,80]+800,inf));

~tum02[22]=\filter->{arg in; Limiter.ar(in, 0.7)};
~tum02[100]=\filter->{arg in; Compander.ar(in, ~kick1sec.ar, 0.1, 1, 0.1) };

);
~tum02[6]=nil;
~tum02[34]=\filter->{arg in; LPF.ar(in,800)};

>>>>>>> master

////////////////////////////////////////////////////////////////////////////////////////////////////////THIS IS FINE

~tum01.clear;
~tum01.stop(fadeTime:40);

(
~tum01.quant=16;
~tum01.play(vol:0.4, fadeTime:4, numChannels:2);)
~tum01.play(vol:0.02, fadeTime:4, numChannels:2);)
(~tum01[0]=Pbind(
	\instrument, \kick,
	\amp,Pseq([
		Pseq([ 0,0.2,0.4,1,    1,1,0,1,  0,1,1, 0,    0,0, 0.8,1],2),
		Pseq([ 0,  0,0.4,1,    1,1,0,1,  0,1,1, 0,    1,1,   0,1],2)+0.1,

	],inf),                                                                /////////ADD HERE AMP FOR ALL THE TIME BEAT
	\freq, 800,
	\pan, Pseq([-1,0,1],inf),
	\dur, 0.25,
	\rq,0.01,
	\top, 40,
	\legato, 0.2,
);
~tum01[76]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.9),0.8)};
~tum01[77]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/32).range(40,60))};
~tum01[78]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/64).range(2800,700))};)

);
~tum01[6]=\filter->{arg in; GVerb.ar(in,roomsize:180,revtime:6,mul:0.02)+(in*2)};
~tum01[12]=\filter->{arg in; HPF.ar(in,\hp4.kr)};
~tum01[14]=\set->Pbind(\args, [\hp4], \dur, 64, \hp4, Pseq([180,340,240,80]+800,inf));

~tum01[68]=\filter->{arg in; Limiter.ar(in, 0.7)};
~tum01[100]=\filter->{arg in; Compander.ar(in, ~kick1sec.ar, 0.1, 1, 0.1) };

);
~tum01[60]=nil;
~tum01[6]=nil;
~tum01[34]=\filter->{arg in; LPF.ar(in,800)};



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TADA TUM
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
	\degree, 2,
	\dur,Pseq([Pseq([2,[0.25,0.15,-2].flatten,1].convertRhythm),Rest(6)],inf),
	\rq, 2,
	\sustain, 1,

);
~tum12[6]=\filter->{arg in; GVerb.ar(in,roomsize:60,revtime:5,mul:SinOsc.kr(1/64).range(0.01,0.4))+in};
);
~tum12[6]=nil;



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////RHM drum
p.gui;


~rhmdrum.clear;
~rhmdrum.stop(fadeTime:40);
~rhmdrum.play(vol:1, fadeTime:4, numChannels:2);
~rhmdrum.quant_(4);
(~rhmdrum[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, [44,45]+20,
	\dur, Pseq([Pseq([4,[0.1!4,-1,0.1,-0.1,0.1!3,-0.1].flatten,3].convertRhythm),
		Pseq([4,[0.1!4,-1,0.1,0.1,0.1!3,-0.1].flatten,3].convertRhythm)
	],inf),
	\sustain, 0.05,
	\top, 80,

););
~rhmdrum[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,5,mul:0.5,add:in*0.5),0.6)};
~rhmdrum[7]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/32).range(1600,2000))};
~rhmdrum[6]=nil;
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
~kick1sec.clear;

~kick1sec.stop(freeTime:10);
(
~kick1sec.play(vol:1.5, fadeTime:4, numChannels:2);
~kick1sec.quant=1;
)
(~kick1sec[0]=Pbind(
	\instrument, \kick,
	\amp, 3,
	\legato, 0.4,
	\pan, 0,
	\freq, [45,47]-5,
	\dur,Pseq([0.5,1,Rest(0.5)],inf),
);
~kick1sec[2]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(220,2)),0.4)};

//~kick1sec[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,[0.2,0.1],mul:0.2)+in,0.7)};
//~kick1sec[8]=\filter->{arg in; HPF.ar(in,5)};
~kick1sec[5]=\filter->{arg in; Compander.ar(in,thresh:0.2,slopeBelow:0.1,slopeAbove:1,relaxTime:0.01)};
//~kick1sec[6]=\filter-> {arg in; Clip.ar(in,-0.4,0.4)};



)

~kick1sec[4]=nil;

p.gui;


////////////////////////////////////////////////////////////////////////////////////////////////////////THIS IS FINE

~tum01.clear;
~tum01.stop(fadeTime:40);

(
~tum01.quant=16;
~tum01.play(vol:0.8, fadeTime:4, numChannels:2);)
(~tum01[0]=Pbind(
	\instrument, \tum,
	\amp,Pseq([
		//Pseq([ 0,0,0,0,  0,0,0,1,  0,1,1, 0,    0,  0,0,1],2),

		Pseq([ 0,0.2,0.4,1,    1,1,0,1,  0,1,1, 0,    0,0, 0.8,1],2),
		Pseq([ 0,  0,0.4,1,    1,1,0,1,  0,1,1, 0,    0,0,   0,1],2),
		Pseq([ 0,  0,0.4,1,    1,1,0,1,  0,1,1, 0.5,  0,0.5, 0,1],4),
	],inf),                                                                /////////ADD HERE AMP FOR ALL THE TIME BEAT
	\freq, 265,
	\pan, Pseq([-1,0,1],inf),
	\dur, 0.25,
	\rq,0.01,
	\sustain, 0.4,

);
~tum01[6]=\filter->{arg in; GVerb.ar(in,roomsize:180,revtime:6,mul:0.02)+(in*2)};
~tum01[12]=\filter->{arg in; HPF.ar(in,\hp4.kr)};
~tum01[14]=\set->Pbind(\args, [\hp4], \dur, 64, \hp4, Pseq([180,340,240,80]+800,inf));

~tum01[22]=\filter->{arg in; Limiter.ar(in, 0.7)};
~tum01[100]=\filter->{arg in; Compander.ar(in, ~kick1sec.ar, 0.1, 1, 0.1) };

);
~tum01[6]=nil;
~tum01[34]=\filter->{arg in; LPF.ar(in,800)};



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////  RHM DRUM


~rhmdrum.clear;
~rhmdrum.stop(fadeTime:40);
(
~rhmdrum.play(vol:0.4, fadeTime:4, numChannels:2);
~rhmdrum.quant_(16);)
(~rhmdrum[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, [44,45]+20,
	\dur, Pseq([Pseq([4,[0.1!4,-1,0.1,-0.1,0.1!3,-0.1].flatten,3].convertRhythm),
		Pseq([4,[0.1!4,-1,0.1,0.1,0.1!3,-0.1].flatten,3].convertRhythm)
	],inf),
	\top, 40,

);
~rhmdrum[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.9),0.8)};
~rhmdrum[7]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/32).range(40,60))};
~rhmdrum[8]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/64).range(2800,200))};)
~rhmdrum[6]=nil;


p.gui;


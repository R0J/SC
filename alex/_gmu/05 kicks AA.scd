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
~kick1sec.play(vol:1, fadeTime:4, numChannels:2);
~kick1sec.quant=16;
)
(~kick1sec[0]=Pbind(
	\instrument, \kick,
	\amp, 2,
	\legato, 1,
	\pan, 0,
	\freq, [45,47]+10,
	\dur,Pseq([Rest(3),1],inf),
);
~kick1sec[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(320,2)),0.4)};
~kick1sec[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,[2,1],mul:0.2)+in,0.7)};
//~kick1sec[8]=\filter->{arg in; HPF.ar(in,5)};

)



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

		Pseq([ 0,0.2,0.4,1,    1,1,0,1,  0,1,1, 0,    0,0, 0.8,1],2)+0.1,
		Pseq([ 0,  0,0.4,1,    1,1,0,1,  0,1,1, 0,    0,0,   0,1],2)+0.1,
		Pseq([ 0,  0,0.4,1,    1,1,0,1,  0,1,1, 0.5,  0,0.5, 0,1],4),
	],inf),                                                                /////////ADD HERE AMP FOR ALL THE TIME BEAT
	\freq, 65,
	\pan, Pseq([-1,0,1],inf),
	\dur, 0.25,
	\rq,0.01,
	\sustain, 1,

);
~tum01[6]=\filter->{arg in; GVerb.ar(in,roomsize:20,revtime:0.2,mul:0.1)+(in*2)};
~tum01[12]=\filter->{arg in; HPF.ar(in, SinOsc.ar(1/64).range(60,80))};
~tum01[13]=\filter->{arg in; LPF.ar(in, \lpf6.kr(800))};
~tum01[14]=\set->Pbind(\args, [\lpf6], \dur, 64, \lpf6, Pseq([480,340,540,680],inf));
~tum01[15]=\filter->{arg in; Normalizer.ar(in,1)};

~tum01[22]=\filter->{arg in; Limiter.ar(in, 0.7)};
~tum01[100]=\filter->{arg in; Compander.ar(in, ~kick1sec.ar, 0.1, 1, 0.1) };

);
~tum01[6]=nil;



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


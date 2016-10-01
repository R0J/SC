////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
(

{
	var freq,time, ex, delay, filter, local;

	freq= 440;
	time= freq.reciprocal+0.04;

	ex= WhiteNoise.ar(EnvGen.kr(Env([1.0,1.0,0.0,0.0], [time,0,100])));

	local= LocalIn.ar(1);

	filter= LPZ1.ar(ex+local); //apply filter
	filter=LPF.ar(filter,300+freq.reciprocal);
	delay= DelayN.ar(filter, time, time-0.01);



	LocalOut.ar(delay*0.95);

	Out.ar(0, Pan2.ar(filter,0.0))
}.play

)

(
{
	var s, z, y;
	// 10 voices of a random sine percussion sound :
	s = Mix.ar(Array.fill(10, { Resonz.ar(Dust.ar(0.2, 50), 200 + 3000.0.rand, 0.003)}) );
	// reverb predelay time :
	z = DelayN.ar(s, 0.048);
	// 7 length modulated comb delays in parallel :
	y = Mix.ar(Array.fill(7,{ CombL.ar(z, 0.1, LFNoise1.kr(0.1.rand, 0.04, 0.05), 15) }));
	// two parallel chains of 4 allpass delays (8 total) :
	4.do({ y = AllpassN.ar(y, 0.050, [0.050.rand, 0.050.rand], 1) });
	// add original sound to reverb and play it :
	s*0.5+(0.8*y)
}.scope
)

(
~infiniterev={
	arg feedback=1;
	var sig, local;

	sig=SinOsc.ar()*Decay.ar(Impulse.ar(1),0.2);
	local=LocalIn.ar(2)+sig.dup;

	15.do{local=AllpassN.ar(local, 0.06, Rand(0.001,0.06),3 )};
	LocalOut.ar(local*feedback);
	Out.ar(0, sig+local);
}.play;
)



(
~comb={
	arg feedback=1, freq=500;
	var sig, local;

	sig=CombL.ar(WhiteNoise.ar(0.5!2),freq.reciprocal,freq.reciprocal,10);
	sig = Mix.ar(Array.fill(7,{ CombL.ar(sig, 0.1, LFNoise1.kr(0.01.rand, 0.01, 0.02), 10) }));
	local=LocalIn.ar(2)+sig;
	15.do{local=AllpassN.ar(local, 0.02, Rand(0.001,0.09),3 )};
	LocalOut.ar(local*feedback);


	Out.ar(0, Limiter.ar(sig,0.2));
}.play;
)
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
(
ProxySynthDef(\string, {
	arg  sustain,  amp, pan, feedback=0.2, freq;
	var sig, env, renv, senv, local;

	env=EnvGen.kr(Env([0,1,0],[1,0.1],[\welch,\lin]),timeScale:sustain,doneAction:2);
	renv=EnvGen.kr(Env([0.01,5,0.01],[0.8,0.3],[\welch,\lin]),timeScale:sustain,doneAction:2);
	feedback=EnvGen.kr(Env([0.3,0.1,0.3]+feedback,[0.9,0.3],[\welch,\lin]),timeScale:sustain,doneAction:2);

	sig=CombL.ar(WhiteNoise.ar(0.5!2),freq.reciprocal,freq.reciprocal,10)*env;
	local=LocalIn.ar(2)+sig;
	sig=DelayN.ar(local,renv, renv);
	LocalOut.ar(sig*feedback);
	sig=MoogFF.ar(sig, freq,2,mul:0.2)+sig;
	sig=Pan2.ar(sig,pan)*env*amp;

}).add;


)


~comb2.clear;
~comb2.stop(freeTime:60);
~comb2.play(vol:0.02, fadeTime:4, numChannels:2).quant_(4);
(~comb2[0]=Pbind(
	\instrument, \string,
	\amp, 1,
	\legato, 1.2,
	\pan, 0,
	//\octave,Pseq([[5,3],[3,4]],inf),
	//\degree,1,
	\freq, Pseq([Pseq([1200,[0.6!2,-0.5,0.4,0.8,0.6,-1].flatten,4].convertRhythm),
		Pseq([1600,[0.6!2,-0.5,0.4,0.8,0.6,-1].flatten,4].convertRhythm)	],inf),
	\dur,Pseq([Pseq([16,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm),
		Pseq([32,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm)
	],inf),

);)
(
~comb2[3]=\filter->{arg in; HPF.ar(in,60)};
~comb2[6]=\filter->{arg in; LPF.ar(in,\lpfc.kr(800,2))};
~comb2[7]=\set->Pbind(\args, [\lpfc], \dur, 8, \lpfc, Pseq([280,420,400,440,480,800,620],inf));
~comb2[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[40,60],[6,8],mul:0.2)+in,0.2)};
)
~comb2[9]=nil;


~comb3.clear;
~comb3.stop(freeTime:60);
~comb3.play(vol:0.0002, fadeTime:4, numChannels:2).quant_(4);
(~comb3[0]=Pbind(
	\instrument, \combP,
	\amp, 1,
	\legato, 1.7,
	\pan, 0,
	//\octave,Pseq([[3,4],[5,4]],inf)+3,
	//\degree,1,
	\freq, Pseq([Pseq([3200,[0.6!2,-0.5,0.4,0.8,0.6,-1].flatten,4].convertRhythm),
	],inf),
	\dur,Prand([8,6],inf),//Pseq([Pseq([8,[0.25!2,-0.5,0.5,0.5,0.5,-1].flatten,1].convertRhythm)],inf),

);)
(
~comb3[3]=\filter->{arg in; HPF.ar(in,60)};
~comb3[6]=\filter->{arg in; LPF.ar(in,\lpfc.kr(2400,2))};
~comb3[7]=\set->Pbind(\args, [\lpfc], \dur, 8, \lpfc, Pseq([280,420,400,440,480,800,620],inf));
~comb3[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[40,60],[6,8],mul:0.2)+in.softclip,0.2)};
)
~comb3[9]=nil;

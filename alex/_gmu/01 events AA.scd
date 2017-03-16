////////////////////////
////////////////////////
////////////////////////
////////////////////////
////EVENTS
currentEnvironment.print;
currentEnvironment.free;
(
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;
p.clock.tempo = 137/60;
)
p.gui;



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// LEAKY NOISE

~leaky.clear;
~leaky.stop(fadeTime:10);
(
~leaky.play(vol:0.05, numChannels:2);
~leaky.quant=16;)
(
~leaky[0]=Pbind(
	\instrument, \leaky,
	\dur,32,
	\octave,Prand([8,6,7,9],inf),
	\degree, [2,9,1,-3],
	\strum, 0.2,
	\amp, 0.2,
	\legato, 0.02,
);

~leaky[4]=\filter->{arg in; GVerb.ar(in,40,8,mul:SinOsc.kr(2).range(0.2,0.9),add:in)+in};
//~leaky[5]=\filter->{arg in; FreeVerb.ar(in,room:1,damp:0.1)};
~leaky[6]=\filter->{arg in; Limiter.ar(in, 0.6)};
)
~leaky[5]=nil;



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// ONION 32

~onion.clear;
~onion.stop(fadeTime:4);
(
~onion.play(vol:0.02, fadeTime:4, numChannels:2);
~onion.quant=16;)
(
~onion[0]=Pbind(
	\instrument, \onion,
	\dur, Pseq([16,Rest(16)],inf),
	\sustain,Prand([0.5,0.4,0.3],inf),
	\freq, Prand([80,60,50,55,65,400,800],inf),
	\amp, 0.2,
	\fsaw, Prand([2,4,10,8,6],inf),
);
~onion[30]=\filter->{arg in; GVerb.ar(in,[80,60],[6,8],maxroomsize:120,add:in)};
~onion[33]==\filter->{arg in; HPF.ar(in,200)};
)


p.gui;



/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SWEEP NOISE

~sweepnoise.clear;
~sweepnoise.stop(fadeTime:20);
(
~sweepnoise.play(vol:0.08, fadeTime:4, numChannels:2);
~sweepnoise.quant=4;)
(
~sweepnoise[0]=Pbind(
	\instrument, \noisek,
	\dur,Prand([64,32],inf),
	\sustain, Prand([2,3,1,4,],inf),
	\octave,Prand([4,6,3,8],inf),
	\degree, [2,6],
	\panpos, Pseq([-1,1],inf),
	\amp, 4,
);
~sweepnoise[98]=\filter->{arg in; LPF.ar(in,\lpf6.kr(400))+(in*0.8)};
~sweepnoise[99]=\set->Pbind(\args, [\lpf6], \dur,16, \lpf6, Pseq([600,800,200],inf));


~sweepnoise[101]=\filter->{arg in; GVerb.ar(in,[80,60],[4,6],maxroomsize:120,add:in,mul:0.8)+in};
~sweepnoise[102]=\filter->{arg in; HPF.ar(in,200)};
)
~sweepnoise[101]=nil;




/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// IMP SHORT  16sec/ 0.4 legato

~imps.clear;
~imps.stop(freeTime:20);

(
~imps.play(vol:0.04, fadeTime:4, numChannels:2);
~imps.quant=16;);
(~imps[0]=Pbind(
	\instrument, \imp,
	\amp, 3,
	\legato, Pseq([0.1,0.2],inf),
	\den,Prand([8,22,15,12,24],inf),
	\dur, 32,
);


~imps[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf3.kr(2000,2)),0.7)};
~imps[4]=\set->Pbind(\args, [\lpf3], \dur,4,\lpf3, Pseq([820,600,400,1200,400,600,820]+200,inf));
~imps[5]=\filter->{arg in; GVerb.ar(in,[180,120],[4,6],mul:0.8,maxroomsize:200)+in};
~imps[8]=\filter->{arg in; HPF.ar(in, 200)};
)
~imps[5]=nil;



////////////////////////////hmmmmm
~dis4.stop;
~dis4.play(vol:0.004, fadeTime:2, numChannels:2).quant_(2);
(
~dis4[0]={
	var sig, env, fenv, noise;

	noise=EnvGen.kr(Env([0.01,0.05,0.04,0.01],[0.1,0.02,0.1],curve:\welch),\trig.tr);
	env=EnvGen.kr(Env(times:[0.01,1.5],curve:\welch),\trig.tr);
	fenv=EnvGen.kr(Env([\freq.kr,\freq.kr*6,\freq.kr,\freq.kr+(fib(10)*2)], [0.001, 0.04,0.8] ,curve:\welch),\trig.tr);

	sig=LFSaw.ar(fenv+fib(10)).lag([0.01,0.02,0.03,0.04,0.05]);
	sig=(sig+ClipNoise.ar(noise*0.4))*env;
};
)

~dis4[1]=\set-> Pbind(\args, [\trig], \dur, 16, \trig, 1);
~dis4[2]=\set-> Pbind(\args, [\freq], \dur, 16, \octave, Pseq([[4,8],5,[6,4],[6,5],[7,6]],inf), \degree, [2,4,6,8]);

~dis4[29]=\filter-> {arg in; CombN.ar(in,1,0.02)};
~dis4[30]=\filter-> {arg in; GVerb.ar(Limiter.ar(in,0.2), [20,40],[8,12,4])};



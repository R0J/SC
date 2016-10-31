
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////BUMWOOSH 32 sec   VOLUME
~bumwoosh.clear;
~bumwoosh.stop(fadeTime:60);
(
~bumwoosh.play( vol:0.1,numChannels:2, fadeTime:2);
~bumwoosh.quant_(2);)
(~bumwoosh[0]={
	var sig, env,nenv;

	env=EnvGen.kr(Env([0,1,0], [0.04,1.4]),\trig.tr);
	nenv=EnvGen.kr(Env([0,1,0,0.7,0],[0.04,0.2,1,0.2]),\trig.tr);
	sig=SinOsc.ar([\freq.kr,\freq.kr+4])+GrayNoise.ar(nenv);
	sig*env;};)

(
~bumwoosh[1]=\set->Pbind(\args, [\trig], \dur, 32, \trig, 1);
~bumwoosh[2]=\set->Pbind(\args, [\freq], \dur, 32, \freq, Pseq([[47,45]+20,[65,67]+20,[55,57],[75,77]+120],inf));

~bumwoosh[30]=\filter->{arg in; FreeVerb.ar(in,room:2,damp:0.7,add:in)*LFSaw.kr([3,1]).lag(0.2)};
~bumwoosh[31]=\filter-> {arg in; Limiter.ar(GVerb.ar(in,120,[4,6],0.7,mul:0.7,add:in),0.6)};

)

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////SWEEP NOISE SOFTt rand 8,16,32

~sweepnoise.clear;
~sweepnoise.stop(fadeTime:20);
(
~sweepnoise.play(vol:0.1, fadeTime:4, numChannels:2);
~sweepnoise.quant_(4);)
(
~sweepnoise[0]=Pbind(
	\instrument, \noisek,
	\dur,Prand([8,16,32],inf),
	\sustain, 8,
	\octave,9,
	\degree, [2,6],
	\panpos, Pseq([-1,1],inf),
	\amp, 12,
);)
(
~sweepnoise[3]=\filter->{arg in; LPF.ar(in,\lpf6.kr(400))+(in*0.8)};
~sweepnoise[7]=\set->Pbind(\args, [\lpf6], \dur,16, \lpf6, Pseq([600,800,200],inf));


~sweepnoise[30]=\filter->{arg in; GVerb.ar(in,[80,60],[10,20],maxroomsize:120,add:in,mul:0.8)+in};
~sweepnoise[32]=nil;
)



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// ONION 16/32 rand sustain 2

~onion.clear;
~onion.stop(fadeTime:30);
(
~onion.play(vol:0.06, fadeTime:4, numChannels:2);
~onion.quant_(4);)
(
~onion[0]=Pbind(\instrument, \onion,
	\dur, Prand([16,32],inf)/2,
	\sustain,2,
	\freq, 65,
	\amp, 0.2,
	\fsaw, Prand([2,4,10,80,60],inf),
);
~onion[30]=\filter->{arg in; GVerb.ar(in,[80,60],[4,3],maxroomsize:120,add:in)};)





/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// IMP SHORT  16sec/ 0.4 legato

~imps.clear;
~imps.stop(freeTime:60);

(
x=Group.new;
~imps.play(vol:6, fadeTime:4, numChannels:2, group:x);
~imps.quant_(4););
(~imps[0]=Pbind(
	\instrument, \imp,
	\amp, 3,
	\legato, Pseq([0.4,0.3],inf),
	\den,Prand([30,18,24],inf),
	\dur, 16,
);


~imps[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf3.kr(2000,2)),0.7)};
~imps[4]=\set->Pbind(\args, [\lpf3], \dur,4,\lpf3, Pseq([820,600,400,1200,400,600,820]+200,inf));
~imps[5]=\filter->{arg in; GVerb.ar(in,[180,120],[4,6],mul:0.8,maxroomsize:200)+in};
)
~imps[5]=nil;



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// FEEDBACK LOOP 32 sec/0.2 legato

~feedloopevnt.clear;
~feedloopevnt.stop(freeTime:60);
(
~feedloopevnt.play(vol:0.01, fadeTime:4, numChannels:2);
~feedloopevnt.quant_(4);)
(~feedloopevnt[0]=Pbind(
	\instrument, \string,
	\amp, 1,
	\legato, 0.2,
	\pan, 0,
	\freq,10,
	\dur,32,
);)
(
~feedloopevnt[3]=\filter->{arg in; HPF.ar(in,60)};
~feedloopevnt[6]=\filter->{arg in; LPF.ar(in,\lpfs.kr(240,2))};
~feedloopevnt[7]=\set->Pbind(\args, [\lpfs], \dur, 8, \lpfs, Pseq([280,420,300,840,480,800,320]+400,inf));
~feedloopevnt[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[40,60],[4,6],mul:0.2)+in.softclip,0.8)};
)
~feedloopevnt[9]=nil;






//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TSLONG every 4/8 sec

~tslongevnt.clear;
~tslongevnt.stop(fadeTime:40);
(
~tslongevnt.play(vol:0.02, fadeTime:4, numChannels:2);
~tslongevnt.quant_(4);)
(~tslongevnt[0]=Pbind(
	\instrument, \tss,
	\amp,1,
	\freq, Pseq([100,200,140,400],inf),
	\dur, Pseq([Pn(0.01,50),Pseq([Rest(7.5),Rest(3.5)],1)],inf),
	\legato, Pseq([0.02,0.05],inf),
	\pan, Pseq([-1,1,0],inf),
	\noise, 1,
	\hpf, 800,

);

~tslongevnt[6]=\filter->{arg in; HPF.ar(in,SinOsc.kr(6).range(90,11000))};
~tslongevnt[7]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,0.2,mul:0.9,add:in*2),0.9)};
~tslongevnt[8]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,4,mul:0.4,add:in*2),0.9)};
)


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TSLONG every 4/8 sec

~bowevnt.clear;
(
~bowevnt.play(vol:0.01, fadeTime:4, numChannels:2);
~bowevnt.quant_(4);)
(
~bowevnt[0]=Pbind(
	\instrument, \bow,
	\dur,Prand([8,4,16],inf),
	\sustain,1,
	\octave, Pseq([Pn([1,3],16),Pn([2,3],16)],inf),
	\degree, [6,4]-Prand([2,1,3],inf),
	\amp, 0.2,
);)
(
~bowevnt[3]=\filter->{arg in; LPF.ar(in,\lpf6.kr(400))+(in*0.01)};
~bowevnt[7]=\set->Pbind(\args, [\lpf6], \dur,12, \lpf6, Pseq([600,800,200],inf));


~bowevnt[30]=\filter->{arg in; GVerb.ar(in,[80,60],[6,4],maxroomsize:120,add:in,mul:0.2)+in};
~bowevnt[32]=\filter->{arg in; Limiter.ar(in,0.7)};
)
p=ProxySpace.push(s);

(
~o[0]={
	var sig, env, lenv;
	//lpf sound panned with verb
	//env=EnvGen.kr(Env([0,0.5,0.8,0], [0.2,0.4,0.4],[\welch,\lin, \sin]),\trig.tr);
	//lenv=EnvGen.kr(Env([0,0.5,0.8,0],[600,1200,200],[\welch,\lin, \sin]),\trig.tr);

	env=Decay.kr(Impulse.kr(1/4),0.1);
	lenv=Decay.kr(Impulse.kr(1),0.01).range(6000,400);


	sig=LFPulse.ar(35)*LFSaw.ar(LFSaw.kr(1/64).range(220,1600));
	sig=LPF.ar(sig,lenv);
	sig=Compander.ar(sig,thresh:0.6,thresh:0.4,slopeBelow:1,slopeAbove:1);
	sig=Pan2.ar(sig,SinOsc.kr(1/2));
	sig=Limiter.ar(sig,1);
	sig=Clip.ar(sig);

};
)
~o[1]=Pbind(\args, [\trig], \dur, 1, \trig, 1);
~o[2]=\filter->{arg in; GVerb.ar(in,180,12,mul:0.9)+in};
~o.play( vol:0.5, fadeTime:1, numChannels:2);


(
ProxySynthDef(\sawlpf, {
	arg  sustain,  amp, pan, freq, f2, lpf;
	var sig, env, lenv;
	env=EnvGen.kr(Env([0,0.5,0.8,0],[0.5,0.8,0.001],[\welch, \sin, \lin]),timeScale:sustain,doneAction:2);
	lenv=EnvGen.kr(Env([lpf, lpf+800, lpf+400],[0.2,0.1],\welch),timeScale:sustain,doneAction:2);
	sig=LFPulse.ar(f2)*LFSaw.ar(freq);
	sig=LPF.ar(sig,lenv);
	sig=Compander.ar(Limiter.ar(sig,1),thresh:0.6,thresh:0.4,slopeBelow:1,slopeAbove:1);
	sig=Pan2.ar(NumChannels.ar(sig,1),pan);
	sig=sig*env*amp;

}).add;
)


~sawlpf.clear;
(
~sawlpf.play(vol:0.8, numChannels:2);
~sawlpf.quant=16;)
(
~sawlpf[0]=Pbind(
	\instrument, \sawlpf,
	\dur,2,
	\octave,Prand([4,6,5],inf),
	\degree, [3,9,6,2],
	\lpf, 400,
	\f2, 5,
	\pan, Prand([-1,1,0,0.5,-0.2,-0.6,0.2],inf),
	\amp, 0.2,
	\sustain, 5,
);

~sawlpf[4]=\filter->{arg in; GVerb.ar(in,60,8,mul:0.7)+in};
~sawlpf[5]=\filter->{arg in; HPF.ar(in, 100)};
)
~sawlpf[6]=\filter->{arg in; LPF.ar(in, \lpf5.kr(200))};
~sawlpf[7]=\set->Pbind(\args, [\lpf5], \dur, 8, \lpf5, Pseries(200,20,180));

~sawlpf[5]=nil;
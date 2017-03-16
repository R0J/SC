p=ProxySpace.push(s);
p=makeTempoCl

(
ProxySynthDef(\osc, {
	arg  sustain,  amp, pan;
	var sig, sig1, sig2, sig3, sig4, env;
	env=EnvGen.kr(Env([0,1,0],[1,0.001],\welch),timeScale:sustain,doneAction:2);
	sig1=Mix.fill(4, SinOsc.ar(\freq.kr+fib(4).reciprocal));
	sig2=Mix.fill(4, SinOsc.ar(\freq.kr-(\freq.kr/2)));
	sig3=Mix.fill(4, SinOsc.ar(\freq.kr-(\freq.kr/4)));
	sig4=BPF.ar(WhiteNoise.ar(0.8),\freq.kr-fib(12),rq:LFNoise0.kr(LFSaw.kr(2).range(1,15)).range(0.02,2));
	sig=sig1+sig4;
	sig=sig*env*amp;

}).add;
)


(
~osc1.play;
~osc1.fadeTime=1;
~osc1.quant=4;
)
(~osc1[1]=Pbind(
	\instrument, \osc,
	\dur, Pseq([2,4],inf),
	\legato,2,
	\octave, Pseq([Pseq([2,4,1,6,3],2),Pseq([7,2,1,6,3],2)],inf),
	\strum, 4,
	\degree,[1,3,7],
	\amp, 0.3

););
(
~osc1[4]=\filter->{arg in; GVerb.ar(in,[40,80],[4,2],mul:0.4,add:in)*4};
~osc1[5]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf6.kr(1400)),0.5)};
~osc1[4]=nil;
)



(
ProxySynthDef(\tim, {
	arg  sustain,  amp, pan;
	var sig, sig1, sig2, sig3, sig4, env, lenv;
	env=EnvGen.kr(Env([0,1,0],[0.4,0.001],\welch),timeScale:sustain,doneAction:2);
	lenv=EnvGen.kr(Env([12000,400,100],[0.4,0.001],\welch),timeScale:sustain,doneAction:2);

	sig1=Mix.fill(4, SinOsc.ar(\freq.kr+fib(4).reciprocal));
	sig2=HPF.ar(GrayNoise.ar(0.6),\hpf.kr(800));
	sig3=LFSaw.ar(\freq.kr/2,[1,2,0.5,4])*LFSaw.ar(0.25);
	sig=sig2+sig3+sig1;
	sig=LPF.ar(sig,lenv);
	sig=sig*env*amp;

}).add;
)


(
~osc2.play;
~osc2.fadeTime=1;
~osc2.quant=4;
)
(~osc2[1]=Pbind(
	\instrument, \tim,
	\dur,2,
	\legato,Prand([ 0.1,0.4,0.2],inf),
	\octave,[2,3]+1,
	\hpf, 8000,
	//\strum, 4,
	\degree,[1,3,7],
	\amp, 0.7

););
(
~osc2[4]=\filter->{arg in; GVerb.ar(in,[40,60],[1,2],mul:0.2,add:in)*4};
~osc2[5]=\filter->{arg in; Limiter.ar(in,0.7)};
//~osc2[4]=nil;
)
{LFSaw.ar(2,SinOsc.kr(1.02))}.play;

{Limiter.ar(SinOsc.ar([45,85],Decay.kr(Impulse.kr([5,3]),0.02)*[8,12])*Decay.kr(Impulse.kr([5,3]),0.4),0.8)}.play;
{Limiter.ar(SinOsc.ar([40,42])*LFPulse.ar(0.25),0.8)}.play;

s.boot;


(
~test[1]={
	var noise, filter, sig, env, freq;

	freq=SinOsc.kr(1/16).range(120,4000);
	env=Decay.kr(Impulse.kr(0.25),4);
	sig=LFPulse.ar(45!2);
	filter=BPF.ar(sig,(LFSaw.kr(8)*LFPulse.kr(3)).range(freq,4),LFNoise2.kr(4).range(0.001,4),8);
	filter=Normalizer.ar(filter,8);
	filter=HPF.ar(filter,80);
	filter=LPF.ar(filter,\fl.kr(1800));
	filter=Limiter.ar(filter,0.7);

};
~test[30]=\filter->{arg in; GVerb.ar(in,120,[7,12],mul:0.5,add:in)};
~test[31]=\filter->{arg in; HPF.ar(in,400)};
~test[35]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/64).range(1200,2000))};
~test[59]=\filter->{arg in; Compander.ar(in, ~kick1sec.ar, 0.8, 1, 0.1) };
~test[62]=\filter->{arg in; in*SinOsc.ar([1/16,1/8]).range(0.1,0.8)};

)
~test.quant=16;
~test.play(vol:0.14);
~test.clear;
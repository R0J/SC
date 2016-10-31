p=ProxySpace.push(s);

(
~door[0]={
	var sig, env, mod;
	env=Decay2.kr(Impulse.kr(MouseX.kr(0.5,20)),0.1,decayTime:MouseY.kr(0.1,2));
	mod=[1,5,6,7,9,2,1,5,4,7,5,12,10,14,16];
	sig=LFSaw.ar(45*mod);
	sig=FreeVerb.ar(sig,damp:0.9);
	sig=Wrap.ar(sig,-0.8,0.8).lag(0.02);
	sig=Limiter.ar(sig*env,level:0.2);
	sig=HPF.ar(sig,200);


};
)
~door[5]=\filter->{arg in; GVerb.ar(in,MouseY.kr(10,142),MouseY.kr(1,12))};
~door[6]=\filter->{arg in; Limiter.ar(in,1)};


~door.play(numChannels:2);



(
~door[0]={
	var sig, env, mod;
	env=Decay2.kr(Impulse.kr(MouseX.kr(0.5,2)),0.1,decayTime:MouseY.kr(0.1,4));
	mod=[1,5,6,7,9,2,1,5,4,7,5,12,10,14,16];
	sig=DWGBowed.ar(35);
	sig=Limiter.ar(sig*env,level:0.2);
	sig=LPF.ar(sig,800);


};
)
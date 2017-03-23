(
~mic[0]={
var sig, env;
	env=Decay2.ar(Impulse.ar([2,5]),decayTime:0.2);
	sig=AudioIn.ar([0,1],7);
	sig=DelayN.ar(sig,10,4,2);
	sig=FreeVerb.ar([sig,sig],room:2);

	sig=sig*env*4;

};
)
~mic.quant=4;
~mic.play(numChannels:2, fadeTime:4);
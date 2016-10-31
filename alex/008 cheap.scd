{CheapVerb.ar(SinOsc.ar(80!2)*Decay.ar(Impulse.ar(0.5),0.02),1)}.play;
{Clip.ar(Wrap.ar(VarSaw.ar([1,8])*LFSaw.ar(45),-0.7,0.7),-0.5,0.5)}.play;

p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate=true;


~cheap.play(vol:1, numChannels:2).quant_(4);
(
~cheap[0]=Pbind(
	\instrument, \cheap,
	\dur,Pseq([Pseq([2,[0.25!7,-0.25,0.25,0.1,-1].flatten,1].convertRhythm)],inf),
	\dec, 2,
	\octave, [4,2],
	\degree, [4,6,2],
	\amp, 0.2,
	\strum, 0.15,

);
)
~cheap[4]=\filter->{arg in; CheapVerb.ar(in,4)};
~cheap[4]=nil;

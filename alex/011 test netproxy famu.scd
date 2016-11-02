p=NetProxy.connect;
p.metro(4,1000);
p.bpm(127);
s.latency=0;
currentEnvironment.free;
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;

p.clock.tempo = 127/60;
p.gui;

AutoIDE.run;






~kick.rebuild;
~kick.clear;
~kick.stop(freeTime:10);
~kick.quant_(4);
~kick.play(vol:0.7, fadeTime:4, numChannels:2);
(~kick[0]=Pbind(
	\instrument, \kick,
	\amp, 1,
	\legato, 0.2,
	\pan, Pseq([-1,1],inf),
	\freq, 400,
	\dur, 4,
);)
~kick[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(120,2)),0.7)};
~kick[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[2,1],mul:0.2)+in,0.7)};

~kick2.rebuild;
~kick2.clear;
~kick2.quant_(4);
~kick2.stop(freeTime:10);
~kick2.play(vol:0.7, fadeTime:4, numChannels:2);
(~kick2[0]=Pbind(
	\instrument, \kick,
	\amp, 1,
	\legato, 0.1,
	\pan, Pseq([-1,1],inf),
	\freq, 1450,
	\dur, 4,
);)
~kick2[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(120,2)),0.7)};
~kick2[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[2,1],mul:0.2)+in,0.7)};

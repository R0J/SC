currentEnvironment.free;
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;

p.clock.tempo = 127/60;
p.gui;

AutoIDE.run;


~noisek1.clear;
~noisek1.play(vol:0.9, fadeTime:4, numChannels:2).quant_(4);
(
~noisek1[0]=Pbind(
	\instrument, \noisek,
	\dur,Pseq([Pseries(1,(-0.05),18),Pseries(0.1,0.01,18)],inf),
	\sustain, 1,
	\octave,9,
	\degree, [2,6],
	\panpos, Pseq([-1,1],inf),
	\amp, 12,
);)
(
~noisek1[3]=\filter->{arg in; LPF.ar(in,\lpf6.kr(400))+(in*0.8)};
~noisek1[7]=\set->Pbind(\args, [\lpf6], \dur,12, \lpf6, Pseq([600,800,200],inf));


~noisek1[30]=\filter->{arg in; GVerb.ar(in,[80,60],[10,20],maxroomsize:120,add:in,mul:0.8)+in};
~noisek1[32]=nil;
)

~noisek2.clear;
~noisek2.play(vol:0.1, fadeTime:4, numChannels:2).quant_(4);
(
~noisek2[0]=Pbind(
	\instrument, \noisek,
	\dur,Prand([8,16,32],inf),
	\sustain, 1,
	\octave,9,
	\degree, [2,6],
	\panpos, Pseq([-1,1],inf),
	\amp, 12,
);)
(
~noisek2[3]=\filter->{arg in; LPF.ar(in,\lpf6.kr(400))+(in*0.8)};
~noisek2[7]=\set->Pbind(\args, [\lpf6], \dur,12, \lpf6, Pseq([600,800,200],inf));


~noisek2[30]=\filter->{arg in; GVerb.ar(in,[80,60],[10,20],maxroomsize:120,add:in,mul:0.8)+in};
~noisek2[32]=nil;
)

////////////////////////////////////////////////////////////////////////////////////////////////////these two
~kick.clear;
~kick.stop(freeTime:10);
~kick.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~kick[0]=Pbind(
	\instrument, \tum,
	\amp, 1,
	\legato, 0.8,
	\pan, 0,
	\rq,2,
	\freq, 40,
	\dur, Pseq([Pseq([4,[0.25!5,-1,0.25,-0.25,0.25!3,-0.5].flatten,4].convertRhythm),
		Pseq([4,[0.25!5,-1,0.25,-0.25,0.25!3,-0.25,0.25].flatten,1].convertRhythm),],inf),

);)
~kick[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,SinOsc.kr(1/32).range(120,620)),0.7)};
~kick[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,2,mul:0.1)+in,0.7)};


~kickg.clear;
~kickg.stop(freeTime:10);
(h=Group.new;
~kickg.play(vol:0.01, fadeTime:1, numChannels:2, group:h).quant_(4);)
(~kickg[0]=Pbind(
	\instrument, \kickfill,
	\amp, 1,
	\legato,0.03,
	\pan, 0,
	\freq, 55,
	\dur,3,

);)
~kickg[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,SinOsc.kr(1/32).range(420,620)),0.7)};
~kickg[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,2,mul:0.1)+in,0.7)};
~kick[4]=nil;



~tss8082.clear;
~tss8082.stop(fadeTime:40);
~tss8082.play(vol:0.4, fadeTime:1, numChannels:2).quant_(4);
(~tss8082[0]=Pbind(
	\instrument, \tss808,
	\amp,\amp,Pseq([1,0.5,0.1,2],inf)*6,,
	\dur,3,
	\legato, 0.6,
	\hpf,6000,

););
~tss8082[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,10,SinOsc.kr(1/64).range(0.8,1),mul:0.8,add:ts*2),0.9)};
~tss8082[7]=nil;

~tss8082[6]=nil;
///////////////////////////////////////////////////////////////////////////////////////////////////


~tss8081.clear;
~tss8081.stop(fadeTime:40);
~tss8081.play(vol:0.4, fadeTime:4, numChannels:2).quant_(4);
(~tss8081[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([1,0.5],inf),
	\dur,0.25,
	\legato, 1.2,
	\hpf,10000,

););
~tss8081[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,10,SinOsc.kr(1/64).range(8,1),mul:0.8,add:in*2),0.9)};
~tss8081[7]=\filter->{arg in;HPF.ar(in,SinOsc.kr(1/32).range(8800,4400))};

~tss8081[6]=nil;

////////////////////////////////////////////////////////////////////////////////////////////////////////////

~comb3.clear;
~comb3.stop(freeTime:60);
~comb3.play(vol:0.1, fadeTime:4, numChannels:2).quant_(4);
(~comb3[0]=Pbind(
	\instrument, \string,
	\amp, 1,
	\legato, 1.4,
	\pan, 0,
	\freq,Pseq([Pseries(5,1,20),Pseries(1,0.5,20),Pseries(2,4,5)],inf),
	\dur,8,
);)
(
~comb3[3]=\filter->{arg in; HPF.ar(in,60)};
~comb3[6]=\filter->{arg in; LPF.ar(in,\lpfs.kr(240,2))};
~comb3[7]=\set->Pbind(\args, [\lpfs], \dur, 8, \lpfs, Pseq([280,420,300,840,480,800,320],inf));
~comb3[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[40,60],[1,0.5],mul:0.2)+in.softclip,0.8)};
)
~comb3[9]=nil;


~comb4.clear;
~comb4.stop(freeTime:60);
~comb4.play(vol:0.2, fadeTime:4, numChannels:2).quant_(4);
(~comb4[0]=Pbind(
	\instrument, \string,
	\amp, 1,
	\legato, 1.4,
	\pan, 0,
	\freq,Pseq([Pseries(80,(-4),20),Pseries(1,4,8),Pseries(60,(-4),5)],inf),
	\dur,12,
);)
(
~comb4[3]=\filter->{arg in; HPF.ar(in,60)};
~comb4[6]=\filter->{arg in; LPF.ar(in,\lpfc.kr(240,2))};
~comb4[7]=\set->Pbind(\args, [\lpfc], \dur, 8, \lpfc, Pseq([280,420,400,440,480],inf));
~comb4[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[2,4,6],[6,8],mul:0.1)+in.softclip,0.7)};
)
~comb4[9]=nil;





~tum2.clear;
~tum2.stop(fadeTime:40);
(f=Group.new;
~tum2.play(vol:0.5, fadeTime:4, numChannels:2, group:f).quant_(4);)
(~tum2[0]=Pbind(
	\instrument, \tum,
	\amp,1,
	\octave, Pseq([4!16,3!16].flatten,inf)-2,
	\degree, [4,2]-2,
	\dur, Pseq([Pseq([2,[-0.25,-0.75,-0.5,0.25!2,-2].flatten,1].convertRhythm),Rest(12)],inf),
	\rq, 2,
	\sustain, 1,

);
~tum2[4]=nil;

~tum2[6]=\filter->{arg in; GVerb.ar(in,roomsize:20,revtime:1,earlyreflevel:0.1,mul:SinOsc.kr(1/32).range(0.01,0.1))+in};
);
~tum2[6]=nil;


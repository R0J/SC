
currentEnvironment.print;
currentEnvironment.free;
//p.arProxyNames.collect({arg n; var synth=(p[n.asSymbol]); synth.stop(30)});
(
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;
p.clock.tempo = 137/60;
)
p.gui;

~kick1sec.clear;

~kick1sec.stop(freeTime:10);
(
~kick1sec.play(vol:0.9, fadeTime:4, numChannels:2);
~kick1sec.quant.(16);
)
(~kick1sec[0]=Pbind(
	\instrument, \kick,
	\amp, 2,
	\legato, 0.9,
	\pan, Pseq([-1,1],inf),
	\freq, [45,47],
	\dur, 1,
);
~kick1sec[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(120,2)),0.4)};
~kick1sec[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,[2,1],mul:0.2)+in,0.7)};
~kick1sec[8]=\filter->{arg in; HPF.ar(in,10)};

)

~kick3sec.clear;

~kick3sec.stop(freeTime:10);
(
~kick3sec.play(vol:2, fadeTime:4, numChannels:2);
~kick3sec.quant.(16);)
(~kick3sec[0]=Pbind(
	\instrument, \kick,
	\amp, 2,
	\legato, 0.2,
	\pan, Pseq([1,-1],inf),
	\freq, [45,47]+10,
	\dur, 6/5,
);
~kick3sec[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,20,[0.2,0.4],mul:0.2)+in,0.7)};
~kick3sec[8]=\filter->{arg in; HPF.ar(in,LFSaw.kr(1/64).range(40,100))};
~kick3sec[24]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(160,2)),0.2)};


)


~tss808long.clear;
~tss808long.stop(fadeTime:40);

(
~tss808long.play(vol:0.51, fadeTime:1, numChannels:2);
~tss808long.quant_(4))
(~tss808long[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([1,0.5,0.1,0.4],inf),
	\dur,4,
	\legato, 0.07,
	\hpf,4000,

););
~tss808long[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,40,4,mul:0.8,add:ts*2),0.9)};
~tss808long[7]=nil;


~tum02.clear;
~tum02.stop(fadeTime:40);

(
~tum02.quant_(16);
~tum02.play(vol:0.8, fadeTime:4, numChannels:2);)
(~tum02[0]=Pbind(
	\instrument, \tum,
	\amp,Pseq([
		Pseq([ 0,0,0.4,1,  1,1,0,1,  0,1,1, 0,  0,0,0,1],2),

	],inf),
	\octave,Prand([Pn(4,64),Pn(2,64),Pn(4,64),Pn(3,64)],inf),
	\degree, 1,
	\dur, Pseq([Rest(2),Pseq([2,[0.25!4, -0.5, 0.2,-0.1,0.25!2].flatten].convertRhythm)],inf),
	\rq, Pseq([Pn(0.2,64),Pn(0.02,64),Pn(1,64)],inf),
	\sustain, 6,

);
~tum02[6]=\filter->{arg in; GVerb.ar(in,roomsize:40,revtime:2,earlyreflevel:0.8,mul:SinOsc.kr(1/32).range(0.01,0.4))+in};
~tum02[12]=\filter->{arg in; HPF.ar(in, SinOsc.ar(1/64).range(2420,600))};
~tum02[13]=\filter->{arg in; LPF.ar(in, \lpf6.kr(200))};
~tum02[14]=\set->Pbind(\args, [\lpf6], \dur, 16, \lpf6, Pseq([400,200,440,280],inf));

);
~tum02[6]=nil;



~kick16sec.clear;

~kick16sec.stop(freeTime:10);
(
~kick16sec.play(vol:0.8, fadeTime:4, numChannels:2);
~kick16sec.quant_(4);)
(~kick16sec[0]=Pbind(
	\instrument, \kick,
	\amp, 1,
	//\legato, 0.2,
	\pan, 0,
	\freq, [45,47]+10,
	\dur, Pseq([Rest(12),Pseq([4,[0.1!2,-1,0.1,0.2,-0.1,0.1!2,-0.1].flatten].convertRhythm)],inf)
);
~kick16sec[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(420,2)),0.7)};
~kick16sec[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[1,2],mul:0.2)+in,0.7)};
~kick16sec[8]=\filter->{arg in; HPF.ar(in,30)};


)
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////mel

~mel01.clear;
~mel01.stop(freeTime:60);

(
~mel01.play(vol:0.002, fadeTime:4, numChannels:2,);
~mel01.quant_(16);
);
(~mel01[0]=Pbind(
	\instrument, \mel,
	\amp, 3,
	\legato, 0.7,
	\octave, Pseq([4,6,5],inf),
	\degree, [4,2,8,6],
	\strum, 0.5,
	\noise, Prand([Pn(0.8,13),Pn(0.1,13*3),Pn(0.2,13),Pn(1,26)],inf),
	\dur, Pseq([0.25!12,Rest(1)].flatten,inf),
);


~mel01[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf3.kr(200,2)),0.7)};
~mel01[4]=\set->Pbind(\args, [\lpf3], \dur,16,\lpf3, Pseq([820,600,400,1200,400,600,820],inf));
~mel01[5]=\filter->{arg in; GVerb.ar(in,[50,20],[1,0.2],mul:0.2,maxroomsize:200)+in};
)
~mel01[5]=nil;

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////impcont
~impcont.clear;
~impcont.stop(freeTime:60);

(
~impcont.play(vol:0.2, fadeTime:4, numChannels:2,);
~impcont.quant_(16);
);
(~impcont[0]=Pbind(
	\instrument, \imp,
	\amp, 3,
	\legato, Pseq([0.4,0.3,0.7],inf),
	\den,Prand([10,18,12],inf),
	\dur, 16,
);


~impcont[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf3.kr(200,2)),0.7)};
~impcont[4]=\set->Pbind(\args, [\lpf3], \dur,16,\lpf3, Pseq([820,600,400,1200,400,600,820],inf));
~impcont[5]=\filter->{arg in; GVerb.ar(in,[180,120],[14,16],mul:0.8,maxroomsize:200)+in};
)
~impcont[5]=nil;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////sweepnoise

~sweepnoise.clear;
~sweepnoise.stop(fadeTime:20);
(
~sweepnoise.play(vol:0.3, fadeTime:4, numChannels:2);
~sweepnoise.quant_(16);)
(
~sweepnoise[0]=Pbind(
	\instrument, \noisek,
	\dur,Prand([16,32],inf),
	\sustain, 4,
	\octave,Prand([4,6,3,8],inf),
	\degree, [2,6],
	\panpos, Pseq([-1,1],inf),
	\amp, 4,
);)
(
~sweepnoise[3]=\filter->{arg in; LPF.ar(in,\lpf6.kr(400))+(in*0.8)};
~sweepnoise[7]=\set->Pbind(\args, [\lpf6], \dur,16, \lpf6, Pseq([600,800,200],inf));


~sweepnoise[30]=\filter->{arg in; GVerb.ar(in,[80,60],[4,6],maxroomsize:120,add:in,mul:0.8)+in};
~sweepnoise[35]=\filter->{arg in; HPF.ar(in,200)};
~sweepnoise[32]=nil;
)



~combdrone.clear;
~combdrone.stop(freeTime:60);
(
~combdrone.play(vol:0.07, fadeTime:4, numChannels:2);
~combdrone.quant_(16);)
(~combdrone[0]=Pbind(
	\instrument, \bow,
	\amp, 1,
	\legato, Prand([0.8,0.7,0.6],inf),
	\strum, 0.25,
	\pan, 0,
	\octave, Prand([3,[2,4],4,6],inf),
	\degree,Prand([2,4,[6,2],4],inf),
	\dur,Pseq([8,Rest(8)],inf),
);)
(
~combdrone[2]=\filter->{arg in; CombC.ar(in, 16,[4,8],32)};
~combdrone[3]=\filter->{arg in; HPF.ar(in,40)};
~combdrone[6]=\filter->{arg in; LPF.ar(in,600)};
//~combdrone[7]=\set->Pbind(\args, [\lpfc], \dur, 8, \lpfc, Pseq([280,420,400,440,480],inf));
~combdrone[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[2,40,6],[16,8,12],mul:0.1)+in.softclip,0.7)};
)
~combdrone[9]=nil;



~tum01.clear;
~tum01.stop(fadeTime:40);

(
~tum01.quant_(16);
~tum01.play(vol:0.2, fadeTime:4, numChannels:2);)
(~tum01[0]=Pbind(
	\instrument, \tum,
	\amp,Pseq([
		Pseq([ 0,0,0.4,1,  1,1,0,1,  0,1,1, 0,  0,0,0,1],2),

	],inf),
	\octave,Prand([Pn(4,64),Pn(2,64),Pn(4,64),Pn(3,64)],inf),
	\degree, 1,
	\dur, 0.25,
	\rq, Pseq([Pn(0.2,64),Pn(0.02,64),Pn(1,64)],inf),
	\sustain, 6,

);
~tum01[6]=\filter->{arg in; GVerb.ar(in,roomsize:40,revtime:2,earlyreflevel:0.8,mul:SinOsc.kr(1/32).range(0.01,0.4))+in};
~tum01[12]=\filter->{arg in; HPF.ar(in, SinOsc.ar(1/64).range(2420,600))};
~tum01[13]=\filter->{arg in; LPF.ar(in, \lpf6.kr(200))};
~tum01[14]=\set->Pbind(\args, [\lpf6], \dur, 16, \lpf6, Pseq([400,200,440,280],inf));

);
~tum01[6]=nil;


//Pseq([ 1,0,0.4,1,  1,0.4,0,1,  0,1, 1, 0,  0,0,0,1],2),
	//	Pseq([ 1,0,0.6,1,  1,0.6,0,1,  0,0.5,1,0,  0,0,0,1],2),
		//Pseq([ 1, 1, 1,1,  1,0.6,0,1,  0,0.5,1,1,  0,0,0,1],2),




~rhmdrum.clear;
~rhmdrum.stop(fadeTime:40);
~rhmdrum.play(vol:0.4, fadeTime:4, numChannels:2);
~rhmdrum.quant_(16);
(~rhmdrum[0]=Pbind(
	\instrument, \kick,
	\amp,1,
	\freq, [44,45]+20,
	\dur, Pseq([Pseq([4,[0.1!4,-1,0.1,-0.1,0.1!3,-0.1].flatten,3].convertRhythm),
		Pseq([4,[0.1!4,-1,0.1,0.1,0.1!3,-0.1].flatten,3].convertRhythm)
	],inf),
	\top, 40,

);
~rhmdrum[6]=\filter->{arg in; Limiter.ar(GVerb.ar(in,30,mul:0.2,add:in*0.9),0.8)};
~rhmdrum[7]=\filter->{arg in; HPF.ar(in,SinOsc.kr(1/32).range(40,60))};
~rhmdrum[8]=\filter->{arg in; LPF.ar(in,SinOsc.kr(1/64).range(2800,200))};)
~rhmdrum[6]=nil;
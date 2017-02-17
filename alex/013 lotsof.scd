
currentEnvironment.print;
currentEnvironment.free;
//p.arProxyNames.collect({arg n; var synth=(p[n.asSymbol]); synth.stop(30)});
(
p=ProxySpace.push(s);
p.makeTempoClock;
ProxySynthDef.sampleAccurate = true;
p.clock.tempo = 127/60;
)
p.gui;




/////////////////////////////////////////////////////////////////////////////////////////////////////////kick group
~kick1sec.clear;

~kick1sec.stop(freeTime:10);
(
~kick1sec.play(vol:0.8, fadeTime:4, numChannels:2);
~kick1sec.quant.(16);
)
(~kick1sec[0]=Pbind(
	\instrument, \kick,
	\amp, 2,
	\legato, 0.4,
	\pan, 0,
	\freq, [45,47]+20,
	\dur, Pseq([Rest(3),1],inf),
);
~kick1sec[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(220,2)),0.4)};
~kick1sec[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,40,[0.5,1],mul:0.2)+in,0.7)};
~kick1sec[8]=\filter->{arg in; HPF.ar(in,30)};

)


~kick3sec.clear;

~kick3sec.stop(freeTime:10);
(
~kick3sec.play(vol:1, fadeTime:4, numChannels:2);
~kick3sec.quant.(16);)
(~kick3sec[0]=Pbind(
	\instrument, \kick,
	\amp, 2,
	\legato, 0.3,
	\pan, 0,
	\freq, [45,47]+20,
	\dur, Pseq([Rest(3),1],inf),
);
~kick3sec[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,20,[0.2,0.4],mul:0.2)+in,0.7)};
~kick3sec[8]=\filter->{arg in; HPF.ar(in,LFSaw.kr(1/64).range(40,100))};
~kick3sec[24]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf4.kr(260,2)),0.2)};
)

/////////////////////////////////////////////////////////////////////////////////////////////////////////tss
~tss808long.clear;
~tss808long.stop(fadeTime:40);

(
~tss808long.play(vol:0.2, fadeTime:1, numChannels:2);
~tss808long.quant_(16))
(~tss808long[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([1,0.5,0.8,1],inf),
	\dur,0.25,
	\legato,1,
	\hpf,Pseq([Pn(4000,64),Pn(8000,64),Pn(9200,64)],inf),

););
~tss808long[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,20,1,mul:0.8,add:ts*2),0.9)};
~tss808long[7]=nil;


/////////////////////////////////////////////////////////////////////////////////////////////////////////////tsss


~tum02.clear;
~tum02.stop(fadeTime:40);
(
~tum02.quant_(16);
~tum02.play(vol:0.8, fadeTime:4, numChannels:2);)
(~tum02[0]=Pbind(
	\instrument, \tum,
	\amp,Pseq([
		Pseq([ 0,1,0.2,1,  1,1,0,1,  0,1,1,0,  0,0,0.5,1],2),
		Pseq([ 0,1,0.7,1,  1,1,0,1,  0,1,1,0,  0,1,0.5,1],3),
	],inf),
	\octave,Prand([Pn(4,64),Pn(2,64),Pn(4,64),Pn(3,64)],inf),
	\degree, 2,
	\dur, 0.25,
	\rq, Pseq([Pn(0.2,64),Pn(0.02,64),Pn(1,64)],inf),
	\sustain, 2,

);
~tum02[6]=\filter->{arg in; GVerb.ar(in,roomsize:40,revtime:2,earlyreflevel:0.8,mul:SinOsc.kr(1/32).range(0.01,0.4))+in};
~tum02[12]=\filter->{arg in; HPF.ar(in, SinOsc.ar(1/64).range(2420,600))};
~tum02[13]=\filter->{arg in; LPF.ar(in, \lpf6.kr(200))};
~tum02[14]=\set->Pbind(\args, [\lpf6], \dur, 16, \lpf6, Pseq([400,200,440,280],inf));

);
~tum02[6]=nil;



~snake.clear;

~snake.stop(freeTime:10);
(
~snake.play(vol:0.8, fadeTime:4, numChannels:2);
~snake.quant_(16);)
(~snake[0]=Pbind(
	\instrument,\snakes,
	\amp, 6,
	\legato, 1,
	\freq, 500,
	\dur, 8,
	\f2, 12,
);
~snake[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,SinOsc.kr(1/64).range(200,420)),0.7)};
~snake[4]=\filter->{arg in; Limiter.ar(GVerb.ar(in,80,[1,2],mul:SinOsc.kr(1/64).range(0.1,0.4))+in,0.7)};
~snake[8]=\filter->{arg in; HPF.ar(in,40)};
)


~snake[8]=nil;






//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////mel

~mel01.clear;
~mel01.stop(freeTime:60);

(
~mel01.play(vol:0.3, fadeTime:4, numChannels:2,);
~mel01.quant_(16);
);
(~mel01[0]=Pbind(
	\instrument, \mel,
	\amp, 3,
	\legato, 0.7,
	\octave, Prand([4,6,5],inf),
	\degree, [4,2,8,6],
	\strum, 0.5,
	\noise, Prand([Pn(0.8,13),Pn(0.1,13*3),Pn(0.2,13),Pn(1,26)],inf),
	\dur, Pseq([0.25!8,Rest(2)].flatten,inf),
);
~mel01[3]=\filter->{arg in; Limiter.ar(LPF.ar(in,\lpf3.kr(200,2)),0.7)};
~mel01[4]=\set->Pbind(\args, [\lpf3], \dur,16,\lpf3, Pseq([820,600,400,1200,400,600,820],inf));
~mel01[5]=\filter->{arg in; GVerb.ar(in,[50,40],[8,6],mul:0.2,maxroomsize:200)+(in*2)};
)
~mel01[5]=nil;





//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////impcont
~imps.clear;
~imps.stop(freeTime:60);

(

~imps.play(vol:1, fadeTime:4, numChannels:2);
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
~imps[8]=\filter->{arg in; HPF.ar(in, 200)};
)
~imps[5]=nil;




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

p.gui;


~combdrone.clear;
~combdrone.stop(freeTime:60);
(
~combdrone.play(vol:0.07, fadeTime:4, numChannels:2);
~combdrone.quant_(16);)
(~combdrone[0]=Pbind(
	\instrument, \string,
	\amp, 1,
	\legato, Prand([0.8,0.7,0.6],inf),
	\strum, 0.25,
	\pan, 0,
	\octave, Pseq([3,[3,4],4,6],inf),
	\degree,Pseq([2,4,[6,2],4],inf),
	\dur,Pseq([8,Rest(8)],inf),
);)
(
~combdrone[2]=\filter->{arg in; CombC.ar(in, 16,[4,8],32)};
~combdrone[3]=\filter->{arg in; HPF.ar(in,40)};
~combdrone[6]=\filter->{arg in; LPF.ar(in,400)};
//~combdrone[7]=\set->Pbind(\args, [\lpfc], \dur, 8, \lpfc, Pseq([280,420,400,440,480],inf));
~combdrone[9]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[2,40,6],[16,8,12],mul:0.1)+in.softclip,0.4)};
)
~combdrone[9]=nil;


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////aural

~aural1.clear;
~aural1.stop(fadeTime:60);
(
~aural1.play(vol:0.041, fadeTime:4, numChannels:2);
~aural1.quant_(4);)
(~aural1[0]=Pbind(
	\instrument, \aural,
	\amp,Prand([0.8,0.4,0.6],inf)/8,
	\legato, 0.2,
	\octave, Prand([[5,6],[4,3],[5,4],[3,6]],inf)+Prand([(-2),(-1),2,1],inf),
	\degree, Prand([[1,5],[4,6],[6,2],[2,4],[2,6],[8,2]],inf),
	\dur, Pseq([Pn(8,20),Pn(2,20),Pn(4,20)],inf),
);)
(
~aural1[5]=\filter->{arg in; GVerb.ar(in,[40,60],[18,14],mul:0.4)+in};
~aural1[6]=\filter->{arg in; LPF.ar(in,\lpf3.kr(2000,4))};
~aural1[7]=\set->Pbind(\args, [\lpf], \dur,1, \lpf, Pseq([1000,600,800,200],inf));)
// ~aural1[7]=nil;


















////////////////////////////////////////////////////////////////////////////////////////////////////////THIS IS FINE

~tum01.clear;
~tum01.stop(fadeTime:40);

(
~tum01.quant_(16);
~tum01.play(vol:0.4, fadeTime:4, numChannels:2);)
(~tum01[0]=Pbind(
	\instrument, \tum,
	\amp,Pseq([
		Pseq([ 0,0,0.4,1,  1,1,0,1,  0,1,1, 0,  0,0,0,1],2),
		Pseq([ 0,0,0.4,1,  1,1,0,1,  0,1,1, 0.5,  0,0.5,0,1],2)
	],inf),
	\octave,Prand([Pn(2,64),Pn(3,64)],inf),
	\degree, 1,
	\dur, 0.25,
	\rq, 0.02,//Pseq([Pn(0.2,64),Pn(0.02,64),Pn(1,64)],inf),
	\sustain, 6,

);
~tum01[6]=\filter->{arg in; GVerb.ar(in,roomsize:40,revtime:2,earlyreflevel:0.8,mul:0.2)+in};
~tum01[12]=\filter->{arg in; HPF.ar(in, SinOsc.ar(1/64).range(24,40))};
~tum01[13]=\filter->{arg in; LPF.ar(in, \lpf6.kr(60))};
~tum01[14]=\set->Pbind(\args, [\lpf6], \dur, 16, \lpf6, Pseq([400,240,440,280],inf));

);
~tum01[13]=nil;


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


p.gui;




////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// LEAKY NOISE

~leaky.clear;
~leaky.play(vol:0.1, numChannels:2);
~leaky.quant_(16);
(
~leaky[0]=Pbind(
	\instrument, \leaky,
	\dur,32,
	\octave,Prand([8,6,7,9],inf),
	\degree, [4,6,2],
	\strum, 0.2,
	\amp, 0.7,
	\legato, 0.02,
);
)
~leaky[4]=\filter->{arg in; GVerb.ar(in,40,12,mul:0.4,add:in)*SinOsc.kr(1/32).range(0.1,0.7)};





//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// ONION 16/32 rand sustain 2

~onion.clear;
~onion.stop(fadeTime:30);
(
~onion.play(vol:0.051, fadeTime:4, numChannels:2);
~onion.quant_(4);)
(
~onion[0]=Pbind(
	\instrument, \onion,
	\dur, Pseq([16,Rest(16)],inf),
	\sustain,Prand([0.5,0.6,0.3],inf),
	\freq, 50,
	\amp, 0.2,
	\fsaw, Prand([2,4,10,8,6],inf),
);
~onion[30]=\filter->{arg in; GVerb.ar(in,[80,60],[6,8],maxroomsize:120,add:in)};
~onion[33]==\filter->{arg in; HPF.ar(in,200)};
)


p.gui;
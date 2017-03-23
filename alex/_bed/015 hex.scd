({ var n = PinkNoise.ar(), f = (0.01, 0.015 ..0.07);
v = LFTri.kr(f.scramble[5..7]).range(0.0001, 0.003);
Splay.ar( MembraneHexagon.ar(n, f.scramble[1..3], mul: v), SinOsc.kr(f.choose))
}.play)

(
ProxySynthDef(\ww, {
	arg  sustain,  amp, freq,f,del, rq;
	var sig, env, f2;
	env=EnvGen.kr(Env([0,1,0.5,0.8,0],[0.01,2,1,0.01],\welch),timeScale:sustain,doneAction:2);
	f2=EnvGen.kr(Env([f,f*1.2,f-2,f+2,f+1,f],[0.01,2,1,0.5,0.01],\welch),timeScale:sustain,doneAction:2);

	sig=GrayNoise.ar(LFNoise1.kr(20).range(0.7,0.9)!2,1);
	sig=Clip.ar(sig,-0.5,0.5);
	sig=BPF.ar(sig,freq, rq,add:sig*LFTri.kr(1/16),mul:4);
	sig=sig*LFSaw.ar(f2,LFSaw.kr(1/16));
	sig=CombN.ar(sig,10,del,5);
	sig=Splay.ar(sig,SinOsc.kr(0.125));
	sig=sig*env*amp;

}).add;
)

p.gui;
~ww.clear;
~ww.stop(freeTime:60);

(
~ww.play(vol:0.1, fadeTime:4, numChannels:2,);
~ww.quant_(1);
);
(~ww[0]=Pbind(
	\instrument, \ww,
	\amp, 0.5,
	\f, Prand([14,20,8,4],inf),
	\rq, 0.01,
	\del, Pseq([0.02,0.02,0.1],inf),
	\octave,Pseq([3,[4,2],[6,7]],inf),
	\degree, Pseq([7,5,4],inf),
	\sustain, Prand([0.5,0.6],inf),
	\dur,Prand([32,64],inf),
);


~ww[5]=\filter->{arg in; GVerb.ar(in,[50,60],[8,6],mul:0.2,maxroomsize:200)+(in*2)};
~ww[8]=\filter->{arg in; CompanderD.ar(in,1,0.8,0.2)};
~ww[9]=\filter->{arg in; HPF.ar(in,60)};
~ww[10]=\filter->{arg in; RLPF.ar(in,SinOsc.kr(1/64).range(1800,2800),SinOsc.kr(1/64).range(0.01,1))};
~ww[38]=\filter->{arg in; Limiter.ar(in,2)};

)
~ww[3]=nil;


~ping.clear;
~ping.stop(freeTime:60);

(
~ping.play(vol:0.04, fadeTime:4, numChannels:2,);
~ping.quant_(16);
);
(~ping[0]=Pbind(
	\instrument, \ping,
	\amp, 0.5,
	\pan, Prand([1,(-1),0,0.4,(-0.2), (-0.08), 0.36],inf),
	\octave,Pseq([6,4,5],inf),
	\degree, [3,5],
	\strum, Pseq([2,0.1,0.8],inf),
	\legato,0.2,
	\dur,16,
);
)
(
~ping[5]=\filter->{arg in; GVerb.ar(in,[50,60],[8,6],mul:0.2,maxroomsize:200)+(in*2)};
~ping[8]=\filter->{arg in; CompanderD.ar(in,1,0.8,0.2)};
~ping[9]=\filter->{arg in; HPF.ar(in,60)};
~ping[10]=\filter->{arg in; CombN.ar(in,14,8,40)};
~ping[11]=\filter->{arg in; RLPF.ar(in,SinOsc.kr(1/64).range(1800,2800),SinOsc.kr(1/64).range(0.01,1))};
~ping[38]=\filter->{arg in; Limiter.ar(in,2)};

)
~ping[3]=nil;


p = NetProxy.push
p = NetProxy.connect(\joach)
p.bpm(127)
p.bpm
p.time
p.restartClock
p.metro(16, 8000)
p.players
p.disconnect
(
s.plotTreeView(interval: 0.05, parent: Window.new(name:\plotTree, bounds:Rect(Window.availableBounds.width-330,Window.availableBounds.height-910,300,500)).front.alpha_(0.5).alwaysOnTop_(true));
s.scope(2,4,rate:\audio).view.bounds_(Rect(Window.availableBounds.width-330,Window.availableBounds.height-370,300,300)).alpha_(0.5).alwaysOnTop_(true);
)
TempoClock.default = currentEnvironment.clock;
TempoClock.default.tempo
currentEnvironment.clock.tempo
thisThread.clock_(currentEnvironment.clock)

~swarm.play(vol:0.2, fadeTime:8);
// ~swarm.clock = currentEnvironment.clock;
~swarm.quant_(16);
~swarm.fadeTime = 8;
~swarm.monitor.fadeTime = 4;
(
~swarm[0] = {
	var selEnv = Select.kr(\tone_selEnv.kr(0),
		[
			EnvGen.ar(Env([0.2,0.65,0.65,0.2],[1.0,0.5,0.25,0.25], \sin), \selTrig.tr),
			EnvGen.ar(Env([0.2,0.95,0.65,0.2],[0.25,0.75,3], \sin), \selTrig.tr)
		]
	);

	var osc = LinSelectX.ar(selEnv,
		[
			Saw.ar(42,SinOsc.kr(1/16, 0, 0.13, 0.58), 0.25),
			VarSaw.ar(SinOsc.kr([1600,2600.1], 0.01, 50, 250),0,0.5, 0.1, SinOsc.kr([80,40], 0, 0.15, 0.59), 0.4),
		]
	);
	Pan2.ar(osc * BrownNoise.ar(0.05,0.95), \panTone.kr(0));
};
)
~swarm[30] = \filter -> {|in| BPF.ar(in, SinOsc.kr([12,16],0.01, 80, 850), Saw.kr(1/4, 8, 15)) };
~swarm[31] = \filter -> {|in| FreeVerb.ar(in,0.12,1.47,1.95) };
~swarm[32] = \filter -> {|in| GVerb.ar(in,250, 0.7) };

(
~swarm[40] = \set -> Pbind(\type, \set, \args, [\tone_selEnv, \selTrig, \toneTrig],
	\dur, 2,
	\legato, 2.1,
	\toneTrig, 1,
	\selTrig, Pseq([1,1,0], inf),
	\tone_selEnv, Pseq([0,1,0,1], inf)
))

~swarm.stop(18);
~swarm.free;


/////////////////////////////

~metalPlate.play(vol: 0.8, fadeTime: 12)
~metalPlate.quant_(8);
// ~metalPlate.clock = currentEnvironment.clock;
~metalPlate.fadeTime = 8;
(
~metalPlate[0] = {
	var aEnv = EnvGen.ar(Env([0,1,0], [0.95,0.05], [\sin, \welch]), \aTrig.tr);
	var fEnv = EnvGen.ar(Env([\freq.kr(60,1),4000,\freq.kr(60,1)], [0.003,0.01], \exp), \aTrig.tr);
	Pan2.ar( SinOsc.ar( fEnv, 0, Saw.kr(1/[2,4,8,16]) ), \pan.kr(0,0.125) );
})

~metalPlate[3] = \filter -> {|in| LPF.ar(in, \lpf_freq.kr(400)) };

~metalPlate[4] = \filter -> {|in| GVerb.ar(in, \gverb_size.kr(20, 4), \gverb_time.kr(0.75, 1)) };
~metalPlate[5] = \filter -> {|in| Limiter.ar(in, 1, 0.01); };

~metalPlate[31] = \set -> Pbind(\args, [\aTrig], \aTrig, 1, \dur,  0.5 ) //Pseq([Pn(0.5, 4), Rest(2), Pn(1,2), Rest(2)], inf));
~metalPlate[32] = \set -> Pbind(\args, [\freq], \octave, 2, \degree, Pseq([3,4,3,7], inf), \dur, 8);
~metalPlate.set(\lpf_freq, 480)

~metalPlate.stop(18);

////////////////////////////

~panBrownTone.play(vol:0.1, fadeTime:22)
// ~panBrownTone.clock = currentEnvironment.clock;
~panBrownTone.quant_(16);
(
~panBrownTone[0] = {
	var panEnv = EnvGen.ar( Env([0,-0.8,0.8,-1,1,0], [0.125,0.125,0.125,0.125,1.5], \sin), \panTrig.tr);
	Pan2.ar(SinOsc.ar(\freq.kr(60,2)) * BrownNoise.ar(0.4, 0.6), panEnv)
};
~panBrownTone[2] = \set -> Pbind(\args, [\panTrig], \dur, 0.5, \panTrig, Pseq([1,0,Pwhite(0,1),1], inf));
~panBrownTone[3] = \set -> Pbind(\args, [\freq], \dur, 8, \octave, 4, \degree, Pseq([5,7, 4, 6], inf));
)
~panBrownTone.stop(10);

////////////////////////////

~phone.play(vol:0.1, fadeTime:40)
// ~phone.clock = currentEnvironment.clock;
~phone.quant_(16);
(
~phone[0] = {
	var aEnv = EnvGen.ar(Env.perc(0.0125, 0.05), \ampTrig.tr, doneAction:0);
	Saw.ar(\freq.kr([90,98, 120, 200])) * SinOsc.ar([60,65], 0, 0.4, 0.6) * aEnv;
};
)
~phone[1] = \set -> Pbind(\args, [\ampTrig], \dur, Pseq([Pn(0.0625,32), Rest(22)], inf), \ampTrig, 1);
~phone[30] = \filter -> {|in| GVerb.ar(in, 12, 20) };

~phone.stop(40);

///////////////////////

~tone.play(fadeTime:4, vol:0.6)
~tone.quant_(8);
(
~tone[0] = { LinSelectX.ar(
	EnvGen.ar(Env.linen(0.125, 2.75,3,curve:\sin), \selTrig.tr), [
		SinOsc.ar(13000,0, SinOsc.kr(1/8, 0, 0.03, 0.18), 0.25),
		SinOsc.ar(SinOsc.kr(60, 0.01, 50, 250),0, 0.1, SinOsc.kr([2,4.1], 0, 0.15, 0.19), 0.2),
]);
};
)
~tone[1] = \set -> Pbind(\args, [\selTrig], \dur, 1, \selTrig, 1);
~tone[30] = \filter -> {|in| BPF.ar(in, 800, Saw.kr(1/2, 6, 15)) };
~tone.stop(8);

~drum.play(fadeTime:2, vol:0.1)
~drum.quant_(8);
// ~drum.mold(4);
(
~drum[0] = {
	var f = \freq.kr([40,82,84,88]);
	var aEnv = EnvGen.kr(Env([0, 1, 0],[[0.01,0.05], [0.009, 0.49, 1, 0.5]], \welch), \aTrig.tr);
	var fEnv = EnvGen.kr(Env([f, 6800, f*1.2, f], [0.003, 0.008, 0.15], \exp), \fTrig.tr);
	Pan2.ar( Mix.fill(4, {|i| SinOsc.ar(fEnv,1/4) * BrownNoise.ar(0.2, SinOsc.kr([16, 16.01], 0, 0.8))}) * aEnv, \pan.kr(0));
};
~drum[1] = \set -> Pbind(\args, [\aTrig], \aTrig, 1,
	\dur, 4,
	/*
	Pswitch([
		Pn(8,2),
		Pn(4,2),
		Pseq([1,1,2]),
		Pseq([0.5,0.5,1,2]),
	],
	2)
	*/
	// Pseq([0,1,0,1,0,2,0,1,0,3], inf))
);
~drum[2] = \set -> Pbind(\args, [\freq], \dur, 1, \octave, 3, \degree, Pseq(#[[1,1b,2,2b], [2,2b,3,3b]], inf));
~drum[3] = \set -> Pbind(\args, [\fTrig], \fTrig, 1,
	\dur, 1
	/*
	Pswitch([
		Pseq([Pn(0,5, 2), 0.75]),
		Pseq([Pn(0.25, 4), 0.75]),
		Rest(8),
	], Pn(Pseq([Pn(0,3),1],8) ++ 2, inf))
	*/
);
)
~drum[30] = \filter -> {|in| AllpassC.ar(in,0.02,0.005,0.05) };
~drum[32] = \filter -> {|in| LPF.ar(in, \lpfFreq.kr(2800, 16)) };
~drum[33] = \set -> Pbind(\args, [\lpfFreq], \dur, 24, \lpfFreq, Pseq([800,2800], inf));
~drum.stop(6);
~drum.scope

///////////////////

~circSaw.play(fadeTime:2, vol:0.2)
~circSaw.quant_(16);
(
~circSaw[0] = {
	var aEnv = EnvGen.ar(Env.perc(0.49, 0.01, curve:[8,-4]), \aTrig.tr);
	Pan2.ar(WhiteNoise.ar(0.35, 0.65) * LFPulse.ar(\freq.kr(60, 2),0,0.2,0.7,0.3) * aEnv, \pan.kr(0,0.125));
};
)
~circSaw[1] = \set -> Pbind(\args, [\aTrig], \aTrig, 1, \dur, Pseq([1,1,Rest(14)], inf));
~circSaw[2] = \set -> Pbind(\args, [\freq], \freq, Pseq([300, 300, 370, 300, 340], inf), \dur, 16);
~circSaw[32] = \filter -> {|in| GVerb.ar(in, 30, 8, 0.1)};
~circSaw.end(8);

///////////////////

~spacePiknik.play(vol: 0.2, fadeTime: 8)
~spacePiknik.fadeTime = 8;
~spacePiknik.quant_(2);
(
~spacePiknik[0] = {

	var aEnv = EnvGen.ar(Env([0,1,0], [0.003,0.05], [\sin, \welch]), \aTrig.tr);
	var fEnv = EnvGen.ar(Env([\freq.kr(60),18000,\freq.kr(60)], [0.013,0.009], \exp), \aTrig.tr);

	var osc = LFSaw.ar( fEnv, 0, 1) * Saw.kr([6,8,12,16],0.2,0.8) * BrownNoise.ar(0.05,0.95);
	Pan2.ar( osc * aEnv, \pan.kr(0,0.125) );
}
)
(
~spacePiknik[1] = \set -> Pbind(\args, [\aTrig], \aTrig, 1,
	\dur, Pswitch([
		Pseq([Pn(1/2,31), 1/4, 1/4]),
		Pseq([Pn(1/4,7), Rest(1/4)],4)
	], Pseq([0,0,0,1], inf)
	);
)
)

~spacePiknik[2] = \filter -> {|in| LPF.ar(in, \lpf_freq.kr(1000,18)) };
~spacePiknik[3] = \set -> Pbind(\args, [\pan], \pan, Pseq([-0.7,0.7], inf), \dur, 0.25);
~spacePiknik[4] = \filter -> {|in| FreeVerb.ar(in, 0.13) };
~spacePiknik[4] = nil;

~spacePiknik[5] = \filter -> {|in| CombC.ar(in, 1/3, 1/6, 5/6) };
~spacePiknik[5] = nil;

~spacePiknik[6] = \filter -> {|in| GVerb.ar(in, \gverb_size.kr(4), \gverb_time.kr(0.15)) };
~spacePiknik[6] = nil;

~spacePiknik[7] = \filter -> {|in| CompanderD.ar( in,  0.55, 0.95) };

~spacePiknik.set(\lpf_freq, 800);
~spacePiknik.set(\freq, 600);
~spacePiknik.set(\pan, 0);
~spacePiknik.stop(46);

//////////////////

~dirtyString.play(fadeTime:24, vol:0.0)
~dirtyString.fadeTime = 8;
~dirtyString.quant_(8);

(
~dirtyString[0] = {
	var ampEnv = EnvGen.ar(Env.perc(0.95,0.05),\aTrig.tr);
	var ampEnv2 = EnvGen.ar(Env.perc(0.05,0.25),\aTrig2.tr);
	var ampEnv3 = EnvGen.ar(Env([0,0.7,0.2,1,0], [0.35,0.05,0.5,0.1], \exp),\aTrig3.tr);
	var freqEnv = EnvGen.ar(Env([\freq.kr(160, 2), 2200, \freq.kr(160, 2)], [0.003, 0.002],[\exp, \welch]),\fTrig.tr) ;
	Pan2.ar( SinOsc.ar(freqEnv, 0, ampEnv) * LFPulse.ar(\freq.kr(160, 2), 0, 0.9, 0.2, 0.8), 0);
};
~dirtyString[1] = \set -> Pbind(\args, [\aTrig, \fTrig],
	\dur,Pswitch([
		Pseq([Pn(0.125, 4), 0.5, 7], inf),
		Pseq([Pn(0.125, 8), 0.5, 6.5], inf),
		Pseq([Pn(0.125, 16), 0.5, 3], inf)
	], Pseq([0,0,0,1,0,2], inf)),
	\fTrig, 1,
	\aTrig, Pseq([1,0,0], inf), \aTrig2, Pseq([0,1,0], inf), \aTrig3, Pseq([0,0,1], inf),
);
~dirtyString[2] = \set -> Pbind(\args, [\freq], \dur, 2,
	\octave, Pstutter(Pseq([6,2], inf), Pseq([3,4], inf)),
	\degree, Pseq([3,3,3,7], inf)
);
)
// ~dirtyString[3] = nil;
// ~dirtyString[3] = \set -> Pbind(\args, [\octave], \dur, Pseq([16,4], inf), );
~dirtyString[30] = \filter -> {|in| GVerb.ar(in, 180, 8) };
~dirtyString[31] = \filter -> {|in| LPF.ar(in, Saw.kr(4).range(400, 8000)) };
~dirtyString.stop(10);

////////////////////////////

~funaral.play(fadeTime:4, vol:0.6);
~funaral.quant = 2;
~funaral.fadeTime = 0;

(
~funaral[0] = {|gate=1, freq, sustain|
	var aEnv, fEnv, osc, tone;
	aEnv = EnvGen.kr(Env.adsr(0.3*sustain,0.25*sustain,0.1,0.45*sustain),gate, doneAction:2);
	fEnv = Mix.fill(80, {|i| SinOsc.ar(480,pi/80*i,Rand(0,5));});
	osc = SinOsc.ar([freq+fEnv,freq+fEnv+10],0,0.7,Saw.ar([90,95],0.4,0.6));
	tone = osc * aEnv;
};
)
(
~funaral[10] = \set -> Pbind(
	\dur, Pseq([3.5,0.5,1.5,0.5], inf),
	\legato, Pseq([1.1,1.3,0.5,1.7,0.4,0.9], inf),
	\octave, Pseq([Pn(2,4),Pn(3,2)], inf),
	\degree, Pswitch([
		Pseq([4,4,5,4]),
		Pseq([4,7,5,4]),
		Pseq([4,2,5,4])
	], Pseq([0,0,1,0,2], inf))
);
)
~funaral[30] = \filter -> {|in| LPF.ar(in, 1800);};
~funaral[31] = \filter -> {|in| AllpassC.ar(in/2,0.5,0.25,8.5,1,in/2); };
// )
~funaral.stop(100);
~funeral.scope

//////////////////////

~slowMelody.play(fadeTime:8, vol:0.4);
~slowMelody.quant = 16;
~slowMelody.fadeTime = 8;

~slowMelody[0] = { |freq = 220| SinOsc.ar((freq*0.5)!2,0,SinOsc.kr(1/2,0,0.4), 0.6) * BrownNoise.ar(0.35,0.65) * 0.5; };
~slowMelody[1] = { |freq = 220| LFSaw.ar(freq!2,0,Saw.kr(1/4,0.2), 0.8) * 0.5; };

(
~slowMelody[10] = \set -> Pbind(
	\dur, Pseq([Pn(16,4),8,16,8], inf),
	\octave, Pstutter([8,4,2],Pseq([3,5,4], inf)),
	\degree, Pswitch([
		Pseq([3,3b,3,7], inf),
		Pseq([3,7,6,3b], inf),
	], Pseq([0,1], inf))
);
)
~slowMelody[30] = \filter -> {|in| LPF.ar(in, SinOsc.kr(1/16,0, \lpf.kr(1000), \lpf.kr(1000) + 400)); };
~slowMelody[31] = \filter -> {|in| GVerb.ar(in, 20, 0.5, 0.7); };

~slowMelody.set(\lpf, 400)
~slowMelody.set(\freq, 160)
~slowMelody.stop(14);
~slowMelody.scope

//////////////////////

~darkCloud.play(fadeTime:18, vol:0.9)
~darkCloud.fadeTime = 20;
// ~darkCloud.clock = currentEnvironment.clock;
~darkCloud.quant_(16);

(
~darkCloud[0] = { |vol = 0.5, lpf = 110, poleFreq = 0.9|
	var env, tone;
	env = EnvGen.ar(Env([0,1,1,0],[0.5,1,0.5],\step), \gateTrig.tr(0), doneAction:0);
	tone = LFSaw.ar([1,2,4,8],mul:vol) * LFSaw.ar([255,255.1,355.2,255.3]) * env;
	tone = OnePole.ar(tone,SinOsc.ar(0.1).range(-0.9,0.9));
	tone = LPF.ar(tone,lpf);
	tone = AllpassC.ar(tone,0.133,0.133,1);
	tone = FreeVerb.ar(tone,0.133,1);
	Splay.ar(tone);
}
)
(
~darkCloud[1] = \set -> Pbind(\args, [\gateTrig,\lpf], \gateTrig, 1,
	\legato, 2.1,
	\dur, 4,
	\lpf, Pn(Pseries(100,150,10),inf)
))
~darkCloud.set(\lpf, 410, 8);
~darkCloud.stop(20)
~darkCloud.scope

~drumTail.play(vol:0.4, fadeTime:16);
~drumTail.clock = currentEnvironment.clock;
~drumTail.quant_(16);
(
~drumTail[0] = {
	var aEnv = Select.kr(\tail_id_aEnv.kr(0),
		[
			EnvGen.kr(Env([0.001,0.8,1,0.001], [1,2.7,0.3], [\welch, \lin, \step]), \tailTrig.tr(0)),
			EnvGen.kr(Env([0.001,0.75,0.3,1,0.001], [2.7,0.3,0.8,0.2], [\exp, \step, \exp, \sin]), \tailTrig.tr(0)),
			EnvGen.kr(Env(
				[0,0.7,0,1,0,1,0,1,0,1,0],
				[1.75,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.05,0.45],
				[-8, 4]
			), \tailTrig.tr(0)),
			EnvGen.kr(Env([0,1,0], [2,6], [\welch, \sin]), \tailTrig.tr(0))
		]
	);
	var selEnv = EnvGen.ar(Env([0.001,1,0.001], [3.99,0.01], [\exp,\step]), \tailTrig.tr(0));
	var osc = SelectX.ar(selEnv,
		[
			SinOsc.ar(\tailFreq.kr(400),0,0.15),
			WhiteNoise.ar(0.8,0.2) * LFSaw.ar(\tailFreq.kr(400)*12,0.1,0.9),
			LFSaw.ar(\tailFreq.kr(400),0,0.15)
		]
	);
	Pan2.ar( osc * aEnv, 0);
};
)

~drumTail[10] = \filter -> {|in| GVerb.ar(in,10,1.5,earlyreflevel:0.2,taillevel:0.9) };
~drumTail[11] = \filter -> {|in| LPF.ar(in,\lpf.kr(2000)) };

(
~drumTail[30] = \set -> Pbind(\args, [\tailTrig, \tail_id_aEnv], \tailTrig, 1,
	\dur, 32,
	\tail_id_aEnv, Pseq([0,1,0,2,1,3], inf)
))

~drumTail.free;

~drumTail.set(\tailTrig, 1, \tail_id_aEnv, 3);
~drumTail.set(\tailFreq, 400)


p.gui




AutoIDE.run
AutoIDE.end

p = ProxySpace.push(s).makeTempoClock;
p.clock.tempo_(127/60);

/////////////////////////////////////////////////////////////

(
~drumKof[0] = {
	var num = 4;
	var aEnv = \amp.kr(0);
	var sig = Mix.fill(num,{|n| SinOsc.ar(29.midicps+(aEnv**0.5*2),aEnv**(200+(n/200))*8pi) + BrownNoise.ar(aEnv**46)});
	sig = sig * aEnv/3.9;
	Splay.ar(sig, 0, center:\stereo.kr(0));
};
)

~drumKof.play(vol: 0.2, fadeTime: 10);
~drumKof.stop(4);
~drumKof.scope;
e.hraj(~drumKof, \amp, 4, Env([0.001,1,0.0001],[0.0002,3.75],[3,-3]));
e.hraj(~drumKof, \stereo, 8, Env([-1,1,-1], [6,2] , \sin))
e.zastav(~drumKof, \amp)
e.zastav(~drumKof, \stereo)

/////////////////////////////////////////////////////////////


(
~drumExp[0] = {
	var num = 4;
	var freq = 43;
	var aEnv = \amp.kr(0);
	var sig = Mix.fill(num,{|i|
		SinOsc.ar(
			// freq: freq + (aEnv**0.5*2),
			// freq: freq + (\fplus.kr(0)**0.5*2),
			freq: freq + (\fplus.kr(0)),
			// phase: aEnv**(200+(i/200))*8pi) + BrownNoise.ar(\noise.kr(0)**46)
		phase: aEnv**(200+(i/200))*8pi) + BrownNoise.ar(\noise.kr(0))
	});
	sig = sig * aEnv/3.9;
	Splay.ar(sig, 0, center:\stereo.kr(0));
};
~drumExp[1] = \filter -> {|in| GVerb.ar(in,
	\gverb_size.kr(120),
	\gverb_time.kr(4),
	damping: 0.1,
	inputbw: 0.9,
	spread: 10,
	drylevel: 2.9,
	earlyreflevel:0.1,
	taillevel: 1.9,
	maxroomsize:1500
	// ,mul:\mulgverb.kr(1)*/
) };
~drumExp[2] = \filter -> {|in| LPF.ar(in, \lpf_freq.kr(2800,8)) };
)

~drumExp.play(vol: 0.4, fadeTime: 4);
~drumExp.stop(1);
~drumExp.scope;
e.hraj(~drumExp, \amp, 1, Env([0.001,1,0.0001],[0.0002,0.75],[3,-3]));
e.hraj(~drumExp, \fplus, 1, Env([0,1400,350,80,0], [0.005, 0.01, 0.03, 0.12], [\hold, \hold, -28, -2]))
e.hraj(~drumExp, \noise, 1, Env([0.05,1,0.05], [0.0002,0.75],[3,-3]))
e.hraj(~drumExp, \stereo, 8, Env([-1,1,-1], [6,2] , \sin))
e.zastav(~drumExp, \amp)
e.zastav(~drumExp, \stereo)



1**0.5*2

/////////////////////////////////////////////////////////////

(
~drum[0] = {
	// var f = [60,82,84,98];
	// var f = [74,45,64,48];
	var f = [74,45,64,48,60,82,84,98];
	var num = 18;
	var osc = Mix.fill(num, {|i| SinOsc.ar(f + \fplus.kr(0),0.005*i,1/num)});
	var noise = BrownNoise.ar(\noise.kr(0), SinOsc.kr([8, 9, 32, 36.01], 0, 0.7));
	var sig = osc * noise * \amp.kr(0);
	Splay.ar(sig, 0, center:\stereo.kr(0));
};
~drum[1] = \filter -> {|in| AllpassC.ar(in,0.06,0.01,0.75); };
~drum[1] = nil;
~drum[2] = \filter -> {|in| FreeVerb.ar(in,0.12,0.47,0.95); };
~drum[2] = nil;
~drum[3] = \filter -> {|in|  LPF.ar(in, \lpf.kr(800)); };
// ~drum[3] = nil;
)

~drum.play(vol: 0.5, fadeTime: 1);
~drum.stop(4);
~drum.scope;
e.hraj(~drum, \amp, 1, Env([0,1,0], [0.05, 0.95]))
e.hraj(~drum, \fplus, 1, Env([0,2000,4000,80,0], [0.05, 0.01, 0.06, 0.22], [\hold, \step, -8, -2]))
e.hraj(~drum, \lpf, 1, Env([4000,800,50], [0.17,1.2] , [\hold, \sin]))
e.hraj(~drum, \noise, 1, Env([0.05,0.3,0.05], [0.1,0.4] , \sin))
e.hraj(~drum, \stereo, 4, Env([-1,1,-1], [2,2] , \sin))
e.zastav(~drum, \amp)
e.zastav(~drum, \fplus)
e.zastav(~drum, \lpf)
e.zastav(~drum, \noise)
e.zastav(~drum, \stereo)

/////////////////////////////////////////////////////////////

~drumTail[0] = {
	var aEnv = Select.kr(\tail_id_aEnv.kr(0),
		[
			EnvGen.kr(Env([0.001,0.8,1,0.001], [1,2.7,0.3], [\welch, \lin, \step]), \tailTrig.tr),
			EnvGen.kr(Env([0.001,0.75,0.3,1,0.001], [2.7,0.3,0.8,0.2], [\exp, \step, \exp, \sin]), \tailTrig.tr),
			EnvGen.kr(Env(
				[0,0.7,0,1,0,1,0,1,0,1,0],
				[1.75,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.05,0.45],
				\step
			), \masterTrig.tr),
			EnvGen.kr(Env([0,1,0], [2,6], [\welch, \sin]), \tailTrig.tr)
		]
	);
	var selEnv = EnvGen.ar(Env([0.001,1,0.001], [3.99,0.01], [\exp,\step]), \tailTrig.tr);
	var osc = SelectX.ar(selEnv,
		[
			SinOsc.ar(\tailFreq.kr(400),0,0.15),
			WhiteNoise.ar(0.8,0.2) * LFSaw.ar(\tailFreq.kr(400)*12,0.1,0.9)
		]
	);
	Pan2.ar( osc * aEnv, 0);
};
~drumTail[10] = \filter -> {|in| GVerb.ar(in,10,1.5,earlyreflevel:0.2,taillevel:0.9) };

(
~tone[0] = {
	var volEnv = EnvGen.ar(\tone_volEnv.kr(Env.newClear(8)), \tone_volTrig.tr);
	var panEnv = EnvGen.kr(Env([0.3,-0.3,0.7,-0.7,0.3],[0.5,1.5],\sin), \masterTrig.tr);
	Pan2.ar(SinOsc.ar(\tone_freq.kr(70)) * volEnv, panEnv);
};
)
~tone.play(vol: 0.2, fadeTime: 10);
~tone.stop(4);
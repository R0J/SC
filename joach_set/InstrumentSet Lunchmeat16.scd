AutoIDE.run
AutoIDE.end

p = ProxySpace.push(s).makeTempoClock;
p.clock.tempo_(127/60);
p.clock.tempo_(160/60);
s.scope(6,4,rate:\audio);

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
			freq: freq + (\fplus.kr(0)),
			phase: aEnv**(200+(i/200))*8pi) + BrownNoise.ar(\noise.kr(0))
	});
	sig = sig * aEnv/3.9;
	Splay.ar(sig, 0, center:\stereo.kr(0));
};
~drumExp[1] = \filter -> {|in| GVerb.ar(in,
	\gverb_size.kr(120),
	\gverb_time.kr(1.5),
	damping: 0.1,
	inputbw: 0.9,
	spread: 10,
	drylevel: 2.9,
	earlyreflevel:0.1,
	taillevel: 1.9,
	maxroomsize: 1500
) };
~drumExp[2] = \filter -> {|in| LPF.ar(in, \lpf_freq.kr(600,8)) };
~drumExp[3] = \filter -> {|in| Limiter.ar(in, 1, 0.0001); };

// ~drum.quant_(4);
// ~drum.fadeTime_(16);
~drum[0] = {
	var f = [60,82,84,98];
	// var f = [74,45,64,48];
	// var f = [1740,45,64,48,260,82,84,198];
	var num = 48;
	var osc = Mix.fill(num, {|i| SinOsc.ar(f + \fplus.kr(0),0.005*i,1/num)});
	var noise = BrownNoise.ar(\noise.kr(0), SinOsc.kr([8, 9, 32, 36.01], 0, 0.7));
	var sig = (osc + noise) * \amp.kr(0);
	Splay.ar(sig, 0, center:\stereo.kr(0));
};

~drum[1] = \filter -> {|in| AllpassC.ar(in,4.26,0.01,0.75); };
// ~drum[1] = nil;
~drum[2] = \filter -> {|in| FreeVerb.ar(in,0.12,0.47,0.95); };
// ~drum[2] = nil;
~drum[3] = \filter -> {|in| LPF.ar(in, \lpf.kr(800)); };
// ~drum[3] = nil;
~drum[4] = \filter -> {|in| Limiter.ar(in, 1, 0.0001); };

/////////////////////////////////////////////////////////////

~drumTail[0] = {
	var freq = \freq.kr(400);
	var osc = SelectX.ar(\select.kr(0),
		[
			SinOsc.ar(freq + SinOsc.kr(1/4, 5) ,0,0.15) * (1.4 - \amp.kr(0)),
			// WhiteNoise.ar(0.8,0.2) + LFSaw.ar(\tailfreqMult.kr(4000),0.8,0.2)
			LFSaw.ar(\tailfreqMult.kr(4000), WhiteNoise.ar(0.8,0.2),0.8)
			// WhiteNoise.ar(0.8,0.2) * LFSaw.ar(\tailFreq.kr(400)*12,0.1,0.9)
		]
	);
	Pan2.ar( osc * \amp.kr(0), 0);
};
~drumTail[1] = \filter -> {|in| GVerb.ar(in,
	roomsize:10,
	revtime:1.5,
	earlyreflevel:0.2,
	taillevel:0.9
)};
~drumTail[2] = \filter -> {|in| AllpassC.ar(in,12.26,0.01,0.75); };
~drumTail[3] = \filter -> {|in| LPF.ar(in, \lpf.kr(400)); };
~drumTail[4] = \filter -> {|in| Limiter.ar(in, 1, 0.0001); };
)







AutoIDE.run
AutoIDE.end

p = ProxySpace.push(s).makeTempoClock;
p.clock.tempo_(127/60);

(
~drum[0] = {
	// var f = [60,82,84,98];
	// var f = [74,45,64,48];
	var f = [74,45,64,48,60,82,84,98];
	// var f = [174,145,164,148,160,182,184,198];
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
~drum[3] = \filter -> {|in| GVerb.ar(in, \gverb_size.kr(142), \gverb_time.kr(3), 0.1, mul:\mulgverb.kr(1)) };
// ~drum[3] = nil;
~drum[4] = \filter -> {|in|	LPF.ar(in, \lpf.kr(800)); };
// ~drum[3] = nil;
)

~drum.play(vol: 0.9, fadeTime: 30);
~drum.stop(4);
~drum.scope;
e.hraj(~drum, \amp, 1, Env([0,1,0], [0.05, 0.95]))
e.hraj(~drum, \fplus, 1, Env([0,4000,2380,380,0], [0.05, 0.01, 0.02, 0.22], [\hold, \hold, -8, -8]))
e.hraj(~drum, \fplus, 1, Env([0,4000,2380,80,0], [0.05, 0.01, 0.06, 0.22], [\hold, \hold, -28, -2]))
e.hraj(~drum, \fplus, 1, Env([0,2000,4000,80,0], [0.05, 0.01, 0.06, 0.22], [\hold, \step, -8, -2]))
e.hraj(~drum, \lpf, 8, Env([250,4000,800,250], [0.01, 0.17, 3.7] , [4 ,\hold, \sin]))
e.hraj(~drum, \noise, 1, Env([0.05,0.4,0.05], [0.1,0.4] , \sin))
e.hraj(~drum, \noise, 1, Env([0.05,0.2,0.05], [0.1,0.4] , \sin))
e.hraj(~drum, \stereo, 4, Env([-1,1,-1], [2,2] , \sin))
e.zastav(~drum, \amp)
e.zastav(~drum, \fplus)
e.zastav(~drum, \lpf)
e.zastav(~drum, \noise)
e.zastav(~drum, \stereo)



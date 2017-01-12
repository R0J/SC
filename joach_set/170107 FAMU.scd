p = NetProxy.connect(\joach)
p.metro(4, 8000)
p.restartClock
p.bpm

(
s.plotTreeView(interval: 0.05, parent: Window.new(name:\plotTree, bounds:Rect(Window.availableBounds.width-330,Window.availableBounds.height-910,300,500)).front.alpha_(0.5).alwaysOnTop_(true));
s.scope(2,4,rate:\audio).view.bounds_(Rect(Window.availableBounds.width-330,Window.availableBounds.height-370,300,300)).alpha_(0.5).alwaysOnTop_(true);
)

~drum.play(vol:0.2, fadeTime:44);
~drum.play(vol:0.7, fadeTime:24);
~drum.play(vol:0.5, fadeTime:4);
~drum.stop(10)

(
~drum[0] = {
	var num = 8;
	var noise = 0.4;
	var freq = 63;
	var sig = Mix.fill(num,{|i|
		SinOsc.ar(
			freq: freq!2 + Sdef([\drum, \fplus]).kr,
			phase: Sdef([\drum, \amp]).kr**(200+(i/200))*8pi
		)
	});
	var noiseGen = BrownNoise.ar(noise);
	(sig + noiseGen) * Sdef([\drum, \amp]).kr;
})
~drum[1] = \filter -> {|in| LPF.ar(in, Sdef([\drum, \lpf]).kr) };

Sdef([\drum, \amp], 4, Env([0,1,0], [0.005,3.7], [8, -4]))
Sdef([\drum, \fplus], 4, Env([0,200, 60,0], [0.005, 0.05, 0.09], [8, -16, -8]))
Sdef([\drum, \fplus], 2, Env([0,800, 120,0], [0.005, 0.05, 0.12], [8, -16, -8]))
Sdef([\drum, \fplus], 1, Env([0,1400, 160,0], [0.005, 0.05, 0.19], [8, -16, -8]))
Sdef([\drum, \fplus], 0.5, Env([0,800, 160,0], [0.005, 0.05, 0.19], [8, -16, -8]))
Sdef([\drum, \lpf], 2, Env([400,1400,800, 400], [0.05,0.95, 1], \sin))
// Sdef([\drum, \fplus, \f2], 1, Env([0,1400, 160,0], [0.005, 0.05, 0.19], [8, -16, -8]))
// Sdef([\drum, \fplus], 16)


Sdef([\drum, \fplus]).plot(true)

~panBrownTone.play(vol:0.50, fadeTime:44)
~panBrownTone.play(vol:0.10, fadeTime:64)
~panBrownTone.play(vol:0.00, fadeTime:64)

~panBrownTone.quant_(1);
(
~panBrownTone[0] = {
	var panEnv = EnvGen.ar( Env([0,-0.8,0.8,-1,1,0], [0.125,0.125,0.125,0.125,1.5], \sin), \panTrig.tr);
	Pan2.ar(SinOsc.ar(\freq.kr(60,2)) * BrownNoise.ar(0.3, 0.7), panEnv)
};
~panBrownTone[2] = \set -> Pbind(\args, [\panTrig], \dur, 0.5, \panTrig, Pseq([1,0,Pwhite(0,1),1], inf));
~panBrownTone[3] = \set -> Pbind(\args, [\freq],
	\dur, 8,
	// \octave, 4,
	\octave, [4,6],
	\degree, Pseq([5,7, 4, 6], inf));
)
~panBrownTone.stop(20);

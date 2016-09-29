~drumExp.play(vol: 0.2, fadeTime: 1);
~drumExp.stop(4);
~drumExp.free
~drumExp.scope;
e.hraj(~drumExp, \amp, 8, 0);
e.hraj(~drumExp, \amp, 8, Env([0,1,0],[0.0002,7.75],[3,-3]));
e.hraj(~drumExp, \amp, 4, Env([0,1,0],[0.0002,3.75],[3,-3]));
e.hraj(~drumExp, \amp, 1, Env([0,1,0],[0.0002,0.75],[3,-3]));
(
e.hraj(~drumExp, \amp, [1,1,0.25,4],
	Pswitch([
		[Env([0,0.9,0], [0.0002, 0.70], [3,-3])],
		[Env([0,0.5,0], [0.0002, 0.15], [3,-3])],
		[Env([0,0.9,0.2], [0.0002, 0.20], [3,-3])],
		[Env([0,0.9,0], [0.0002, 1.75], [3,-3])]
	],
	Pseq([Pn(2,32), 1,3])
	// Pseq([Pseq([0,0,0,1], 4), Pn(2,16), 3],inf))
	)
))
e.hraj(~drumExp, \fplus, 0.5, Env([0,1400,350,80,0], [0.005, 0.01, 0.03, 0.12], [\hold, \hold, -28, -2]))
e.hraj(~drumExp, \noise, 4, Env([0.05,1,0.05], [0.0002,0.25],[3,-3]))
e.hraj(~drumExp, \stereo, 8, Env([-1,1,-1], [6,2] , \sin))
e.zastav(~drumExp, \amp)
e.zastav(~drumExp, \fplus)
e.zastav(~drumExp, \stereo)

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

~drum.play(vol: 0.9, fadeTime: 1);
~drum.stop(14);
~drum.free;
~drum.scope;
e.hraj(~drum, \amp, 1, Env([0.2,1,0.2], [0.0002, 0.95]))
e.hraj(~drum, \fplus, 0.5, Env([0,2000,14000,80,0], [0.0002, 0.0002, 0.006, 0.12], [\hold, \step, -8, -2]))
e.hraj(~drum, \lpf, 1, Env([18000,400,50], [0.007,0.7] , [\hold, \sin]))
e.hraj(~drum, \noise, 1, Env([0.05,0.7,0.05], [0.0001,0.14] , \sin))
e.hraj(~drum, \stereo, 4, Env([-1,1,-1], [3,1] , \sin))
e.zastav(~drum, \amp)
e.zastav(~drum, \fplus)
e.zastav(~drum, \lpf)
e.zastav(~drum, \noise)
e.zastav(~drum, \stereo)

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


~drumTail.play(vol: 0.9, fadeTime: 1);
~drumTail.stop(4);
~drumTail.scope;
~drumTail.free
e.hraj(~drumTail, \amp, 4, Env([0.2,0.8,1,0.2], [1,2.7,0.3], [\welch, \lin, \step]))
e.hraj(~drumTail, \select, 16, Env([0,0.1,1,0], [2, 3.99, 0.01], [\lin, \sin,\step]))
e.hraj(~drumTail, \freq, 48, Env([0, 200, 220, 206], 8, \step))
e.hraj(~drumTail, \tailfreqMult, 48, Env([440 ,12, 320, 1200, 500, 420, 24, 4000, 440], 8, \step))
e.hraj(~drumTail, \lpf, 1, Env([100,1800,450], [0.17,0.8] , [\hold, \sin]))

e.zastav(~drumTail, \tailfreq)
e.zastav(~drumTail, \lpf)




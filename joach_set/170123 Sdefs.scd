p = NetProxy.push
(
s.plotTreeView(interval: 0.05, parent: Window.new(name:\plotTree, bounds:Rect(Window.availableBounds.width-330,Window.availableBounds.height-910,300,500)).front.alpha_(0.5).alwaysOnTop_(true));
s.scope(8,0,rate:\control).view.bounds_(Rect(Window.availableBounds.width-330,Window.availableBounds.height-370,300,300)).alpha_(0.5).alwaysOnTop_(true);
)
p.metro(4,8000)

~kick.play
~kick.stop(4)
~kick.fadeTime = 12
~kick[0] = \kick
~kick[1] = \lpf
~kick[2] = \volume
~kick.sdef(\amp, 0).env([0,1,0], [0.005, 1.995], [8, -4], 2)
~kick.sdef(\fplus, 0).env([0,6400, 200, 0], [0.01, 0.009, 0.17], [16, \step, -3], 1)
~kick.sdef(\noise, 0).ramp(0.1,0,4)
~kick.sdef(\lpf, 0).env([1200,6400, 2200, 200], [0.005, 0.1, 0.17], [16, \step, -3], 2)
~kick.sdef(\vol, 0).level([0.4,0.2, 0.4, 0.2], [0.1,0.9,0.4,0.6])
~kick.sdef(\amp).plot

~kick.dependants
~kick.sdef(\amp).parentNode

Sdef.play(10, 20)
Sdef.stop

~note.play
~note.vol_(0.01)
~note.stop
~note[0] = {SinOsc.ar(\freq.kr(160)!2, mul:\amp.kr(0))}
~note.sdef(\amp, 0).env([0,1,0], [3.85, 0.15], [8, -4], 4)
~note.sdef(\amp, 1).env([0.1,0.2,0.1], [0.15, 0.25], [8, -4],0.5)
~note.sdef(\amp, 2).fade(0,1,28)
~note.sdef(\amp, 3).fade(1,0,8)
~note.sdef(\amp, 4).dupTime(1,8, 0.5)
~note.sdef(\amp, 5).chain(2,4,3)
~note.sdef(\freq, 0).freq(3,[3,7,5,4],1)
~note.sdef(\freq, 1).dupTime(0,20, 4)
~note.sdef(\freq, 1).delete
~note.sdef(\amp).plot
~note.sdef(\freq).plot

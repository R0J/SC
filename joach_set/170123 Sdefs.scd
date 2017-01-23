p = NetProxy.push
(
s.plotTreeView(interval: 0.05, parent: Window.new(name:\plotTree, bounds:Rect(Window.availableBounds.width-330,Window.availableBounds.height-910,300,500)).front.alpha_(0.5).alwaysOnTop_(true));
s.scope(8,0,rate:\control).view.bounds_(Rect(Window.availableBounds.width-330,Window.availableBounds.height-370,300,300)).alpha_(0.5).alwaysOnTop_(true);
)

~kick.play
~kick.stop(4)
~kick[0] = \kick
~kick[1] = \lpf
~kick.sdef(\amp,0).env([0,1,1,0.1], [0.005, 0.035, 0.96], [8, \step, -2], 4)
~kick.sdef(\fplus, 0).env([0,6400, 200, 0], [0.01, 0.009, 0.17], [16, \step, -3], 1)
~kick.sdef(\noise, 0).ramp(0.1,0,4)
~kick.sdef(\lpf, 0).env([1200,6400, 2200, 200], [0.005, 0.1, 0.17], [16, \step, -3], 4)
~kick.sdef(\lpf).plot

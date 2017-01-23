p = NetProxy.push
(
s.plotTreeView(interval: 0.05, parent: Window.new(name:\plotTree, bounds:Rect(Window.availableBounds.width-330,Window.availableBounds.height-910,300,500)).front.alpha_(0.5).alwaysOnTop_(true));
s.scope(8,0,rate:\control).view.bounds_(Rect(Window.availableBounds.width-330,Window.availableBounds.height-370,300,300)).alpha_(0.5).alwaysOnTop_(true);
)

~kick.play
~kick.stop(4)
~kick[0] = \kick
~kick[1] = \lpf
~kick.sdef(\amp,0).env([0,1,1,0], [0.005, 0.035, 0.96], [8, \step, -2])
~kick.sdef(\fplus, 0).env([0,6400, 200, 0], [0.01, 0.009, 0.17], [16, \step, -3])
~kick.sdef(\fplus, 1).fixTime(0,1)
~kick.sdef(\noise, 0).ramp(0.1,0.4,4)
~kick.sdef(\fplus).plot
~kick.dependants

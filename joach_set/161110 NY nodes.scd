p = NetProxy.push
p.bpm(127)

s.plotTreeView(interval: 0.05, parent: Window.new(name:\plotTree, bounds:Rect(Window.availableBounds.width-330,Window.availableBounds.height-910,300,500)).front.alpha_(0.5).alwaysOnTop_(true));
Library.at(\qMachine).postTree

NodeStage.current(nil, 28)
NodeStage.current(\stage3, 8, 4)

s.scope
~kick.play
~kick.set(\vol, 1)
~kick[0] = \kick
~kick[1] = \lpf

~kick.env(\amp).set(\a1, Env([0,1,0],[0.005,0.85],[5,-3])).fixDur(\a1, 1)
~kick.env(\fplus).set(\f1, Env([0,2400,0],[0.005,0.14],[15,-8])).fixDur(\f1, 1);
~kick.env(\fplus).set(\f2, Env([0,8400,0],[0.005,0.09],[15,-8])).fixDur(\f2, 1);
~kick.env(\flpf).set(\lpf1, Env([800,1400,400,200],[0.003, 0.01, 0.91],[18, -5, \sin])).fixDur(\lpf1, 1);
~kick.env(\flpf).set(\lpf2, Env([400,8400,4800,200],[0.003, 0.01, 0.11],[18,-5,-3])).fixDur(\lpf2, 1);

~kick.cycle(\test1).set(\amp,Pn(\a1, 4),0).set(\fplus, Pn(\f1,1),0).set(\flpf, Pn(\lpf1,4),0)
~kick.cycle(\test2).set(\amp,Pn(\a1, 4),0).set(\fplus, Pn(\f2,1),0).set(\flpf, Pn(\lpf2,4),0)
~kick.cycle(\test3).set(\amp,Pn(\a1, 4),0).set(\fplus, Pn(\f2,2),0).set(\flpf, Pn(\lpf1,2),0)

~kick.stage(\stage1).set(0, \test1);
~kick.stage(\stage2).set(0, \test2);
~kick.stage(\stage3).set(0, Pn(\test2,3) ++ \test3 ++ \test1);


~kick.post
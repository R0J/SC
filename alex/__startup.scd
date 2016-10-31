Server.local.options.memSize = 65536*4; // increase block size (default is 64)
Server.internal.options.blockSize = 65536*4;

s.scope(2,0,rate:\audio).view.bounds_(Rect(1350,650,300,300)).alpha_(0.7).alwaysOnTop_(true);
//s.scope(8,0,rate:\control).view.bounds_(Rect(1350,650,300,300)).alpha_(0.5).alwaysOnTop_(true);
//s.plotTreeView(interval: 0.05, parent: Window.new(name:\plotTree, bounds:Rect(1350,100,300,500)).front.alpha_(0.7).alwaysOnTop_(true));


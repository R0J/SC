p = NetProxy.connect();

p.bpm;
p.metro(1);
p.restartClock;
p.bpm(127);

p.players;
p.time;
p.makeTempoClock;

p.disconnect;



////// SAFE ///////////////
p = NetProxy.push();
(
~tick.rebuild;
~counter.rebuild;
)

(
    //p.clock.tempo_(120/60);
    //p.arProxyNames.do(p[_.asSymbol].rebuild);
)


// test synth ///////////////////////////////////////

(
    ~test.ar(2);
    ~test.clock = p.clock;
    ~test.quant=4;
    ~test.fadeTime=4;
    ~test={
        var sig = SinOsc.ar(5000!2,mul:0.1) * Pulse.ar(~tempo/8);
        Splay.ar(sig,0.5,0.25);
    };
    ~test.play;
);
~test.stop(4);
~test.clear;
~test.pause;
~test.resume;


p.moveNodeToTail(\master);

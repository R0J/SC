p = NetProxy.connect();

p.bpm;
p.metro(1,1500);
p.bpm(127);

p.players;
p.time;
p.makeTempoClock;

p.disconnect;



/////// MANUAL TICK RESET /////////////////////////////////////////////////////////////////
(
    ~tick.kr(1);
    ~counter.kr(1);

    ~tick.clock = p.clock;
    ~counter.clock = p.clock;

    ~tick.quant=4;
    ~counter.quant=4;

    ~tick = { var imp = Impulse.kr(~tempo.kr * 4); SendTrig.kr(PulseDivider.kr(imp,16), 0, ~tempo.kr ); imp;  };
    ~counter = {PulseCount.kr(~tick.kr);};
    p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].rebuild;};});
    (
        p.clock.sched(p.clock.timeToNextBeat(4),{ 
            p.restartClock;
            p.moveNodeToTail(\master);

        });
    );
)



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

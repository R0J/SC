


/////////////// CONNECT //////////////////////

p.disconnect;
p = NetProxy.connect();


p.bpm;
p.metro(4,1800);
p.bpm(127);


////////////////////////////////////////////////

p.players;
p.time;
p.makeTempoClock;

///////////////////////////////////////////////






/////// MANUAL TICK RESET /////////////////////////////////////////////////////////////////
(
    //s.latency = 0.2;
    p.restartClock();
    p.clock.sched(p.clock.timeToNextBeat(16),{ 
        ~tick.kr(1);
        ~counter.kr(1);

        ~tick.clock = p.clock;
        ~counter.clock = p.clock;

        ~tick.quant=0;
        ~counter.quant=0;

        ~tick = { var imp = Impulse.kr(~tempo.kr * 4); SendTrig.kr(PulseDivider.kr(imp,16), 0, ~tempo.kr ); imp;  };
        ~counter = {PulseCount.kr(~tick.kr);};
        p.moveNodeToTail(\master);
    });
);


(

    p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].rebuild;};});
    (
        p.clock.sched(p.clock.timeToNextBeat(4),{ 
            p.restartClock;

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

// test_one synth ///////////////////////////////////////

(
~test_one.ar(2);
~test_one.clock = p.clock;
~test_one.quant=[16,0];
~test_one.fadeTime=0.02;
~test_one={
  //  |       |       |       |       |  
  var pat = [
    1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0,
  ];
  var imp = PulseDivider.kr(~tick.kr,1,~counter.kr%pat.size);
  var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
  var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.5],[3,-3]),imp * hit);
  var sig = SinOsc.ar(50!2,env**20*8pi);
  sig = sig * env;
  Splay.ar(sig,0.75,0.5);
};
~test_one.play;
);
~test_one.stop(4);
~test_one.clear;
~test_one.pause;
~test_one.resume;

// test synth ///////////////////////////////////////

(
    ~test.ar(2);
    ~test.clock = p.clock;
    ~test.quant=16;
    ~test.fadeTime=0;
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

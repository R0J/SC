p = NetProxy.connect();

p.bpm;
p.metro(4);
p.restartClock;
p.bpm(127);

p.players;
p.time;
p.makeTempoClock;

p.disconnect;

(
  //p.clock.tempo_(120/60);
  //p.arProxyNames.do(p[_.asSymbol].rebuild);
)


(
p.addOnRestartClock({
  ~tick.kr(1);
  ~tick.clock = p.clock;
  ~tick.quant=4;
  ~tick = { Impulse.kr(~tempo * 4); };
  ~counter = {PulseCount.kr(~tick.kr);};
  p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].rebuild;};});
});    
)


////////////////////////////////////////////////////
///////  /////////////////////////////////////////////
////////////////////////////////////////////////////


(
  TempoClock.default = p.clock;
  p.restartClock;
  ~tick.quant=4;
  ~counter.quant = 4;
  ~counter = {PulseCount.kr(~tick.kr);};
  ~tick = { Impulse.kr(~tempo*4); };
  p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].rebuild;};});
);

////////////////////////////////////////////////////
////////////////////////////////////////////////////
////////////////////////////////////////////////////

(
//////////////////////////  FADE -> OUT ///////////////////////////////////////////////////////
{
  p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clear(20);};});
}.fork;  
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

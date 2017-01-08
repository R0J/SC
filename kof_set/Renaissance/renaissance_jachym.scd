
/////////////// CONNECT //////////////////////

p.disconnect;
p = NetProxy.connect(\kof);
TempoClock.default = p.clock;

p.bpm(120);

p.bpm;
p.metro(4,1800);
p.players;


// soft reset
(
    //for now
    s.latency = 0;

    ~tick.kr(1);
    ~tick.clock = p.clock;
    ~tick.quant = 16;
    ~counter.clock = p.clock;
    ~counter.quant = 16;
    ~tick = { var imp = Impulse.kr(~tempo.kr * 4); SendTrig.kr(PulseDivider.kr(imp,16), 0, ~tempo.kr ); imp;  };
    ~counter = {PulseCount.kr(~tick.kr);};
    p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){ p[synth.asSymbol].quant = [16,0]; p[synth.asSymbol].rebuild;};});

    ~master.quant=16;
    ~master.play;
    ~master.fadeTime = 0.01;
    ~master = {|wet=0.55,lp=4500,hp=250,len=4,rev = 2,duckLo = 150|
        var sig = In.ar([0,1]);
        var orig = sig;
        var fx,low,high,mid;
        var duck;

        duck = LPF.ar(LPF.ar(sig,duckLo),duckLo);
        low = CompanderD.ar(LPF.ar(sig,LFNoise2.kr(1/4!2*~tempo).range(100,800)),9/10,1,1/2);
        mid = CompanderD.ar(BPF.ar(sig,LFNoise2.kr(1/8!2*~tempo).range(500,4500),1/2),9/10,1,1/2);
        high = CompanderD.ar(HPF.ar(sig,LFNoise2.kr(1/16!2*~tempo).range(3000,10000)),9/10,1,1/2);
        sig = CompanderD.ar((high+mid+low).flat,9/10,1,1/1.2);
        orig = sig;
        fx = LPF.ar(sig,lp).distort;
        fx = LPF.ar(fx,lp);
        fx = HPF.ar(fx,hp);
        fx = HPF.ar(fx,hp);
        fx = AllpassN.ar(fx,1/1600*~tempo,1/4*~tempo,len);

        fx = DWGReverb.ar(NumChannels.ar(fx,2),[5000,5100],1/len)/rev + fx;

        sig = SelectX.ar(wet,[orig,fx]);
        //  sig = Compander.ar(sig,~duck.ar,8/10,1/2,1);
        sig = NumChannels.ar(sig,2) ;
        sig = Limiter.ar(sig.softclip);
        ReplaceOut.ar(0,sig);
    };

    ~master_wet.quant = 4;
    ~master_wet = {LFDNoise1.kr(~tempo/1600!2).exprange(0.15,0.35);};
    // ~master_wet = {|sig| sig-(1-LFSaw.ar(~tempo*1)).range(0,1)**4};
    ~master.map(\wet,~master_wet);
    p.moveNodeToTail(\master);
);


// primal ritual dance /////////////////////////////////////// jachym

(
    ~hh3.ar(2);
    ~hh3.clock = p.clock;
    ~hh3.quant=[16,0];
    ~hh3.fadeTime=0.2;
    ~hh3={
        //         |       |       |       |       |
        var pat = [
            1,0,1,0, 1,0,1,0, 1,0,1,0, 0,0,0,0,
            1,0,1,0, 1,0,1,0, 1,0,1,0, 0,1,0,1,
            1,0,1,0, 1,0,1,0, 1,0,1,0, 0,0,0,0,
            1,0,1,0, 1,0,1,0, 1,0,1,0, 1,1,1,1,

        ].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,[1,2,4,8,16,32].nthPrime%4);
        var hit = Select.kr(PulseCount.kr(imp)/[1,2,3,4]%pat.size,pat);
        var env = EnvGen.ar(Env([0.01,1,0.0001],[0.005,1/16*[1,2,3]],[80,-5]),imp * hit);
        var sig = LFNoise1.ar(22000*LFNoise0.kr(1/4*~tempo/2!4).range(0.5,1)) * Crackle.ar([0.1,0.7,0.4]+env);
        sig = sig * env;
        Splay.ar(sig,0.75,0.15);
    };
    ~hh3.play;
);
~hh3.stop(4);
~hh3.clear(40);
~hh3.pause;
~hh3.resume;




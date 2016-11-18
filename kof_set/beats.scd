
(
    p=ProxySpace.push(s.reboot);
    p.makeTempoClock;
);

p.makeTempoClock;
(
    TempoClock.default = p.clock;
//    p.clock.tempo_(127/60);
    p.arProxyNames.do(p[_.asSymbol].rebuild);
)



(
    ~tick.kr(1);
    ~tick.clock = p.clock;
    ~counter.clock = p.clock;
    ~tick.quant=2;
    ~counter.quant=2;
    ~tick = { Impulse.kr(~tempo * 4); };
    ~counter = {PulseCount.kr(~tick.kr);};
);    



p.clock.tempo_(127/60);
(


    (
        (

            ~tick.kr(1);
            // p.arProxyNames.do({|synth| ~tick.quantize(p[synth.asSymbol]);});
            ~tick.clock = p.clock;
            ~tick.quant=[2,0];
            ~tick = { Impulse.kr(~tempo * 4); };
            ~counter = {PulseCount.kr(~tick.kr);};
            //p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){~tick.quantize(p[synth.asSymbol]);};});
            p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clock=p.clock;p[synth.asSymbol].fadeTime=0;p[synth.asSymbol].rebuild;};});
        );    

    );

);

(

    (
        ~master.quant=2;
        ~master.play;
        ~master.fadeTime = 0.01;
        ~master = {|wet=0.35,lp=7500,hp=350,len=20,rev = 1|
            var sig = In.ar([0,1]);
            var orig = sig;
            var fx,low,high,mid;
            //fx = Limiter.ar(sig,0.95);

            //sig = CombN.ar(sig,1/50,1/50,1);
            low = CompanderD.ar(LPF.ar(sig,LFNoise2.kr(1/4!2*~tempo).range(100,800)),3/4,1/1.12,1/2,0.1,0.3);
            mid = CompanderD.ar(BPF.ar(sig,LFNoise2.kr(1/8!2*~tempo).range(500,4500),0.1),3/4,1/1.32,1/2,0.02,0.1);
            high = CompanderD.ar(HPF.ar(sig,LFNoise2.kr(1/16!2*~tempo).range(3000,10000)),3/4,1.12,1/2,0.02,0.05);
            sig = CompanderD.ar((high+mid+low).flat,3/4,1/1.14,1/1.2); 
            //sig = Compander.ar(sig,sig,3/4,1/2,1/3);
            orig = sig;
            fx = LPF.ar(sig,lp);
            fx = HPF.ar(fx,hp);
            fx = DWGReverb.ar(NumChannels.ar(fx,2),[5000,5010],1/len)/rev;
            sig = SelectX.ar(wet,[orig,fx]);
            sig = NumChannels.ar(sig,2);
            sig = Limiter.ar(sig.softclip);
            ReplaceOut.ar(0,sig);
        };

        ~master_wet.quant = 4;
        ~master_wet = {LFDNoise1.kr(~tempo/4!2).exprange(0.05,0.35);};
        ~master.map(\wet,~master_wet);
        p.moveNodeToTail(\master);
    );

)
p.moveNodeToTail(\master);

(
    //////////////////////////  FADE -> OUT ///////////////////////////////////////////////////////
    {
        p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clear(20);};});
    }.fork;  
)

(
/// 8 KICK start

(
    ~kick.ar(2);
  //  ~kick.clock = p.clock;
    ~kick.quant = [1,0];
    ~kick.fadeTime=16.2;
    ~kick={
        //         |       |       |       |       |  
        var pat = [
            1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,1,
            1,0,1,0,1,0,1,0,1,0,1,0,1,0,0,1,
            1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,1,
            1,0,1,0,1,0,1,0,1,0,1,0,1,0,0,1,
        ];
        var mod2 = LFNoise2.kr(~tempo/160!4).range(2,80);
        var mod = LFDNoise1.kr(~tempo/160!4).range(2,80);
        var imp = PulseDivider.kr(~tick.kr,1,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,2.5/[1,1.01,1.011,1.0111]],[2,-3.3]), imp * hit);
        var hum = LFDNoise1.kr(~tempo).range(0.0,0.01);
        var sig;
        env = env ** LFNoise1.kr(1/64!4).range(0.07,1.6);

        // env = DelayN.ar(env,hum,hum);

        sig = Mix.fill(20,{|i|SinOsc.ar(55+(env**mod2*55),env**(mod2+i)*8pi)})/20.9;
        sig = sig * env;
        Splay.ar(sig,0.005,0.65);
    };
    ~kick.play;
);
)

(
    ~onea.ar(2);
//    ~onea.clock = p.clock;
    ~onea.quant=[1,1/4];
    ~onea.fadeTime=0.02;
    ~onea={
        //         |       |       |       |       |  
        var pat = [
            1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,
            1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,
            1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,
            1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,
        ];
        var imp = PulseDivider.kr(~tick.kr,1,~counter.kr);
        var imp2 = PulseDivider.kr(~tick.kr,[4,8,16,3],~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var hit2 = Select.kr(PulseCount.kr(imp2)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.5],[10,-3],curve:'exp'),imp * hit);
        var env2 = EnvGen.ar(Env([0.001,1,0.0001],[0.02,1.15],[10,-3],curve:'exp'),imp2 * hit2);
        var sig = SinOsc.ar(50 + (env**50*500));

        //sig = LFSaw.ar([60,67,64].midicps*64+(env2**20*1500),mul:0.7)**LFNoise2.kr(1/16!2 * ~tempo).range(0.4,4)/1.5+sig;
        sig = LPF.ar(sig,LFSaw.kr([1,2,3,4]/16*~tempo).range(190,9000));

        sig = sig * env;

        //sig = GVerb.ar(HPF.ar(sig,900),10,3)/4+sig;
        //sig = GVerb.ar(LPF.ar(sig,900),100,10)/4+sig;
        sig = CompanderD.ar(sig,3/4,1/1.5,1);
        Splay.ar(sig,0.75,0.65);
    };
    //~onea[1] = \filter -> {|sig| DWGReverb.ar(HPF.ar(LPF.ar(sig,5000),50),[5000,5050],1/2)+sig};
    ~onea.play;
);


(
~kick.clear(1);
~onea.clear(1);
)



(
    (
        ~sell.ar(4);
        // ~sell.quantize(~tick);
        ~sell.mold(4);
        ~sell.clock = p.clock;
        ~sell.quant=[8,0];
        ~sell.fadeTime=4;

        ~sell={var t = LFTri.ar([1/1600,1/1600,[1/4,1/2,1/8,1/16]]*~tempo) * 1/4 + LFSaw.ar(1/4000,mul:2); Perlin3.ar(t[0],t[1],t[2]);};
    );

    // scanner synth ///////////////////////////////////////

    (
        //~scanner.ar(4);
        //~scanner.clock = p.clock;
        ~scanner.quant=[2,1/2];
        ~scanner.fadeTime=0.2;
        ~scanner[0]={
            //         |       |       |       |       |
            var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0];
            var imp = PulseDivider.kr(~tick.kr,[1,2,8,4],~counter.kr % pat.size);
            var hit = Select.kr(PulseCount.kr(imp)*[1,2,8,4]%pat.size,pat);
            var env = Decay2.kr(imp*hit,1/4*[1,2,8,4]*~tempo*LFNoise2.kr(~tempo/64!4).exprange(1.0,4.9),0.0005);
            var sig = [
                SinOsc.ar(60.midicps/4,Pulse.ar(~tempo/[1,2,8,4]*2)),
                LFNoise0.ar(1500*[1,2,4,8]) * Pulse.ar(~tempo/[1,2,8,4]),
                Pulse.ar([4,8,2,1]*~tempo*2) * SinOsc.ar(55,Pulse.ar(~tempo/[1,2,8,4])),
                Crackle.ar(1.5*Pulse.ar(~tempo/[1,2,8,4])) * Pulse.ar(~tempo/[1,2,8,4]);
                Pulse.ar(~tempo*[4,2]) * SinOsc.ar(60.midicps*8/[1,1.5],mul:0.12),
                LFNoise1.ar(15800!2) * Pulse.ar(~tempo/[1,2,8,4]),
            ];

            sig = sig * env
        };

        ~scanner[1] = \filter -> {|sig| SelectX.ar(~sell.ar*(sig.size+1) % (sig.size+1),sig);};
        ~scanner[2] = \filter -> {|sig| Resonz.ar(sig,LFNoise2.kr(1/4/[1,2,3,4] * ~tempo).exprange(100.01,1600),0.2,mul:2)+sig/2};
        ~scanner[3] = \filter -> {|sig| HPF.ar(sig,20); };
        ~scanner[4] = \filter -> {|sig| Limiter.ar(sig,0.995) };
        ~scanner[6] = \filter -> {|sig| CompanderD.ar(sig,3/4,1/2,1/20,0.002,1/2) } ;
        ~scanner[7] = \filter -> {|sig| Splay.ar(sig,0.95,0.15,SinOsc.kr(4.01*~tempo)/1.2); };

        ~scanner.play;
    );

);
~scanner.stop(4);
~scanner.clear;
~scanner.pause;

~scanner.rebuild;

(
    //////////////////////////  FADE -> OUT ///////////////////////////////////////////////////////
    {
        p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clear(20);};});
    }.fork;  
)
~scanner.resume;




// crack synth ///////////////////////////////////////

(
    ~crack.ar(2);
    ~crack.clock = p.clock;
    ~crack.quant=[2,1/2];
    ~crack.fadeTime=4;
    ~crack={
        var sig = Crackle.ar(1+LFNoise2.kr(1/4).range(0.01,0.7),LFPulse.ar(5000*[1,2,1.3333,1.5])/LFSaw.ar(1/4*~tempo/2).range(1,50)) * Pulse.ar(8*~tempo/2) * LFPulse.ar(1*~tempo/2) * LFPulse.ar(7/[1,2,3,4]*~tempo/2);
        sig = DelayN.ar(sig,1/2*~tempo,1/2*~tempo);
        Splay.ar(sig,0.5,0.15);
    };
    ~crack.play;
);
~crack.stop(4);
~crack.clear;
~crack.pause;
~crack.resume;




(
    p=ProxySpace.push(s.reboot);
    p.makeTempoClock;
);

p.makeTempoClock;
p.clock.tempo_(127/60);
(
    TempoClock.default = p.clock;
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

//HIGH
(
    ~master.quant=2;
    ~master.clock = p.clock;
    ~master.play;
    ~master.fadeTime = 0.01;
    ~master = {|wet=0.45,lp=7500,hp=200,len=5,rev = 4|
        var sig = In.ar([0,1]);
        var orig = sig;
        var fx,hi,lo;
        //fx = Limiter.ar(sig,0.95);
        sig = Limiter.ar(sig);
        fx = LPF.ar(sig,lp);
        fx = LPF.ar(fx,lp);
        fx = HPF.ar(fx,hp);
        fx = HPF.ar(fx,hp);
        lo = DWGReverb.ar(NumChannels.ar(fx,2),[5000,5010],1/len)/rev;
        fx = HPF.ar(sig,lp);
        hi = DWGReverb.ar(NumChannels.ar(fx,2),[4000,4010],1/len/10)/rev/10;
        sig = SelectX.ar(wet,[orig,NumChannels.ar(lo+hi,2)]);
        sig = Compander.ar(sig,3/4,1/2,1/3);
        sig = NumChannels.ar(sig,2);
        ReplaceOut.ar(0,sig);
    };
    ~master_wet.quant = 4;
    ~master_wet = {LFDNoise1.kr(~tempo/2!2).exprange(0.15,0.25);};
    ~master.map(\wet,~master_wet);
    p.moveNodeToTail(\master);
);

//DEEP
(

    (
        ~master.quant=2;
        ~master.play;
        ~master.fadeTime = 0.01;
        ~master = {|wet=0.35,lp=7500,hp=250,len=2,rev = 8|
            var sig = In.ar([0,1]);
            var orig = sig;
            var fx,low,high,mid;
            //fx = Limiter.ar(sig,0.95);

            //sig = CombN.ar(sig,1/50,1/50,1);
            low = CompanderD.ar(LPF.ar(sig,LFNoise2.kr(1/32!2*~tempo).range(250,800)),3/4,1/1.12,1/1.5,0.1,0.3);
            mid = CompanderD.ar(BPF.ar(sig,LFNoise2.kr(1/32!2*~tempo).range(800,7500),0.1),3/4,1/1.25,1/2,0.02,0.1);
            high = CompanderD.ar(HPF.ar(sig,LFNoise2.kr(1/32!2*~tempo).range(7000,10000)),3/4,1.12,1,0.02,0.05);
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
        ~master_wet = {LFDNoise1.kr(~tempo/640!2).exprange(0.05,0.85);};
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
        ~kick.clock = p.clock;
        ~kick.quant = [2,0];
        ~kick.fadeTime=0.02;
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

    (
        ~onea.ar(2);
        ~onea.clock = p.clock;
        ~onea.quant=[2,1/4];
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
            var imp2 = PulseDivider.kr(~tick.kr,8,~counter.kr);
            var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
            var hit2 = Select.kr(PulseCount.kr(imp2)%pat.size,pat);
            var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.5],[10,-3],curve:'exp'),imp * hit);
            var env2 = EnvGen.ar(Env([0.001,1,0.0001],[0.5,8.005]/~tempo,[2,-30],curve:'exp'),imp2 * hit2);
            var sig = SinOsc.ar(55 + (env**50*550));

            var sig2 = VarSaw.ar([60,67,64].midicps/2,0,(env2**2),mul:0.7)**LFNoise2.kr(1/16!2 * ~tempo).range(0.4,4);

            sig = sig * env;
            //sig = sig + (sig2 * env2);
            sig =  HPF.ar(sig,20);
            sig = LPF.ar(sig,LFSaw.kr([1,2,3,4]/16*~tempo).range(190,9000));

            //sig = GVerb.ar(HPF.ar(sig,900),10,3)/4+sig;
            //sig = GVerb.ar(LPF.ar(sig,900),100,10)/4+sig;
            sig = CompanderD.ar(sig,3/4,1/1.5,1);
            Splay.ar(sig,0.75,0.25);
        };
        //~onea[1] = \filter -> {|sig| DWGReverb.ar(HPF.ar(LPF.ar(sig,5000),50),[5000,5050],1/2)+sig};
        ~onea.play;
    );
)

(
    ~kick.clear(1);
    ~onea.clear(1);
    ~scanner.clear(1);
)

(
    //////////////////////////  FADE -> OUT ///////////////////////////////////////////////////////
    {
        p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clear(20);};});
    }.fork;  
)

(
    ~kick.play;
    ~onea.play;
    ~sell.play;
    ~scanner.play;
)


(
    (
        ~sell.ar(4);
        // ~sell.quantize(~tick);
        ~sell.mold(4);
        ~sell.clock = p.clock;
        ~sell.quant=[2,0];
        ~sell.fadeTime=4;

        ~sell={var t = LFSaw.ar([1/1600,1/1600,[1/4,1/3]]*~tempo) * 1 * LFSaw.ar(1/2000,mul:4); Perlin3.ar(t[0],t[1],t[2]) ** LFNoise2.kr(1/64*~tempo).range(0.4,4);};
    );

    // scanner synth ///////////////////////////////////////

    (
        //~scanner.ar(4);
        //~scanner.clock = p.clock;
        ~scanner.quant=[2,1/4];
        ~scanner.fadeTime=0.2;
        ~scanner[0]={
            //         |       |       |       |       |
            var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0];
            var imp = PulseDivider.kr(~tick.kr,[1,2,3,4],~counter.kr % pat.size);
            var hit = Select.kr(PulseCount.kr(imp)*[1,2,3,4]%pat.size,pat);
            var env = Decay2.kr(imp*hit,1/4*[1,2,3,4]*~tempo*LFNoise2.kr(~tempo/64!4).exprange(1.0,4.9),0.0005);
            var sig = [
                SinOsc.ar(60.midicps/2,Pulse.ar(~tempo/[1,2,3,4]*2))/8,
                LFNoise0.ar(1500*[1,2,3,4]) * Pulse.ar(~tempo/[1,2,3,4]),
                Crackle.ar(0.005*Pulse.ar(~tempo/[1,2,3,4])+0.14) * Pulse.ar(~tempo/[1,2,3,4]);
                LFNoise1.ar(150!2) * Pulse.ar(~tempo/[1,2,3,4]),
            ];
            sig=  HPF.ar(sig,200);
            sig = sig * env
        };

        ~scanner[1] = \filter -> {|sig| SelectX.ar(~sell.ar*(sig.size+1) % (sig.size+1),sig);};
        ~scanner[2] = \filter -> {|sig| Resonz.ar(sig,LFNoise2.kr(1/4/[1,2,3,4] * ~tempo).exprange(100.01,1600),0.2,mul:2)+sig/2};
        ~scanner[3] = \filter -> {|sig| HPF.ar(sig,200); };
        ~scanner[4] = \filter -> {|sig| Limiter.ar(sig)/2 };
        ~scanner[6] = \filter -> {|sig| CompanderD.ar(sig,3/4,1/2,1/2,0.002,1/2) } ;
        ~scanner[7] = \filter -> {|sig| Splay.ar(sig,0.95,0.05,SinOsc.kr(4.01*~tempo)/1.2); };

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

// switch synth ///////////////////////////////////////

(
    ~switch.ar(2);
    ~switch.clock = p.clock;
    ~switch.quant=[2,1/2];
    ~switch.fadeTime=8.2;
    ~switch={
        var pat = [1,0,1,0,1,0,1,0,1,0,1,0,1,1,0,0].pyramid;
        //         |       |       |       |       |
        var imp = PulseDivider.kr(~tick.kr,[1,2,3,4,5,6].scramble,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.1*[1,2,3,4]],[3,30]),imp * hit);
        var sig = WhiteNoise.ar(0.4!4) * Pulse.ar(1000);
        sig = RHPF.ar(sig,[1000,2500,12500,15000,1000,500,12500],LFNoise2.kr(1/4!4 * ~tempo/2).range(0.1,0.9));
        sig = sig * env * LFPulse.ar(1/4/(1..6)*~tempo,0,1/4);
        sig = CompanderD.ar(sig,3/4,1,1/3,0.0002);
        Splay.ar(sig,0.5,0.245);
    };
    ~switch.play;
    //~switch[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),5000),[4001,4011]),12000)+sig;};
);
~switch.play;
~switch.stop(4);
~switch.clear;






// crack synth ///////////////////////////////////////

(
    ~crack.ar(2);
    ~crack.clock = p.clock;
    ~crack.quant=[2,1/4];
    ~crack.fadeTime=4;
    ~crack={
        var sig = Crackle.ar(1.005+LFNoise2.kr(1/4!4*~tempo).range(0.01,0.67),LFPulse.ar(5000*[1,2,1.3333,1.5])/LFSaw.ar(1*~tempo/2).range(1,50)) * Pulse.ar(16*~tempo/2) * LFPulse.ar(8*~tempo/2) * LFPulse.ar(4/[1,2,3,4]*~tempo);
        //sig = DelayN.ar(sig,1/2*~tempo,1/2*~tempo);
        sig = HPF.ar(sig,200);
        Splay.ar(sig,0.05,0.85);
    };
    ~crack.play;
);
~crack.stop(4);
~crack.clear;
~crack.pause;
~crack.resume;


// two synth ///////////////////////////////////////

(
    ~k_two.ar(2);
    ~k_two.clock = p.clock;
    ~k_two.quant=[4];
    ~k_two.fadeTime=0.2;
    ~k_two={
        //         |       |       |       |       |
        var mod = LFNoise2.kr(1/4*~tempo).range(20,80);
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,1,1,1];
        var imp = PulseDivider.kr(~tick.kr,[2,2,1,4],~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.00001],[0.002,1],[10,-3]),imp * hit);
        var sig = SinOsc.ar(33.midicps+(env**(mod*2)*LFNoise1.kr(1/32!4).range(50,350)),env**mod*8pi);
        sig = sig * env;
        Splay.ar(sig,0.25,0.67);
    };
    ~k_two.play;
);
~k_two.stop(4);
~k_two.clear(8);
~k_two.pause;
~k_two.resume;

// hh synth ///////////////////////////////////////

(
    ~hh3.ar(2);
    ~hh3.clock = p.clock;
    ~hh3.quant=[4,1/2];
    ~hh3.fadeTime=0.2;
    ~hh3={
        //         |       |       |       |       |
        var pat = [1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,1,0,0,1,0,1,0];
        var imp = PulseDivider.kr(~tick.kr,[2,2,1,1],~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)/[1,2,3,4]%pat.size,pat);
        var env = EnvGen.ar(Env([0.01,1,0.0001],[0.005,1/8*~tempo],[60,-5]),imp * hit);
        var sig = LFNoise1.ar(16000/[1,4,8,16]) * Crackle.ar([0.1,0.7,0.4,0.6]+env);
        sig = sig * env;
        Splay.ar(sig,0.75,0.115);
    };
    ~hh3.play;
);
~hh3.stop(4);
~hh3.clear(40);
~hh3.pause;
~hh3.resume;

(
    //////////////////////////  FADE -> OUT ///////////////////////////////////////////////////////
    {
        p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clear(20);};});
    }.fork;  
)

// one synth ///////////////////////////////////////

(
    ~mel_tick.ar(2);
    ~mel_tick.clock = p.clock;
    ~mel_tick.quant=[2,1/4];
    ~mel_tick.fadeTime=0.2;
    ~mel_tick={
        var f,sig,slo;
        slo = 4;
        f = TWChoose.kr(Impulse.ar(4/slo/(1..16)*~tempo/4),[60,65,67,64,57,72,36].midicps*4,(7..1),1);
        10.do{|n|
            f = f/{TWChoose.kr(Impulse.kr(1/slo/n/(1..16)*~tempo/4),[1,2,1.333333,1/2,1/1.333333,1.5,1/1.5],[4,4,1/3,1/4,1/3,2,0.5],1)};
        };
        sig = Formant.ar(f,f*4,LFTri.ar(1/slo/(1..16)).range(50,5000)*~tempo/2) * (LFTri.ar(1/8/(1..16))**4);
        sig = Vibrato.ar(sig,[6,7,8,9],0.12) + (WhiteNoise.ar(0.01)*Pulse.ar(TChoose.kr(Impulse.ar(4/slo/(1..16)*~tempo/2),[2,4,8,16,32,44,22,33])));
        sig = sig * (LFTri.ar(1/slo/(1..16)*~tempo/2) ** 3);
        sig = MoogFF.ar(sig,f*2,2.85);
        sig = HPF.ar(sig,100);
        sig = CompanderD.ar(sig,3/4,1/2,1/2);
        Splay.ar(sig,0.25,0.85);
    };
    ~mel_tick.play;
);
~mel_tick.stop(120);
~mel_tick.clear(20);
~mel_tick.pause;
~mel_tick.resume;


// crack synth ///////////////////////////////////////

(
    ~crack.ar(2);
    ~crack.clock = p.clock;
    ~crack.quant=[2,1/2];
    ~crack.fadeTime=4;
    ~crack={
        var sig = Crackle.ar(1+LFNoise2.kr(1/4).range(0.01,0.7),LFPulse.ar(5000*[1,2,1.3333,1.5])/LFSaw.ar(1/4*~tempo/2).range(1,50)) * Pulse.ar(8*~tempo/2) * LFPulse.ar(1*~tempo/2) * LFPulse.ar(7/[1,2,3,4]*~tempo/2);
        Splay.ar(sig,0.5,0.05);
    };
    ~crack.play;
);
~crack.stop(4);
~crack.clear;
~crack.pause;
~crack.resume;


// hiss synth ///////////////////////////////////////

(
    ~hiss.ar(2);
    ~hiss.clock = p.clock;
    ~hiss.quant=[16,1/8];
    ~hiss.fadeTime=2.2;
    ~hiss={
        //         |       |       |       |       |  
        var pat = [1,0,1,0,1,0,1,0,1,0,1,0,1,1,1,1];
        var imp = PulseDivider.kr(~tick.kr,[1,2,3],~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.5],[3,-3],curve:'exp'),imp * hit);
        var sig = Crackle.ar([0.1,0.2,0.6]+env);
        sig = sig * env;
        Splay.ar(sig,0.25,0.195);
    };
    ~hiss.play;
);
~hiss.stop(4);
~hiss.clear(10);
~hiss.clear;
~hiss.pause;
~hiss.resume;

(
    ~chaos.clock = p.clock;
    ~chaos.quant = [16,1/8];
    ~chaos.fadeTime = 1;
    ~chaos={
        var sig;
        var all = [];
        var mod = TWChoose.kr(Impulse.kr(1/32*(1..16)*~tempo),[1,2,4,8,16,1.5,1.33333,1.25,1.125],[7,6,5,4,3,2,1],1).lag(1/10);
        sig = {ChaosGen.allSubclasses.choose.ar}!16 * (Decay.ar(Impulse.ar(2/(1..16) * ~tempo),0.2 * LFNoise2.kr(1/16*~tempo*16).range(0.7,1.2)) ** 2);
        sig = LeakDC.ar(sig) * LFPulse.ar(8*~tempo*mod);
        sig = CompanderD.ar(sig,3/4,1/2,1/2);
        sig = PitchShift.ar(sig,0.05,1/mod/[1,2,1,4,1,8,1,16]);
        //sig = TChoose.ar(Impulse.ar(~tempo*4),all);
        Splay.ar(sig,0.92,0.05,SinOsc.ar(1*~tempo));
    };
    ~chaos.play;
);
~chaos.clear;


// shuttles synth ///////////////////////////////////////

(
    ~shuttles.ar(2);
    ~shuttles.clock = p.clock;
    ~shuttles.quant=[2];
    ~shuttles.fadeTime=4;
    ~shuttles={
        var mod = Duty.kr(4/~tempo,0,Dseq([1,2,1,4,1,1.5],inf));
        var t = LFSaw.ar([1,1/4/[1,2,3,4]*~tempo/2,LFSaw.ar(1/5000).range(1,50)]) + LFSaw.ar(1/4000) * 40;
        var sig = Perlin3.ar(t[0],t[1],t[2])/2;// * Decay.ar(Impulse.ar(8/[1,2,3,4]),0.02);
        var f = 1/[100,200,2000,1000]/mod;// + (Pulse.ar(4/[1,2,3,4],mul:1/100000));
        sig = LocalIn.ar(4) + sig;
        sig = BPF.ar(sig,LFSaw.ar(1/16/[1,2,3,4]*~tempo/2).exprange(50.01,15000),0.1);
        sig = DelayN.ar(sig,f,f) * LFTri.ar(1/32/[1,2,4,1.5] * ~tempo/2).range(0.15,1);
        //sig = Normalizer.ar(sig)/2;
        LocalOut.ar(sig/1.00001);

        Splay.ar(sig,0.95,0.35,SinOsc.kr(2/[5,6]*~tempo));
    };
    ~shuttles.play;
);
~shuttles.stop(4);
~shuttles.clear(20);
~shuttles.pause;
~shuttles.resume;




// raw synth ///////////////////////////////////////

(
    ~raw.ar(2);
    ~raw.clock = p.clock;
    ~raw.quant=[2,1/8];
    ~raw.fadeTime=8;
    ~raw={|speed = 1|
        var sig = VarSaw.ar([55,550] +.x [1,2,3,4,5],0,LFSaw.ar(speed/[1,2,3,4]*~tempo)**4) + GrayNoise.ar(0.5!2) * (1-(LFTri.ar(speed)**4));
        //sig = Resonz.ar(sig,LFSaw.ar(1/32/[1,2,3,4]).exprange(160.001,250)+SinOsc.ar(4).range(-100,1000),LFNoise2.kr(1/32!2).range(0.02,0.6));
        sig = Resonz.ar(sig,LFSaw.ar(speed*~tempo/2).exprange(55.01,5500),LFDNoise1.kr(1/[1,2,3,4]*~tempo/2).range(0.01,0.3));
        sig = Limiter.ar(sig);// * Pulse.ar(4/[1,2,8,16]);
        Splay.ar(sig,0.5,0.055);
    };
    ~raw.play;

    ~raw.map(\speed,~ctl);

    ~ctl.quant=2;
    ~ctl = {Duty.kr(1/4/~tempo*2,0,Dseq([1,8,4,1,8,2,1,8,3,1,8,16,1,8,32,64,128],inf)) * ~tempo/2};

);
~raw.stop(5);
~raw.clear(5);
~raw.pause;
~raw.resume;



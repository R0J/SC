
p.clock.tempo(127/60);


(
    Task({
        p.arProxyNames.do({|synth| p[synth.asSymbol].stop(10);});
        ~master.play;
        (11).wait;
        //p.arProxyNames.do(p[_.asSymbol].clear);
        p.clean;
        ~tick.rebuild;
    }).play(TempoClock.default);  
)

(
    ~tick.kr(1);
    ~tick.clock = p.clock;
    ~tick.quant=1;
    ~tick = { Impulse.kr(~tempo * 4); };
    ~counter = {PulseCount.kr(~tick.kr);};
);    


(
    ~master.clock = p.clock;
    ~master.quant=2;
    ~master.play;

    ~master.fadeTime = 0.01;
    ~master = {|wet=0.45,lp=7500,hp=250,len=20,rev = 5|
        var sig = In.ar([0,1]);
        var orig = sig;
        var fx,hi,lo;
        //fx = Limiter.ar(sig,0.95);
        sig = Limiter.ar(sig);
        fx = LPF.ar(sig,lp);
        fx = HPF.ar(fx,hp);
        lo = DWGReverb.ar(NumChannels.ar(fx,2),[5000,5010],1/len)/rev;
        fx = HPF.ar(sig,lp);
        hi = DWGReverb.ar(NumChannels.ar(fx,2),[4000,4010],1/len/10)/rev/10;
        sig = SelectX.ar(wet,[orig,NumChannels.ar([]++lo+hi,4)]);
        sig = Compander.ar(sig,3/4,1/2,1/3);
        sig = NumChannels.ar(sig,2);
        ReplaceOut.ar(0,sig);
    };
    ~master_wet.quant = 4;
    ~master_wet = {LFDNoise1.kr(~tempo/2).exprange(0.15,0.55);};
    ~master.map(\wet,~master_wet);
);
p.moveNodeToTail(\master);

p.makeTempoClock;
TempoClock.default=p.clock;
p.clean;

p.restartClock;





(
    ~kick.ar(2);
    ~kick.clock = p.clock;
    ~kick.quant = [8];
    ~kick.fadeTime=0.2;
    ~kick={
        //         |       |       |       |       |  
        var pat = [
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,1,1,0,
        ];
        var mod2 = LFNoise2.kr(~tempo/16).range(10,80);
        var mod = LFDNoise1.kr(~tempo/32).range(20,50);
        var imp = PulseDivider.kr(~tick.kr,4,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.001,2.5],[mod,-3.3]), imp * hit);
        var sig;
        var hum = LFDNoise1.kr(~tempo).range(0.0,0.01);

        // env = DelayN.ar(env,hum,hum);

        sig = SinOsc.ar(33.midicps+(env**mod2*33.midicps),env**mod2*8pi);
        sig = sig * env;
        Splay.ar(sig,0.75,0.75);
    };
    ~kick.play;
);
~kick.stop(10);
~kick.clear;
~kick.pause;
~kick.resume;


(
    ~tick.kr(1);
    ~tick.clock = p.clock;
    ~tick.quant=1;
    ~tick = { Impulse.kr(~tempo * 4); };
    ~counter = {PulseCount.kr(~tick.kr);};
);    

(
{
  p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clear(20);};});
}.fork;  
)
// switch synth ///////////////////////////////////////

(
    ~switch.ar(2);
    ~switch.clock = p.clock;
    ~switch.quant=[4,1/2];
    ~switch.fadeTime=8.2;
    ~switch={
        var pat = [
            0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,
        ];
        //         |       |       |       |       |
        var imp = PulseDivider.kr(~tick.kr,[1,2,3,4,5,6],~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.15*[1,2,3,4]/~tempo],[3,30]),imp * hit);
        var sig = WhiteNoise.ar(0.4!4) + Crackle.ar(env+((1..6)/10))* SinOsc.ar(1/(1..6)*~tempo);
        sig = Resonz.ar(sig,[1000,2500,12500,15000,500,12500],LFDNoise1.kr(~tempo!6).range(0.01,0.9));
        sig = sig * env * LFPulse.ar(8/(1..6)*~tempo,0,1/2);
        sig = CompanderD.ar(sig,3/4,1,1/3,0.0002);
        Splay.ar(sig,0.5,0.125,SinOsc.kr(~tempo,mul:0.24));
    };
    ~switch.play;
);
~switch.play;
~switch.stop(10);
~switch.clear;

// raw synth ///////////////////////////////////////

(
    ~raw.ar(2);
    ~raw.clock = p.clock;
    ~raw.quant=[2];
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
~raw.clear;
~raw.pause;
~raw.resume;





// crack synth ///////////////////////////////////////

(
    ~crack.ar(2);
    ~crack.clock = p.clock;
    ~crack.quant=[2,1/2];
    ~crack.fadeTime=4;
    ~crack={
        var sig = Crackle.ar(1+LFNoise2.kr(1/4).range(0.01,0.7),LFPulse.ar(5000*[1,2,1.3333,1.5])/LFSaw.ar(1/4*~tempo/2).range(1,50)) * Pulse.ar(8*~tempo/2) * LFPulse.ar(1*~tempo/2) * LFPulse.ar(7/[1,2,3,4]*~tempo/2);
        Splay.ar(sig,0.5,0.25);
    };
    ~crack.play;
);
~crack.stop(4);
~crack.clear;
~crack.pause;
~crack.resume;



// under_bass synth ///////////////////////////////////////

(
    ~under_bass.ar(2);
    ~under_bass.clock = p.clock;
    ~under_bass.quant=4;
    ~under_bass.fadeTime=4;
    ~under_bass={
        var sig = SinOsc.ar(33.midicps!2,Pulse.ar(1/4/[1,2,3,4]*~tempo)*8pi) * SinOsc.ar(~tempo/4/[1,2,3,4]);
        sig = Limiter.ar(sig,0.9);
        Splay.ar(sig,0.5,0.25);
    };
    ~under_bass.play;
);
~under_bass.stop(4);
~under_bass.clear;
~under_bass.pause;
~under_bass.resume;

// menavka_deep synth ///////////////////////////////////////

(
    ~menavka_deep.ar(2);
    ~menavka_deep.clock = p.clock;
    ~menavka_deep.quant=[4];
    ~menavka_deep.fadeTime=8;
    ~menavka_deep={
        var mal = TWChoose.kr(Impulse.kr(~tempo/8/[1,2,3,4]),[4,2,8,1],[4,0.5,0.3,0.1],1);
        var basemod = TWChoose.kr(Impulse.kr(~tempo/2/[1,1.3333,1.5,2]),[1,2,4,8],[4,0.5,0.3,0.1],1);
        var base = [33].midicps + LFDNoise1.kr(1/4!4,mul:1/2);
        var tmod = 1;//TWChoose.kr(Impulse.kr(~tempo/[1,2,3,4]!4),[1,2,1/4,1/8],[0.6,0.4,0.2,0.1],1);
        var cmod = LFDNoise1.ar(1/8!4 * ~tempo/2).range(0.25,0.95);
        var mod = LFDNoise1.ar(1/32!4 * ~tempo/2).range(0.1,8);
        var sig = SinOsc.ar(base,Pulse.ar(2/[1,2,3,4]*~tempo*tmod)**mod*2pi);
        sig = CompanderD.ar(sig,cmod,1,1/2);
        sig = CombC.ar(sig,1/base,1/base,4 * LFDNoise1.ar(1/16!4 * ~tempo/2).range(0.01,1) / (~tempo/2));
        sig = Normalizer.ar(sig);
        sig = OnePole.ar(sig,LFDNoise1.ar(1/4!4 * ~tempo/2));
        sig = LPF.ar(sig,8000);
        Splay.ar(sig,0.85,0.35,LFPar.kr(~tempo,mul:0.2));
    };
    ~menavka_deep.play;
);
~menavka_deep.stop(4);
~menavka_deep.clear;
~menavka_deep.pause;
~menavka_deep.resume;

// menavka_deep synth ///////////////////////////////////////

(
    ~menavka_deep_mod.ar(2);
    ~menavka_deep_mod.clock = p.clock;
    ~menavka_deep_mod.quant=[4];
    ~menavka_deep_mod.fadeTime=8;
    ~menavka_deep_mod={
        var mal = TWChoose.kr(Impulse.kr(~tempo/8/[1,2,3,4]),[4,2,8,1],[4,0.5,0.3,0.1],1);
        var basemod = TWChoose.kr(Impulse.kr(~tempo/2/[1,1.3333,1.5,2]),[1,2,4,8],[4,0.5,0.3,0.1],1);
        var base = [33].midicps * (basemod * mal/8)+ LFDNoise1.kr(1/4!4,mul:1/2);
        var tmod = 1;
        var cmod = LFDNoise1.ar(1/8!4 * ~tempo/2).range(0.25,0.95);
        var mod = LFDNoise1.ar(1/32!4 * ~tempo/2).range(0.1,8);
        var sig = SinOsc.ar(base,Pulse.ar(2/[1,2,3,4]*~tempo*tmod)**mod*2pi);
        sig = CompanderD.ar(sig,cmod,1,1/2);
        sig = CombC.ar(sig,1/base,1/base,4 * LFDNoise1.ar(1/16!4 * ~tempo/2).range(0.01,1) / (~tempo/2));
        sig = Normalizer.ar(sig);
        sig = OnePole.ar(sig,LFDNoise1.ar(1/4!4*~tempo/2));
        sig = LPF.ar(sig,8000);
        Splay.ar(sig,0.85,0.35,LFPar.kr(~tempo/1.2,mul:0.2));
    };
    ~menavka_deep_mod.play;
);
~menavka_deep_mod.stop(4);
~menavka_deep_mod.clear;
~menavka_deep_mod.pause;
~menavka_deep_mod.resume;

// menavka synth ///////////////////////////////////////

(
    ~menavka.ar(2);
    ~menavka.clock = p.clock;
    ~menavka.quant=[4,1/2];
    ~menavka.fadeTime=16;
    ~menavka={
        var tmod = 1/16;//TWChoose.kr(Impulse.kr(~tempo/[1,2,3,4]!4),[1,2,1/4,1/8],[0.6,0.4,0.2,0.1],1);
        var mal = TWChoose.kr(Impulse.kr(~tempo/8/[1,2,3,4]*tmod),[4,2,8,1],[4,0.5,0.3,0.1],1);
        var basemod = TWChoose.kr(Impulse.kr(~tempo/2/[1,1.3333,1.5,2]*tmod),[1,2,4,8],[4,0.5,0.3,0.1],1);
        var base = [33,33,36].midicps * basemod + LFDNoise1.kr(1/4!4,mul:1/2);
        var cmod = LFDNoise1.ar(1/8!4 * ~tempo/2).range(0.25,0.95);
        var mod = LFDNoise1.ar(1/32!4 * ~tempo/2).range(0.1,8);
        var sig = SinOsc.ar(base,Pulse.ar(2/[1,2,3,4]*~tempo*tmod)**mod*2pi);
        sig = CompanderD.ar(sig,cmod,1,1/2);
        sig = CombC.ar(sig,1/base,1/base/4.001/basemod,4 * LFDNoise1.ar(1/16!4).range(0.01,1) / (~tempo/2));
        sig = Normalizer.ar(sig);
        sig = OnePole.ar(sig,LFDNoise1.ar(1/4!4 * tmod * ~tempo/2));
        sig = LPF.ar(sig,4000);
        Splay.ar(sig,0.85,0.155,LFPar.kr(~tempo/1.1,mul:0.2));
    };
    ~menavka.play;
    ~menavka[1] = \filter -> {|sig| DWGReverb.ar(HPF.ar(LPF.ar(sig,3500),150),[4020,4000],1/5)};
);
~menavka.stop(4);
~menavka.clear;
~menavka.pause;
~menavka.resume;

(
    ~tick.kr(1);
    ~tick.clock = p.clock;
    ~tick.quant=1;
    ~tick = { Impulse.kr(~tempo * 4); };
    ~counter = {PulseCount.kr(~tick.kr);};
);   

(
  s.latency=0;
  //p.clock.tempo_(127/60);
  p.arProxyNames.do({|synth| if(synth!=\master){p[synth.asSymbol].rebuild;};});
  ~tick.kr(1);
  ~tick.clock = p.clock;
  ~tick.quant=4;
  ~counter.clock = p.clock;
  ~counter.quant = 4;
  ~counter = {PulseCount.kr(~tick.kr);};
  ~tick = { Impulse.kr(~tempo*4); };
);


// flute synth ///////////////////////////////////////

(
    ~flute3.ar(2);
    ~flute3.clock = p.clock;
    ~flute3.quant=[2,1/2];
    ~flute3.fadeTime=8;
    ~flute3={
        var speed = 1/8/[1,2,4] * ~tempo;
        var oct = TWChoose.kr(Impulse.kr(speed/2),[0,1,2,3] *.x [0,12],(8..1),1);
        var f = TWChoose.kr(Impulse.kr(speed/4),[60,64,67,65,71,61]-12+oct,[1,1,1,0.2,0.1,0.1],1).midicps/4;
        //var mod = Duty.kr(4,0,Dseq([1,2,1.5,1.33333],inf));
        var sig = VarSaw.ar([f,4.41/f,speed/40],0,LFDNoise1.kr(speed!3))*14.41;//*f/30;
        sig = CombN.ar(sig,1/f,8/f,1)/8+sig;
        sig = Perlin3.ar(sig[0],sig[1],sig[2]);
        sig = Resonz.ar(sig,f,SinOsc.kr(1/40/[1,2,3]).range(0.001,0.04)) * VarSaw.ar(f+SinOsc.kr(1/f/[1,2,3],mul:pi),(sig.distort*pi),LFDNoise1.kr(speed/2!3));
        sig = Normalizer.ar(sig) * (LFTri.ar(speed/[1,2,3]/4) ** 4 * [1,0.66,0.33]);
        sig = LeakDC.ar(sig);
        sig = Vibrato.ar(sig,[5,6,7],0.02);
        //sig = LPF.ar(sig,f*4);
        //sig = sig * Pulse.ar(1/4/[1,2,3,4]*~tempo);
        sig = CompanderD.ar(sig,3/4,1,1/2);
        sig = LPF.ar(sig,3000);
        Splay.ar(sig,0.65,0.125);
    };
    ~flute3.play;
    ~flute3[1] = \filter -> {|sig| DWGReverb.ar(HPF.ar(LPF.ar(sig,3500),150),[4020,4000],1/5)};
);
~flute3.stop(30);
~flute3.clear;
~flute3.pause;
~flute3.resume;

// flute synth ///////////////////////////////////////

(
    ~flute2.ar(2);
    ~flute2.quant=2;
    ~flute2.fadeTime=8;
    ~flute2={
        var speed = 1/8/[1,2,4] * ~tempo;
        var oct = TWChoose.kr(Impulse.kr(speed/4),[0,-1,2,1]*12,[1,0.2,0.1,0.5],1);
        var f = TWChoose.kr(Impulse.kr(speed/8),[60,64,67,69,71]-12+oct,[1,1,1,0.2,0.1],1).midicps/2;
        //var mod = Duty.kr(4,0,Dseq([1,2,1.5,1.33333],inf));
        var sig = VarSaw.ar([f,4.41/f,speed/40],0,LFDNoise1.kr(speed!3))*14.41;//*f/30;
        sig = CombN.ar(sig,1/f,8/f,1)/8+sig;
        sig = Perlin3.ar(sig[0],sig[1],sig[2]);
        sig = Resonz.ar(sig,f,SinOsc.kr(1/40/[1,2,3]).range(0.001,0.04)) * VarSaw.ar(f+SinOsc.kr(1/f/[1,2,3],mul:pi),(sig.distort*pi),LFDNoise1.kr(speed/2!3));
        sig = Normalizer.ar(sig) * (LFTri.ar(speed/[1,2,3]/4) ** 4 * [1,0.66,0.33]);
        sig = LeakDC.ar(sig);
        sig = Vibrato.ar(sig,[5,6,7],0.02);
        //sig = LPF.ar(sig,f*4);
        sig = CompanderD.ar(sig,1,1/2);
        Splay.ar(sig,0.35,0.15);
    };
    ~flute2.play;
    ~flute2[1] = \filter -> {|sig| DWGReverb.ar(HPF.ar(LPF.ar(sig,3500),150),[4020,4000],1/2)};
);
~flute2.stop(30);
~flute2.clear;
~flute2.pause;
~flute2.resume;

// flute synth ///////////////////////////////////////

(
    ~flute1.ar(2);
    ~flute1.quant=2;
    ~flute1.fadeTime=4;
    ~flute1={
        var speed = 1/8/[1,2,4] * ~tempo;
        var oct = TWChoose.kr(Impulse.kr(speed/4),[0,-1,2,1]*12,[1,0.2,0.1,0.5],1);
        var f = TWChoose.kr(Impulse.kr(speed/8),[60,64,67,69,71]+12+oct,[1,1,1,0.2,0.1],1).midicps/2;
        //var mod = Duty.kr(4,0,Dseq([1,2,1.5,1.33333],inf));
        var sig = VarSaw.ar([f,4.41/f,speed/40],0,LFDNoise1.kr(speed!3))*14.41;//*f/30;
        sig = CombN.ar(sig,1/f,8/f,1)/8+sig;
        sig = Perlin3.ar(sig[0],sig[1],sig[2]);
        sig = Resonz.ar(sig,f,SinOsc.kr(1/40/[1,2,3]).range(0.001,0.04)) * VarSaw.ar(f+SinOsc.kr(1/f/[1,2,3],mul:pi),(sig.distort*pi),LFDNoise1.kr(speed/2!3));
        sig = Normalizer.ar(sig) * (LFTri.ar(speed/[1,2,3]/4) ** 4 * [1,0.66,0.33]);
        sig = LeakDC.ar(sig);
        sig = Vibrato.ar(sig,[5,6,7],0.02);
        //sig = LPF.ar(sig,f*4);
        sig = CompanderD.ar(sig,1,1/2);
        Splay.ar(sig,0.35,0.05);
    };
    ~flute1.play;
    ~flute1[1] = \filter -> {|sig| DWGReverb.ar(HPF.ar(LPF.ar(sig,3500),150),[4020,4000],1/2)};
);
~flute1.stop(30);
~flute1.clear;
~flute1.pause;
~flute1.resume;


// harm synth ///////////////////////////////////////

(
    ~harm.ar(2);
    ~harm.quant=2;
    ~harm.fadeTime=2;
    ~harm={
        var speed = 1/16/(1..8) * ~tempo;
        var master = Duty.kr(1/speed,0,Dseq([1,1.33333,2,1.5].pyramid,inf));
        var change = Impulse.kr(speed);
        var tonic = Duty.kr(1/speed*8,0,Dseq([1,5/4,3/2,15/8].scramble.pyramid,inf)) ;
        var mel = Duty.kr(1/speed*16,0,Dseq([1,2,3,4,3,4,5,6].scramble.octcps,inf));
        var f = mel * tonic + SinOsc.kr(1/6/(1..4),mul:0.3) /  master;
        //var sig = SinOsc.ar(f,mul:LFNoise2.kr(1/16!8)) +PinkNoise.ar(0.1!8);
        var sig = BrownNoise.ar(1!8);

        sig = Vibrato.ar(sig,[3,4,5,6,5,6,7],0.02) ;//SinOsc.ar(1/f/[9/8,4/3,5/3,2]);
        sig = LPF.ar(sig,2000);
        //sig = MoogFF.ar(sig,f*4,3.5);
        //sig = AllpassN.ar(sig * (LFTri.ar(speed)**2),1/f,1/f*4,4);
        sig = Resonz.ar(sig,f,0.4);
        sig= CombN.ar(sig,1/f,1/f*10,8);
        sig = LeakDC.ar(sig);
        sig = AllpassN.ar(sig * (LFTri.ar(speed)**2),1/f,1/f,8);
        sig = CompanderD.ar(sig,3/4,1,1/10);
        //sig = sig * LFSaw.ar(speed);
        Splay.ar(sig,0.95,0.15);
    };
    ~harm.play;
    //~harm[1] = \filter -> {|sig| DWGReverb.ar(LPF.ar(HPF.ar(sig,200),3500),[4020,4000])/2+sig;};
);
~harm.stop(4);
~harm.clear;
~harm.pause;
~harm.resume;


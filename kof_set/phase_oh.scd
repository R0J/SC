

(

    //    p.clock.tempo_(127/60);
    //    TempoClock.default = p.clock;

    (
        (
            ~tick.kr(1);
            ~tick.clock = p.clock;
            ~tick.quant=1;
            ~tick = { Impulse.kr(~tempo * 4); };
            ~counter = {PulseCount.kr(~tick.kr);};
        );    

    );

    (
        ~master.quant=2;
        ~master.play;
        ~master.fadeTime = 0.01;
        ~master = {|wet=0.35,lp=4500,hp=150,len=10,rev = 2|
            var sig = In.ar([0,1]);
            var orig = sig;
            var fx,low,high,mid;
            //fx = Limiter.ar(sig,0.95);

            //sig = CombN.ar(sig,1/50,1/50,1);
            low = CompanderD.ar(LPF.ar(sig,LFNoise2.kr(1/4!2*~tempo).range(100,800)),3/4,1,1/2);
            mid = CompanderD.ar(BPF.ar(sig,LFNoise2.kr(1/8!2*~tempo).range(500,4500),1/2),3/4,1,1/2);
            high = CompanderD.ar(HPF.ar(sig,LFNoise2.kr(1/16!2*~tempo).range(3000,10000)),3/4,1,1/2);
            sig = CompanderD.ar((high+mid+low).flat,3/4,1/1.4,1/1.2); 
            //sig = Compander.ar(sig,3/4,1/2,1/3);
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
        p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clear(2);};});
    }.fork;  
)

(
    s.latency=0;
    //p.clock.tempo_(120/60);
    p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].rebuild;};});
    ~tick.kr(1);
    ~tick.clock = p.clock;
    ~tick.quant=4;
    ~counter.clock = p.clock;
    ~counter.quant = 4;
    ~counter = {PulseCount.kr(~tick.kr);};
    ~tick = { Impulse.kr(~tempo*4); };
);

// one synth ///////////////////////////////////////

(
    ~onea.ar(2);
    ~onea.clock = p.clock;
    ~onea.quant=4;
    ~onea.fadeTime=8.2;
    ~onea={
        //         |       |       |       |       |  
        var pat = [
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
        ];
        var imp = PulseDivider.kr(~tick.kr,1,~counter.kr);
        var imp2 = PulseDivider.kr(~tick.kr,[4,8,16,3],~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var hit2 = Select.kr(PulseCount.kr(imp2)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,2.5],[3,-3],curve:'exp'),imp * hit);
        var env2 = EnvGen.ar(Env([0.001,1,0.0001],[0.002,3.5],[3,-3],curve:'exp'),imp2 * hit2);
        var sig = SinOsc.ar(55!2 + (env**50*500));

        sig = LFSaw.ar(55+(env2**20*150))**LFNoise2.kr(1/16!2 * ~tempo).range(0.4,4)/1.5+sig;
        sig = LPF.ar(sig,900);

        sig = sig * env;

        //sig = GVerb.ar(HPF.ar(sig,900),10,3)/4+sig;
        //sig = GVerb.ar(LPF.ar(sig,900),100,10)/4+sig;
        sig = CompanderD.ar(sig,3/4,1/1.5,1);
        Splay.ar(sig,0.75,0.265);
    };
    //~onea[1] = \filter -> {|sig| DWGReverb.ar(HPF.ar(LPF.ar(sig,5000),50),[5000,5050],1/2)+sig};
    ~onea.play;
);
~onea.stop(4);
~onea.clear(2);
~onea.pause;
~onea.resume;

NodeProxy.help


/// 8 KICK start

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
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
        ];
        var mod2 = LFNoise2.kr(~tempo/160).range(20,80);
        var mod = LFDNoise1.kr(~tempo/160).range(20,80);
        var imp = PulseDivider.kr(~tick.kr,8,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,2.5],[mod,-3.3]), imp * hit);
        var sig;
        var hum = LFDNoise1.kr(~tempo).range(0.0,0.01);

        // env = DelayN.ar(env,hum,hum);

        sig = Mix.fill(4,{|i|SinOsc.ar(33.midicps+(env**mod2*33.midicps),env**(mod2+i)*8pi)})/2;
        sig = sig * env;
        Splay.ar(sig,0.75,0.5);
    };
    ~kick.play;
);
~kick.stop(10);
~kick.clear(6);
~kick.pause;
~kick.resume;

// circular synth ///////////////////////////////////////

(
    ~circular.ar(2);
    ~circular.clock = p.clock;
    ~circular.quant=2;
    ~circular.fadeTime=0.004;
    ~circular={
        var xx;
        var yy;
        var speed = (1..16) / ~tempo * 4;
        var filfreq2 = Duty.ar(speed*16,0,Dseq(([60,64,67,69] +.x [0,0,0,-12,-24,12,24,36,48]).midicps,inf)) + SinOsc.kr(1/8/speed,mul:10).lag(4);
        var filfreq = Duty.ar(speed*4,0,Dseq(([60,64,67,69] +.x [0,0,0,-12,-24,12,24,36,48]).midicps,inf)) + SinOsc.kr(filfreq2/speed,mul:100).lag(4);
        var ff = filfreq+(speed/100);
        var loop = filfreq+(speed/100);
        var t = LFSaw.ar(loop).range(0,2pi) + LFTri.ar(1/400/speed,mul:44100);
        var st = LFTri.ar(loop/10).range(0,2pi) + LFTri.ar(1/40,mul:8pi);
        var x = t.cos;
        var y = t.sin;
        var sx = st.cos;
        var sy = st.sin;


        var sig = Perlin3.ar(x,y,t*ff);
        var bck = Perlin3.kr(sx,sy,st);
        sig = BBandPass.ar(sig,filfreq2,LFTri.ar(1/16/speed).exprange(0.0005,1)**4/2);
        sig = Resonz.ar(sig,filfreq*4,0.62);
        sig = Normalizer.ar(sig) * (LFTri.kr(1/4/speed)**4) * (LFTri.kr(1/16/speed)**4);
        sig = LeakDC.ar(sig);
        sig = AllpassN.ar(PitchShift.ar(sig,2/filfreq,1/[1,1.5,2,1.3333]),1,1,[1,2,3,4]);
        sig = sig/4+LPF.ar(sig,3000);
        sig = CompanderD.ar(sig,1/2,1,1/4);
        #xx,yy = Pan4.ar(NumChannels.ar(sig,4),sx,sy)+sig;
        sig = []++xx++yy;
        Splay.ar(sig,0.75,0.25);
    };
    ~circular.play;
    ~circular[1] = \filter -> {|sig| DWGReverb.ar(HPF.ar(LPF.ar(sig,5000),50),[5000,5050],1/2)+sig};
    ~circular[2] = \filter -> {|sig| NumChannels.ar(Resonz.ar(sig,LFTri.ar(1/16/(1..16)).exprange(130,14000),0.42),2)*2+sig;};
);
~circular.stop(4);
~circular.clear(2);
~circular.pause;
~circular.resume;

// hiss synth ///////////////////////////////////////

(
    ~hiss.ar(2);
    ~hiss.clock = p.clock;
    ~hiss.quant=[2,1/2];
    ~hiss.fadeTime=0.2;
    ~hiss={
        //         |       |       |       |       |  
        var pat = [1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0];
        var imp = PulseDivider.kr(~tick.kr,[1,2,3],~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.5],[3,-3],curve:'exp'),imp * hit);
        var sig = Crackle.ar([0.1,0.2,0.6]+env);
        sig = sig * env;
        Splay.ar(sig,0.25,0.095);
    };
    ~hiss.play;
);
~hiss.stop(4);
~hiss.clear;
~hiss.pause;
~hiss.resume;

(
    ~chaos.clock = p.clock;
    ~chaos.quant = [4,1/2];
    ~chaos.fadeTime = 10;
    ~chaos={
        var sig;
        var all = [];
        var mod = TWChoose.kr(Impulse.kr(1/32!8*~tempo),[1,2,4,8,16,1.5,1.33333,1.25,1.125],[7,6,5,4,3,2,1],1).lag(1/4);
        sig = {ChaosGen.allSubclasses.choose.ar}!16 * Pulse.ar(4/(1..16) * ~tempo);
        sig = LeakDC.ar(sig);
        sig = CompanderD.ar(sig,3/4,1/2,1/2);
        sig = PitchShift.ar(sig,0.05,1/mod/[1,2,1,4,1,8,1,16]);
        //sig = TChoose.ar(Impulse.ar(~tempo*4),all);
        Splay.ar(sig,0.92,0.05);
    };
    ~chaos.play;
);
~chaos.clear(10);


// lark synth ///////////////////////////////////////

(
    ~lark.ar(2);
    ~lark.clock = p.clock;
    ~lark.quant=2;
    ~lark.fadeTime=4;
    ~lark={
        var mod = Duty.kr(4/~tempo,0,Dseq([1,2,1.5,1.333333],inf));
        var a = LFTri.ar(1/4/[1,2,3,4]*~tempo) + LFTri.ar(1/4000,mul:10);
        var t = Perlin3.ar(a,a,a);
        var f = t.exprange(450.1,5230).round(1.octcps)/mod*[1,2,1.3333,1.5];
        var sig = PinkNoise.ar(0.12!4) + LocalIn.ar(4);
        sig = DelayN.ar(sig,1/f,1/f);
        sig = SelectX.ar( LFTri.ar(1/16/[1,2,3,4]*~tempo) ** 2 * 2, [LPF.ar(sig,f),BPF.ar(sig,f),DFM1.ar(sig,f,0.2)]);
        sig = CompanderD.ar(sig,3/4,1/1.52,1/1.02,0.0002,0.1);
        //sig = PitchShift.ar(sig,1/f,2.0195);
        LocalOut.ar(Normalizer.ar(sig));
        sig = CompanderD.ar(sig,3/4,1/2,1/2);
        Splay.ar(sig * LFTri.ar(1/4/[1,2,3,4]*~tempo),0.95,0.15);
    };
    ~lark.play;
    ~lark[10] = \filter -> {|in| NumChannels.ar(GVerb.ar(Resonz.ar(in,1500/[1,2,4,1.5]) * LFSaw.ar(1/2/[1,2,3,4]),[10,20,90,89])+in,2);};
);
~lark.clear(10);




(

    p.clock.tempo_(127/60);
    TempoClock.default = p.clock;

);
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
    ~master.clock = p.clock;
    ~master.play;
    ~master.fadeTime = 0.01;
    ~master = {|wet=0.45,lp=2500,hp=250,len=20,rev = 2|
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
        sig = SelectX.ar(wet,[orig,NumChannels.ar(lo+hi,2)]);
        sig = Compander.ar(sig,3/4,1/2,1/3);
        sig = NumChannels.ar(sig,2);
        ReplaceOut.ar(0,sig);
    };
    ~master_wet.quant = 4;
    ~master_wet = {LFDNoise1.kr(~tempo/2).exprange(0.15,0.35);};
    ~master.map(\wet,~master_wet);
    p.moveNodeToTail(\master);
);

)


NodeProxy.help

// circular synth ///////////////////////////////////////

(
    ~circular.ar(2);
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
~circular.clear(20);
~circular.pause;
~circular.resume;

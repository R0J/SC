


(
//////////////////////////  FADE -> OUT ///////////////////////////////////////////////////////
{
  p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clear(20);};});
}.fork;  
)

(
    //////////////////////////  FADE -> OUT ///////////////////////////////////////////////////////
    {
        p.arProxyNames.do({|synth| if(synth.asSymbol!=\master){p[synth.asSymbol].clear(20);};});
    }.fork;  
)

(
    ~master.quant=16;
    ~master.play;
    ~master.fadeTime = 0.01;
    ~master = {|wet=0.15,lp=3500,hp=250,len=10,rev = 3|
        var sig = In.ar([0,1]);
        var orig = sig;
        var fx;
        //fx = Limiter.ar(sig,0.95);
        sig = Compander.ar(sig,3/4,1/1.1,1/3);
        fx = LPF.ar(sig,lp);
        fx = HPF.ar(fx,hp);
        fx = DWGReverb.ar(NumChannels.ar(fx,2),[5000,5010],1/len)/rev;
        sig = SelectX.ar(wet,[orig,fx]);
        sig = NumChannels.ar(sig,2);

        //sig = sig * TwoPole.ar(sig,TRand.kr(Impulse.kr(1/4*~tempo),500,16000),LFDNoise1.kr(1/4*~tempo).range(0.1,0.99));
        //sig = Normalizer.ar(sig);
        //sig = LeakDC.ar(sig);

        ReplaceOut.ar(0,sig);
    };
);
p.moveNodeToTail(\master);

// intercorporator synth ///////////////////////////////////////

(
    ~intercorporator.ar(2);
    ~intercorporator.clock = p.clock;
    ~intercorporator.quant=[16,0];
    ~intercorporator.fadeTime=4;
    ~intercorporator={
        var speed = 1;
        var fmod = Duty.kr(64/~tempo / speed,0,Dseq(1/[1,2,1.5,1.33333],inf));
        var len = (1..16);
        var mod = Duty.kr(64/~tempo*(1..16).nthPrime / speed,0,Dseq([1,2,1.5,1.33333,1,1/2,1/1.5,1/1.666666],inf));
        var env =  Decay.ar(Impulse.ar(~tempo/len/(1..16).nthPrime * speed),len/(1..16) / ~tempo*4/speed);
        var f = [60,48,36,72].midicps/mod/.x[4,2,1,1.5] * fmod;
        var sig = VarSaw.ar(f,0,env**LFNoise2.kr(~tempo/32!16).range(0.5,4.6));
        sig = sig + SinOsc.ar(f*2,env**LFNoise2.kr(1/160!16*~tempo).range(0.2,20.0)*8pi,mul:0.1) * env;
        sig = sig ** LFDNoise1.ar(4*~tempo!16).range(0.1,1.9);
        sig = LPF.ar(sig,LFDNoise1.kr(1/16!16).range(500,15000));
        sig = CompanderD.ar(sig,3/4,1/2,1/2);
        Splay.ar(sig,0.5,0.15);
    };
    ~intercorporator.play;
);
~intercorporator.stop(4);
~intercorporator.clear(120);
~intercorporator.pause;
~intercorporator.resume;


Integrator.help

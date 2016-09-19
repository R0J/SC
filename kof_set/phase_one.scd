/*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see <http://www.gnu.org/licenses/>.
*
* Copyright (C) kof, 2016
*/


p.arProxyNames.collect{|n| var snth = (p[n.asSymbol]);if(true){snth.stop(30)}};
p.arProxyNames.collect{|n| var snth = (p[n.asSymbol]);snth.clear};


(
    p=ProxySpace.push(s.reboot);
    p.makeTempoClock;
);

ProxyMixer.new(p,16);



(
    p.clock.tempo_(125/60);

    ~offset = {(0..7)/8};

    ~tick.quant = 1;
    ~tick = {Mix(Impulse.kr( ~tempo / 2 , ~offset ))};

    ~counter = {PulseCount.kr(~tick.kr)};
);

// moog synth ///////////////////////////////////////

(
    ~moog.ar(2);
    ~moog.quant=[2,1/2];
    ~moog.fadeTime=16.2;
    ~moog={
        var pat = 1/64/[1,2,16]*~tempo.kr;
        var harm = TChoose.kr(pat,[1,2,4,1.5,1.33333,0.5,0.25]);
        var f =  [60,57].midicps / 4 / {TChoose.kr(pat,[1,2,4,1.5,1.33333,0.5,0.25])}!16 + Mix.kr(LFSaw.kr((1..16)));
        var sig = LFSaw.ar(f,mul:[0.7,0.4,0.2]) * (LFTri.ar(pat*~tempo.kr)**4) * SinOsc.ar(f);
        sig = MoogFF.ar(sig,f*[2,4,1.5,10,20,15]*harm,LFNoise2.kr(1/4!4).range(1.5,3.5));
        sig = CompanderD.ar(sig,3/4,1/1.2,1/2);
        //sig = sig * env;
        Splay.ar(sig,0.75,0.95);

    };
    ~moog.play;
    ~moog[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),30),[4004,4014]),LFNoise2.kr(1/4!2).range(1000,17000))+sig;};
);
~moog.stop(4);
~moog.clear;
~moog.pause;
~moog.resume;



// saw synth ///////////////////////////////////////

(
    ~saw.ar(2);
    ~saw.quant=2;
    ~saw.fadeTime=3.2;
    ~saw={
        var mod = Duty.kr(4*~tempo.kr,0,Dseq([1,2,1.5,1.33333],inf));
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,[4,16,64],~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[[1.602,4,16],0.15],[3,-3],curve:'exp'),imp * hit);
        var sig = [] ++ LFSaw.ar([36.midicps,2500/mod],mul:[1,0.1,0.05]) ++ (SinOsc.ar(15000,mul:0.1) * Pulse.ar(8*~tempo.kr));
        sig = sig * env;
        Splay.ar(sig,0.05,0.75);
    };
    ~saw.play;
    ~saw[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),50),[4000,4010]),1500+LFTri.kr(1/16!2).range(0,10000));};
);
~saw.stop(4);
~saw.clear;
~saw.pause;
~saw.resume;



// first perlin noisy synth ///////////////////////////////////////
(
    ~one_weird.ar(2);
    ~one_weird.quant=[4,1/2];
    ~one_weird.fadeTime=8;
    ~one_weird={
        var t = LFTri.ar([1/64,50,1/32/(1..16)]) * ((1..16)*10);
        var f = Perlin3.ar(t[0],t[1],t[2]);
        var sig = Formant.ar(f.range(50,5000/[1,2,1.5,4]),f.range(50,5000).scramble,f.range(50,5000).reverse,mul:LFTri.kr(1/(1..16))**4 );
        sig = sig ** 0.05;
        sig = CompanderD.ar(BPF.ar(sig,1500/8*(1..8),0.5),3/4,1/LFNoise2.kr(1/4!8).range(1,4),1/2);

        Splay.ar(sig,0.95,0.95);
    };

    ~one_weird_env.ar(2);
    ~one_weird_env.quant=[2,1/4];
    ~one_weird_env.fadeTime=0.2;
    ~one_weird_env={
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,16,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[7,0.005],[5,30],curve:'exp'),imp * hit);
        var sig = ~one_weird.ar;
        sig = sig * env;
        Splay.ar(sig,0.75,0.35);
    };
    ~one_weird_env.play;
    ~one_weird_env[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),50),[4001,4011]),5000)+sig;};

);
~one_weird_env.stop(4);
~one_weird_env.clear;
~one_weird_env.pause;
~one_weird_env.resume;


///// second perlin noisy weirdo FFT_WEIRDO ///////////////////////////////////////////////////////////////////////////

(
    b = Buffer.alloc(s,2048,1);
    ~a.ar(2);
    ~a.quant=2;
    ~a.fadeTime=8;
    ~a.mold(2);
    ~a =  { var in, chain,sig;
        var mel = TChoose.kr(Impulse.kr(1/32*~tempo.kr),[1.5,1.3333,1,2]);//Duty.kr(1/50,0,Dseq([1,2,1.5,1.33333],inf));
        var mod = SinOsc.ar(1/50,mul:0.05);
        var t = LFSaw.ar([1/16,1/4000,60/mel]) * 480 * (LFTri.ar(1/32)**2);
        in = Perlin3.ar(t[0],t[1],t[2]);
        in = Normalizer.ar(in) + RLPF.ar(Pulse.ar(4),8000);
        chain = FFT(b.bufnum, in) ;
        chain = PV_MagFreeze(chain, Decay.kr(Dust.kr(0.15),0.1) );
        chain = PV_BrickWall(chain, mod);
        chain = PV_MagShift(chain, 0.5 );
        chain = IFFT(chain);

        sig = chain;

        sig = LPF.ar(sig,18000);
        sig = HPF.ar(sig,40);
        sig = AllpassN.ar(sig,1/[100,101],1/5,4);
        sig = CombL.ar(sig,1/50*mel,1/50*mel,4);
        sig = CompanderD.ar(sig,3/4,1/3,1/10,0.002);
        Splay.ar(sig,0.99,0.35);

    };

    // kick2 synth ///////////////////////////////////////

    ~a_env.ar(2);
    ~a_env.quant=[4,1/4];
    ~a_env.fadeTime=0.2;
    ~a_env={
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,[2,4]*8,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[[4.2502,0.99],1/40],[5,-3],curve:'exp'),imp * hit);
        //var sig = Mix.fill(10,{|n| SinOsc.ar(50!2,env**(n+10)*8pi)});
        var sig = ~a.ar;
        sig = sig * env;
        Splay.ar(sig,0.85,0.5,SinOsc.ar(1/[4,5])/2);
    };
    ~a_env.play;

    ~a_env[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),50),[4002,4012]),1600+LFSaw.kr(1/32!2).range(0,10000))+sig;};
);
~a_env.stop(4);
~a_env.clear;
~a_env.pause;
~a_env.resume;



// slowmo, player synth ///////////////////////////////////////
b = Buffer.read(s,Platform.resourceDir ++ "/sounds/beat.wav");

(
    ~smp.ar(2);
    ~smp.quant=[2,1/2];
    ~smp.fadeTime=2.02;
    ~smp={
        var mod = 1/8/[1.5,1.25,1/1.3333];
        var spread = 0.0;
        var pos = [0,0.5,0.25,0.6] + LFSaw.ar(1/4000);
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,[8,4,2,1]*4,~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,6.05],[10,3],curve:'exp'),imp * hit);
        var sig = PlayBuf.ar(b.numChannels,b.bufnum,BufRateScale.kr(b.bufnum)*mod+(env**0.01/100),imp, (pos) * BufFrames.kr(b.bufnum),2);

        sig = sig * env;
        sig = Vibrato.ar(sig,[4,5],0.02);
        Splay.ar(sig.flat,0.5,0.5);
    };
    ~smp.play;
    ~smp[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),100),[4003,4013]),1500+LFSaw.kr(1/32!2).range(0,10000))+sig;};
);

~smp.stop(7);
~smp.clear;

// hard sin nabeh, synth ///////////////////////////////////////

(
    ~sup.ar(2);
    ~sup.quant=[2,-0.01];
    ~sup.fadeTime=0.02;
    ~sup={
        //         |       |       |       |       |
        var pat = [
            0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
        ].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,8,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.000001,1,0.0001],[4.00002,0.005],[3,-3],curve:'exp'),imp * hit);
        var sig = Mix.fill(10,{|n| SinOsc.ar(50+(n/100),env**(10+(n*5))*8pi) + WhiteNoise.ar(env**4)})/2;

        sig = sig * env;
        Splay.ar(sig,0.05,0.25);
    };
    ~sup.play;
    ~sup[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),30),[4004,4014]),LFNoise2.kr(1/4!2).range(10000,17000))+sig;};

);
~sup.stop(4);
~sup.clear;
~sup.pause;
~sup.resume;


//////////////////////////////////////////////////////
//////////////////////////////////////////////////////
// pre-kick synth ///////////////////////////////////////

(
    ~kick_even.ar(2);
    ~kick_even.quant=[4,1];
    ~kick_even.fadeTime=0.02;
    ~kick_even={
        //         |       |       |       |       |
        var pat = [
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
        ].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,4,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[1.5,0.005],[3,-3],curve:'exp'),imp * hit);
        var sig = SinOsc.ar(50,env**40*8pi) + PinkNoise.ar(env**2*2);
        //sig= Limiter.ar(sig,0.9);
        sig = sig * env;
        sig = Clip.ar(sig,-0.95,0.95);
        Splay.ar(sig,0.05,0.5);
    };
    ~kick_even.play;

    ~kick_even[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),60),[3000,3010]),LFNoise2.kr(1/4!2).range(1000,7000))+sig;};
);
~kick_even.stop(4);
~kick_even.clear;


// kick synth ///////////////////////////////////////

(
    ~kick.ar(2);
    ~kick.quant=[4,-0.01];
    ~kick.fadeTime=0.02;
    ~kick={
        //         |       |       |       |       |
        var pat = [
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,1,0,
        ].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,2,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.0002,2.75],[3,-3],curve:'exp'),imp * hit);
        var sig = Mix.fill(4,{|n| SinOsc.ar(26.midicps+(env**0.5*2),env**(50+(n/200))*8pi) + BrownNoise.ar(env**46)});
        //sig= Limiter.ar(sig,0.9);
        sig = sig * env/3.9;
        //sig = Clip.ar(sig,-0.95,0.95);

        Splay.ar(sig,0.05,0.95);
    };
    ~kick.play;
);
~kick.stop(4);
~kick.clear;
~kick.resume;



// one synth (too heavy CPU) ///////////////////////////////////////

(
    ~one_puls.ar(2);
    ~one_puls.quant=[2,-1/2];
    ~one_puls.fadeTime=2;
    ~one_puls={
        var t = LFSaw.ar([100,2000,[50,500,5000,1/4]]) * 200 ;
        var perl = Perlin3.ar(t[0],t[1],t[2]);
        var puls = Pulse.ar(LFNoise0.ar(1/4!4).round(1/8)*[16,32,2,1/4]*~tempo.kr);
        var sig = perl * puls;
        sig = RHPF.ar(sig,1500/[100,50,10,1],0.2);
        sig = sig * (LFTri.ar(1*~tempo.kr)**20);
        sig = CompanderD.ar(sig,1/2,1,1/10);
        Splay.ar(sig,0.75,0.1595);
    };
    ~one_puls.play;
    // ~one_puls[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),3000),[4000,4010]),15000);};
);
~one_puls.stop(4);
~one_puls.clear;
~one_puls.pause;
~one_puls.resume;




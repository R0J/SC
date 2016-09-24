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


p.arProxyNames.collect{|n| var snth = (p[n.asSymbol]);if(true){snth.stop(10)}};

p.arProxyNames.collect{|n| var snth = (p[n.asSymbol]);if(true){snth.play}};

p.arProxyNames.collect{|n| var snth = (p[n.asSymbol]);snth.clear};
p.clearAll;

Server.local.peakCPU


(
    p=ProxySpace.push(s.reboot);
    p.makeTempoClock;
);

s.notify;
Server.local.makeGui();

Server.help

ProxyMixer.new(p,16);


// aaa synth ///////////////////////////////////////

(
~aaa.ar(2);
~aaa.quant=2;
~aaa.fadeTime=0.2;
~aaa={
  //         |       |       |       |       |  
  var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(1);
  var imp = PulseDivider.kr(~tick.kr,[4,3,2,1],~counter.kr%pat.size);
  var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
  var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.15],[3,-3],curve:'exp'),imp * hit);
  var sig = SinOsc.ar([5000,500],mul:0.1);
  sig = sig * env;
  Splay.ar(sig,0.75,0.5);
};
~aaa.play;
);
~aaa.stop(4);
~aaa.clear;
~aaa.pause;
~aaa.resume;


(
    p.clock.tempo_(127/60);

    ~offset = {(0..7)/8};

    //~tick.quant = 0;
    ~tick = {Mix(Impulse.kr( ~tempo / 2 , ~offset ))};

    //~counter.quant = 0;
    ~counter = {PulseCount.kr(~tick.kr)};

);

p.clock.tempo_(127/60);

// karplus.g synth ///////////////////////////////////////

(
    ~karplus.ar(2);
    ~karplus.quant=2;
    ~karplus.fadeTime=4;
    ~karplus={
        var pat = 1/4/[1,2,3,4] * ~tempo.kr;
        var sig = (BrownNoise.ar(0.25!2) * LFPulse.ar(pat,0,0.001)) + LocalIn.ar(4);
        var mod = Duty.kr(4/pat,0,Dseq([2,1.5,4,1,1.3333333],inf));
        var mod2 = Duty.kr(2/pat,0,Dseq([2,1.5,4,1,1.3333333],inf));
        var base = [60,48,36,72].midicps * mod / mod2;
        sig = BPF.ar(sig,base,LFNoise2.kr(pat).range(0.02,0.5));
        sig = DelayN.ar(sig,1/base,1/base+SinOsc.ar(1/16/[3,4,5,6]).range(-0.0001,0.0001));
        LocalOut.ar(Normalizer.ar(sig)/4.051);
        sig = sig * (LFTri.ar(pat)**2);
        Splay.ar(sig,0.95,0.5);
    };
    ~karplus.play;
    ~karplus[10] = \filter -> {|sig| LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),120),[4004,4014]),LFNoise2.kr(1/4!2).range(3000,15000))+sig;};
);
~karplus.stop(4);
~karplus.clear;
~karplus.pause;
~karplus.resume;



// moog synth , DANEGEROUS LARGE offset ///////////////////////////////////////

(
    ~moog.ar(2);
    ~moog.quant=[2,1];
    ~moog.fadeTime=16.2;
    ~moog={
        var pat = 1/64/[1,2,3,16]*~tempo.kr;
        //var r = RandSeed.kr(pat/64,2016/[1,2,3,4]);
        var harm = TChoose.kr(Impulse.kr(pat),[1,2,4,1.5,1,0.5,1.3333333]);
        var f =  ([60,63]).midicps / 4 * harm / {TChoose.kr(Impulse.kr(pat),[1,2,4,1.5,1.33333,1/2,1/4])}!4 + Mix.kr(LFSaw.kr((1..16)));
        var sig = SinOsc.ar(f,mul:[0.7,0.4,0.2]) * (LFTri.ar(pat,mul:0.9)**3);
        sig = CombN.ar(sig,1/f,1/f*2,0.5)/2;
        sig = MoogFF.ar(sig,f*[2,4,1.5,10,20,15]*harm,LFNoise2.kr(1/16!3).range(0.5,3.5));
        sig = CompanderD.ar(sig,3/4,1/1.002,1/10);
        sig = Limiter.ar(sig);
        //sig = LeakDC.ar(sig);
        Splay.ar(sig,0.75,0.15);

    };
    ~moog.play;
    ~moog[10] = \filter -> {|sig| LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),80),[4004,4014]),LFNoise2.kr(1/4!2).range(2000,15000))+sig;};
);
~moog.stop(16);
~moog.clear;
~moog.pause;
~moog.resume;



// saw synth ///////////////////////////////////////

(
    ~saw.ar(2);
    ~saw.quant=[2,1/2];
    ~saw.fadeTime=8.2;
    ~saw={
        var mod = Duty.kr(4*~tempo.kr,0,Dseq([1,2,1.5,1.33333],inf));
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,[4,16,64]*4,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[[3.602,4,16],0.15],[3,-3],curve:'exp'),imp * hit);
        var sig = [] ++ LFSaw.ar([36.midicps,72.midicps*8/mod],mul:[1,0.1,0.05]) ++ (SinOsc.ar(84.midicps*16,mul:0.1) * Pulse.ar(8*~tempo.kr));
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

// slowmo, player synth ///////////////////////////////////////
b = Buffer.read(s,Platform.resourceDir ++ "/sounds/mono/beat.wav");

(
    ~smp.ar(2);
    ~smp.quant=[2,1/2];
    ~smp.fadeTime=2.02;
    ~smp={
        var mod = 1/4/[1,1.5,1.3333,2];
        var spread = 0.0;
        var pos = [0,0.5,0.25,0.6] + LFSaw.ar(1/128*~tempo.kr).round(1/128);
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(3);
        var imp = PulseDivider.kr(~tick.kr,[8,4,2,1]*4,~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,6.05],[10,3],curve:'exp'),imp * hit);
        var sig = PlayBuf.ar(b.numChannels,b.bufnum,BufRateScale.kr(b.bufnum)*mod+(env**0.01/100),imp, (pos) * BufFrames.kr(b.bufnum),2);

        sig = sig * env;
        sig = Vibrato.ar(sig,[4,5],0.02);
        Splay.ar(sig.flat,0.5,0.75);
    };
    ~smp.play;
    ~smp[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),300),[4003,4013]),1500+LFSaw.kr(1/32!2).range(0,4000))/4+sig;};
);

~smp.stop(7);
~smp.clear;


// first perlin noisy synth ///////////////////////////////////////
(
    ~one_weird.ar(2);
    ~one_weird.quant=[2,1/2];
    ~one_weird.fadeTime=8;
    ~one_weird={
        var t = LFTri.ar([1/64,50,1/32/(1..16)]) * ((1..16)*10);
        var f = Perlin3.ar(t[0],t[1],t[2]);
        var sig = Formant.ar(f.range(29.midicps,29.midicps*100/[1,2,1.5,4]),f.range(29.midicps,29.midicps*100).scramble,f.range(29.midicps,29.midicps*100).reverse,mul:LFTri.kr(1/(1..16))**4 );
        sig = sig ** 0.05;
        sig = CompanderD.ar(BPF.ar(sig,60.midicps*10/8*(1..8),0.5),3/4,1/LFNoise2.kr(1/4!8).range(1,4),1/2);

        Splay.ar(sig,0.95,0.95);
    };

    ~one_weird_env.ar(2);
    ~one_weird_env.quant=[2];
    ~one_weird_env.fadeTime=0.2;
    ~one_weird_env={
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,16,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[4,0.005],[5,30],curve:'exp'),imp * hit);
        //var env = (LFSaw.ar(1/4*~tempo.kr)**16);
        var sig = ~one_weird.ar * LFSaw.ar([20,20.01]);
        sig = HPF.ar(sig,env.exprange(14000.01,150),0.42);
        sig = sig * env;
        //sig = sig * (LFSaw.ar(1/64*~tempo.kr)**3);
        Splay.ar(sig,0.95,0.5);
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
        var mel = TChoose.kr(Impulse.kr(1/32*~tempo.kr),[1.5,1.3333,1,2,1.666666,1.875]);//Duty.kr(1/50,0,Dseq([1,2,1.5,1.33333],inf));
        var mod = SinOsc.ar(1/50,mul:0.05);
        var t = LFSaw.ar([1/16,1/4000,29.midicps/mel]) * 480 * (LFTri.ar(1/32/[1,2,3])**8);
        in = Perlin3.ar(t[0],t[1],t[2]);
        in = Normalizer.ar(in) + RLPF.ar(Pulse.ar(2*~tempo.kr),8000);
        chain = FFT(b.bufnum, in) ;
        chain = PV_MagFreeze(chain, Decay.kr(Dust.kr(0.15),0.1) );
        chain = PV_BrickWall(chain, mod);
        chain = PV_MagShift(chain, 0.5 );
        chain = IFFT(chain);

        sig = chain;

        sig = LPF.ar(sig,18000);
        sig = HPF.ar(sig,40);
        sig = AllpassN.ar(sig,1/[100,101],1/5,4);
        sig = CombL.ar(sig,1/29.midicps*mel,1/29.midicps*mel,4);
        sig = CompanderD.ar(sig,3/4,1/3,1/10,0.002);
        Splay.ar(sig,0.99,0.25);

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




// hard sin nabeh, synth ///////////////////////////////////////

(
    ~sup.ar(2);
    ~sup.quant=[2];
    ~sup.fadeTime=0.02;
    ~sup={
        //         |       |       |       |       |
        var pat = [
            0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
        ].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,4,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.000001,1,0.0001],[4.00002,0.005],[3,-3],curve:'exp'),imp * hit);
        var sig = Mix.fill(10,{|n| SinOsc.ar(29.midicps+(n/100),env**(10+(n*5))*8pi) + WhiteNoise.ar(env**4)})/2;

        sig = sig * env/4;
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

// kick synth ///////////////////////////////////////

(
    ~kick.ar(2);
    ~kick.quant=2;
    ~kick.fadeTime=0.0002;
    ~kick={
        //         |       |       |       |       |
        var pat = [
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
            1,0,0,0,1,0,0,0,1,0,0,0,1,1,0,0,
        ].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,4,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.0002,2.75],[3,-3],curve:'exp'),imp * hit);
        var sig = Mix.fill(8,{|n| SinOsc.ar(29.midicps+(env**0.5*2),env**(200+(n/200))*8pi) + BrownNoise.ar(env**46)});
        //sig= Limiter.ar(sig,0.9);
        sig = sig * env/3.9;
        sig = CompanderD.ar(sig,3/4,1,1/3,0.0002);
        //sig = Clip.ar(sig,-0.95,0.95);

        Splay.ar(sig,0.05,0.5);
    };
    ~kick.play;
);
~kick.stop(4);
~kick.clear;
~kick.resume;

// two synth ///////////////////////////////////////

(
    ~k_two.ar(2);
    ~k_two.quant=2;
    ~k_two.fadeTime=0.2;
    ~k_two={
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,1,1,1].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,[4,4,2,1],~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.00001],[0.002,2.5],[3,-3],curve:'exp'),imp * hit);
        var sig = SinOsc.ar(50+(env**40*LFNoise1.kr(1/32!4).range(50,350)),env**30*8pi);
        sig = sig * env;
        Splay.ar(sig,0.25,0.87);
    };
    ~k_two.play;
);
~k_two.stop(4);
~k_two.clear;
~k_two.pause;
~k_two.resume;



// switch synth ///////////////////////////////////////

(
    ~switch.ar(2);
    ~switch.quant=[2,1/2];
    ~switch.fadeTime=0.2;
    ~switch={
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,1,0,0].pyramid.rotate(1);
        //         |       |       |       |       |
        //      var pat = [1,0,1,0,1,0,1,1,1,0,1,0,1,0,1,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,[1,2,3,4,5,6].scramble,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.1*[1,2,3,4]],[3,30],curve:'exp'),imp * hit);
        var sig = WhiteNoise.ar(0.4!4) * Pulse.ar(1000,50,2500,10);
        sig = RHPF.ar(sig,[1000,2500,12500,15000,1000,500,12500],LFNoise2.kr(1/4!4).range(0.1,0.9));
        sig = sig * env * LFPulse.ar(1/4/(1..6)*~tempo,0,1/4);
        sig = CompanderD.ar(sig,3/4,1,1/3,0.0002);
        Splay.ar(sig,0.5,0.145);
    };
    ~switch.play;
    ~switch[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),5000),[4001,4011]),12000)+sig;};
);
~switch.play;
~switch.stop(4);
~switch.clear;


// hat synth ///////////////////////////////////////

(
    ~high.quant=[2,1/2];
    ~high.fadeTime=8.02;
    ~high={
        var imp = PulseDivider.kr(~tick.kr,[1,4],~counter.kr);
        //var imp = PulseDivider.kr(~tick.kr,[4,16]/4);
        var env = EnvGen.ar(Env([0.001,1,0.001],[0.002,[0.05,0.2]],[3,-3]),imp);
        var sig = WhiteNoise.ar(0.5!2);
        sig = HPF.ar(sig,LFNoise2.kr(1/16!4).range(1000,16000)) * LFPulse.ar(1/4/[1,4]*~tempo,0,1/4);
        sig = sig * env;
        sig = sig + (CombN.ar(sig * LFPulse.ar(1/4/[1,2,3,4],0,0.15),1/50,1/50,0.5));
        Splay.ar(sig,0.45,0.15);
    };
    ~high.play;
    ~high[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),5000),[4001,4011]),12000)+sig;};
);
~high.stop(7);
~high.clear;


// hh synth ///////////////////////////////////////

(
    ~hh3.ar(2);
    ~hh3.quant=[2,1/2];
    ~hh3.fadeTime=0.2;
    ~hh3={
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,1,0,1,0,0,0,1,0,0,0,1,1,0,0,1,0,1,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,[2,2,1,1],~counter.kr);
        var hit = Select.kr(PulseCount.kr(imp)/[1,2,3,4]%pat.size,pat);
        var env = EnvGen.ar(Env([0.01,1,0.0001],[0.005,0.25],[6,-3],curve:'exp'),imp * hit);
        var sig = LFNoise1.ar(16000/[1,4,8,16]) * Crackle.ar([0.1,0.7,0.4,0.6]+env);
        sig = sig * env;
        Splay.ar(sig,0.75,0.75);
    };
    ~hh3.play;
);
~hh3.stop(4);
~hh3.clear;
~hh3.pause;
~hh3.resume;


// pre-kick synth ///////////////////////////////////////

(
    ~kick_even.ar(2);
    ~kick_even.quant=[4,1/2];
    ~kick_even.fadeTime=0.02;
    ~kick_even={
        var mod = Duty.kr(8 * ~tempo.kr,0,Dseq([1,2,1.5,1.33333,1.875,1.666666],inf));
        //         |       |       |       |       |
        var pat = [
            1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,
        ].rotate(3);
        var imp = PulseDivider.kr(~tick.kr,8,~counter.kr%pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.05,0.75],[9,-3],curve:'exp'),imp * hit);
        var sig = SinOsc.ar((29).midicps,env**40*8pi) + PinkNoise.ar(env**2*2);
        //sig= Limiter.ar(sig,0.9);
        sig = sig * env;
        sig = CompanderD.ar(sig,3/4,1,1/3,0.0002);
        sig = Clip.ar(sig,-0.95,0.95);
        Splay.ar(sig,0.05,0.35);
    };
    ~kick_even.play;

    ~kick_even[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),30),[3000,3010]),LFNoise2.kr(1/4!2).range(1000,7000))+sig;};
);
~kick_even.stop(4);
~kick_even.clear;


b = Buffer.read(s,Platform.resourceDir ++ "/sounds/mono/violin.wav");

// player synth ///////////////////////////////////////

(
    ~violin.ar(2);
    ~violin.quant=2;
    ~violin.fadeTime=4.02;
    ~violin={
        var speed = 1/64/[1,2,3,4] * ~tempo.kr;
        var mod = 1/1.5/[1,1.3333,1.5,2]/TChoose.kr(Impulse.ar(1/16/[1,2,3,4]*~tempo.kr),[1.5,1.33335,1,0.75,1.6666666,1.875])/[1,1.5];
        var trate, dur, clk, pos, pan,sig;
        trate = 120;
        2.do{
            mod = mod / TChoose.kr(Impulse.ar(speed),[1.5,1.33335,1,0.5]);
        };
        dur = BufDur.kr(b) / trate;
        clk = Impulse.kr(trate);
        pos = LFSaw.ar(1/4000/[1,2,4,8]).range(0,BufDur.kr(b)) + TRand.kr(0, 0.0001, clk);
        pan = WhiteNoise.kr(0.6);
        sig = TGrains.ar(2, clk, b.bufnum, mod, pos, dur, pan, 0.3);
        sig = sig * (LFTri.ar(speed)**8);
        Splay.ar(sig.flat,0.95,0.45);
    };
    ~violin.play;
    ~violin[10] = \filter -> {|sig|LPF.ar(DWGReverb.ar(HPF.ar(NumChannels.ar(sig,2),300),[3000,3010]),LFNoise2.kr(1/4!2).range(1000,7000));};
);
~violin.stop(10);
~violin.clear;


// sw synth ///////////////////////////////////////

(
    ~sw.ar(2);
    ~sw.quant=[2,1/2];
    ~sw.fadeTime=8.2;
    ~sw={
        var base = [36].midicps / Duty.kr(4*~tempo,0,Dseq([1,2,1.5,1.33333],inf));
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,(1..8)*4,~counter.kr % pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.005,2.75],[3,-3],curve:'exp'),imp * hit);
        var sig = [
            SinOsc.ar(base!2),
            SinOsc.ar(SinOsc.ar(base/[1,2,3,4]).range(24.midicps,36.midicps*100/[1,1.3333,2,1.5]),mul:0.1),
            Pulse.ar(base),
            LFSaw.ar(base*10/[1,2,1.33333,1.5],mul:0.1),
            Formant.ar(base*10,base*10/[1,1.333,1.5,2],base*100/[1,2,4,8]),
            LFNoise2.ar(base*100!2),
            SinOsc.ar(base*10/[1,2,1.33333,1.5],mul:0.1),
            Pulse.ar(4),
            LFNoise2.ar(base*1000/[1,2,4,8]),
            VarSaw.ar(base!2),
            LFSaw.ar(base*100/[1,2,1.3333,1.5],mul:0.1)
        ];
        sig = SelectX.ar(LFSaw.ar(1/8/(1..8)*~tempo.kr)*sig.size,sig);
        sig = sig * (LFTri.ar(1/4/(1..8)*~tempo.kr)**4);
        sig = CombN.ar(sig,1/base,2/base,4);
        sig = CompanderD.ar(sig,3/4,1/1.5,1/2);
        sig = sig * env;
        Splay.ar(sig,0.15,0.75);
    };
    ~sw.play;
    ~sw[10] = \filter -> {|sig|DWGReverb.ar(HPF.ar(LPF.ar(NumChannels.ar(sig,2),LFNoise2.kr(1/4!2).range(5000,13000)),300),[3000,3010])/4+sig;};

);
~sw.stop(4);
~sw.clear;
~sw.pause;
~sw.resume;






(
    ~sell.quant=2;
    ~sell={LFTri.kr(1/64*~tempo)**4};
)


// scanner synth ///////////////////////////////////////

(
    ~scanner.ar(2);
    ~scanner.quant=2;
    ~scanner.fadeTime=0.2;
    ~scanner={
        //         |       |       |       |       |
        var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0].rotate(1);
        var imp = PulseDivider.kr(~tick.kr,4,~counter.kr % pat.size);
        var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
        var env = EnvGen.ar(Env([0.001,1,0.0001],[0.005,0.25],[3,-3],curve:'exp'),imp * hit);
        var sig = [
            SinOsc.ar(50),
            LFNoise2.ar(1500),
            Pulse.ar(2),
            LFNoise1.ar(16000),
            LFSaw.ar(500!2,mul:0.1),
            LFNoise2.ar(1500),
            Pulse.ar(4),
            Blip.ar(50,3);
            WhiteNoise.ar(1),
            SinOsc.ar(12500),
            LFSaw.ar(75),
            SinOsc.ar(50),
        ];
        sig = SelectX.ar(~sell*sig.size,sig);
        sig = Resonz.ar(sig,LFNoise2.kr(1/4/[1,2,3,4]).exprange(100.01,16000),0.2,mul:2)+sig/2;
        sig = CombN.ar(sig,1/50,1/50+(~sell.kr/20),1)+sig;
        sig = CompanderD.ar(sig,3/4,1/3,1/2);
        sig = Limiter.ar(sig,0.95)/2;
        sig = LPF.ar(GVerb.ar(sig,90,3)/2,2000)+sig/2;
        Splay.ar(sig,0.95,0.125,SinOsc.kr(4.1));
    };
    ~scanner.play;
    ~scanner[10] = \filter -> {|sig|DWGReverb.ar(HPF.ar(LPF.ar(NumChannels.ar(sig,2),LFNoise2.kr(1/4!2).range(5000,13000)),300),[3000,3010])/4+sig;};
);
~scanner.stop(4);
~scanner.clear;
~scanner.pause;
~scanner.resume;



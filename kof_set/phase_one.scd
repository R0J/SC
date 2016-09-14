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

 


// oo synth ///////////////////////////////////////
(
~one_weird.ar(2);
~one_weird.quant=2;
~one_weird.fadeTime=4;
~one_weird={
  var t = LFTri.ar([1/64,50,1/32/(1..16)]) * (1..8);
  var f = Perlin3.ar(t[0],t[1],t[2]);
  var sig = Formant.ar(f.range(50,5000/[1,2,1.5,4]),f.range(50,500).scramble,f.range(50,500).reverse,mul:LFTri.kr(1/(1..64))**4 );
  sig = CompanderD.ar(BPF.ar(sig,1500/8*(1..8),0.5),3/4,1/LFNoise2.kr(1/4!8).range(1,3),1/2);
  Splay.ar(sig,0.95,0.25);
};
~one_weird.play;
);
~one_weird.stop(4);
~one_weird.clear;
~one_weird.pause;
~one_weird.resume;




// player synth ///////////////////////////////////////

(
  b = Buffer.read(s,Platform.resourceDir ++ "/sounds/mono/violin.wav");
  ~one_violin.ar(2);
  ~one_violin.quant=2;
  ~one_violin.fadeTime=8.02;
  ~one_violin={
    var mod = 1/[2,1]/[1,1.3333,1.5,2]/TChoose.kr(Impulse.ar(1/16/[1,2,3,4]),[1.5,1.33335,1,0.75])/1.5;
    var trate, dur, clk, pos, pan,sig;
    trate = 120;
    2.do{
    mod = mod / TChoose.kr(Impulse.ar(1/16/[1,2,3,4]),[1.5,1.33335,1,0.5]);
  };
    dur = BufDur.kr(b) / trate;
    clk = Impulse.kr(trate);
    pos = LFSaw.ar(1/200/[1,2,4,8]).range(0,BufDur.kr(b)) + TRand.kr(0, 0.001, clk);
    pan = WhiteNoise.kr(0.6);
    sig = TGrains.ar(2, clk, b.bufnum, mod, pos, dur, pan, 0.3);
    sig = sig * (LFTri.ar(1/16/[1,2,3,4])**4);
    //sig = sig * Pulse.ar(1/4/[1,2,3,4]);
    //sig = Resonz.ar(sig,[468,1168]/TChoose.kr(Impulse.kr(8!4),[1,2,1.5,1.3333]),0.02);
    sig = CompanderD.ar(sig,3/4,1/2,1/2) ;//** LFNoise2.kr(1/4!3).range(0.5,1.99);
    Splay.ar(sig.flat,0.75,0.05);
  };
  ~one_violin.play;
);
~one_violin.stop(50);
~one_violin.clear;



// one synth ///////////////////////////////////////

(
  ~one_puls.ar(2);
  ~one_puls.quant=2;
  ~one_puls.fadeTime=8;
  ~one_puls={
    var t = LFTri.ar(1/4/[1,2,3]) * 2000 ;
    var perl = Perlin3.ar(t[0],t[1],t[2]);
    var puls = Pulse.ar(LFNoise0.ar(4).round(1/8)*[4,2,1,1/4]*~tempo.kr);
    var sig = perl * puls;
    sig = HPF.ar(sig,12000/[100,50,10,1]);
    //sig = CompanderD.ar(sig,1/2,1,1/10);
    Splay.ar(sig,0.75,0.0595);
  };
  ~one_puls.play;
);
~one_puls.stop(4);
~one_puls.clear;
~one_puls.pause;
~one_puls.resume;

// noise synth ///////////////////////////////////////

(
~one_noise.ar(2);
~one_noise.quant=2;
~one_noise.fadeTime=16.2;
~one_noise={
  //         |       |       |       |       |  
  var pat = [1,0,0,0,1,0,0,0,1,0,0,0,1,0,1,0].rotate(3);
  var imp = PulseDivider.kr(~tick.kr,[1,2,3,4],~counter.kr % pat.size);
  var hit = Select.kr(PulseCount.kr(imp)%pat.size,pat);
  var env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,0.55],[3,-3],curve:'exp'),imp * hit);
  var sig = LFNoise0.ar(17000/[1,2,4,10]) * (LFTri.ar(1/16/[1,2,3,4]) ** 4);
  //sig = sig * env;
  Splay.ar(sig,0.75,0.055);
};
~one_noise.play;
);
~one_noise.stop(4);
~one_noise.clear;
~one_noise.pause;
~one_noise.resume;


// ff filter ///////////////////////////////////////

(
~ff.quant=1;
~ff.fadeTime=4;
~ff={
  var synths = p.playingProxies;
  var high,mid,low;
  
  var mod2 = Duty.kr([4,2]*8,0,Dseq([1,2,1.5,1.3333],inf)).lag(1/4);
  var mod = Duty.kr([4,2],0,Dseq([1,2,1.5,1.3333],inf)).lag(1/40);
  
  var sig = [] ++ ~one_violin.ar ++ ~one_puls.ar ++ ~one_noise.ar ++ ~one_weird.ar;

  sig = CombC.ar(sig,1/50/mod2,1/50/mod2*mod,1.5) * (LFSaw.ar(1/4/[1,2]).range(0.01,1)**1.9) +sig;
  low = CompanderD.ar(LPF.ar(sig,LFNoise2.kr(1/2!2).range(100,1800)),3/4,1/1.5,1/2);
  mid = CompanderD.ar(BPF.ar(sig,LFNoise2.kr(1/2!2).range(700,5500),0.5),3/4,1,1/2);
  high = CompanderD.ar(HPF.ar(sig,LFNoise2.kr(1/2!2).range(4000,10000)),3/4,1/1.5,1/2);
  sig = CompanderD.ar((high+mid+low).flat,3/4,1/1.5,1/2,0.002,0.3);
  sig = LPF.ar(DWGReverb.ar(NumChannels.ar(sig,2),[4000,4010]),2000)/8+sig;
  Splay.ar(sig,0.75,0.35);
};
~ff.play;
);
~ff.stop(7);
~ff.clear;

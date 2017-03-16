(
ProxySynthDef(\string, {
	arg  sustain,  amp, pan, feedback=0.2, freq;
	var sig, env, renv, senv, local;

	env=EnvGen.kr(Env([0,1,0],[1,0.1],[\welch,\lin]),timeScale:sustain,doneAction:2);
	renv=EnvGen.kr(Env([0.01,5,0.01],[0.8,0.3],[\welch,\lin]),timeScale:sustain,doneAction:2);
	feedback=EnvGen.kr(Env([0.3,0.1,0.3]+feedback,[0.9,0.3],[\welch,\lin]),timeScale:sustain,doneAction:2);

	sig=CombL.ar(WhiteNoise.ar(0.5!2),freq.reciprocal,freq.reciprocal,10)*env;
	local=LocalIn.ar(2)+sig;
	sig=DelayN.ar(local,renv, renv);
	LocalOut.ar(sig*feedback);
	sig=MoogFF.ar(sig, freq,2,mul:0.2)+sig;
	sig=Pan2.ar(sig,pan)*env*amp;

}).add;


ProxySynthDef(\noisek, {
	arg  sustain,  amp, pan, freq, panpos=1;
	var sig, env, feedback=0.7, local, feedenv, renv;

	env=EnvGen.kr(Env([0,1,0],[2,0.01],[\welch,\exp]),timeScale:sustain,doneAction:2);
	renv=EnvGen.kr(Env([0.01,5,0.01],[0.8,0.3],[\welch,\lin]),timeScale:sustain,doneAction:2);
	feedenv=EnvGen.kr(Env(levels:[0.1,0.01,0.1],times:[0.1,0.01]),timeScale:sustain,doneAction:2);

	sig=WhiteNoise.ar(LFNoise0.kr(10).range(0.1,0.8))*env;
	local=LocalIn.ar(2)+sig.dup;
	local=AllpassN.ar(local,0.4, feedenv,SinOsc.kr(1/16).range(0.5,4));
	LocalOut.ar(local*feedback);
	local=LPF.ar(sig+local,800,mul:1);
	local=BPF.ar(Limiter.ar(local), freq,rq:0.01)+(local*0.2)*10;
	local=Pan2.ar(Mix.new(local),SinOsc.kr(panpos));
}).add;


ProxySynthDef(\bow, {
	arg  sustain,  amp, pan, freq;
	var sig, env;
	env=EnvGen.kr(Env([0,1,0],[1,0.001],\welch),timeScale:sustain,doneAction:2);
	sig=DWGBowed.ar(freq+(freq*fib(5).reciprocal));
	sig=DWGSoundBoard.ar(sig,c1:10,c3:20,mix:0.5);
	sig=Rotate2.ar(Mix.new(sig),LPF.ar(Mix.new(sig),600),SinOsc.kr(0.25));
	sig=sig*env*amp;

}).add;


ProxySynthDef(\var, {
	arg  sustain,  amp, dec, freq;
	var sig, env;
	env=EnvGen.kr(Env([0,1,0],[0.4,0.001],\welch),timeScale:sustain,doneAction:2);
	sig=Clip.ar(Wrap.ar(VarSaw.ar([1,8])*LFSaw.ar(freq),-0.7,0.7),-0.5,0.5);
	sig=sig*env*amp;

}).add;


ProxySynthDef(\cheap, {
	arg  sustain,  amp, dec, freq;
	var sig, env;
	env=EnvGen.kr(Env([0,1,0],[0.4,0.001],\welch),timeScale:sustain,doneAction:2);
	sig=SinOsc.ar(freq+[0,2]);
	sig=CheapVerb.ar(sig,decayTime:dec);
	sig=sig*env*amp;

}).add;


ProxySynthDef(\onion, {
	arg sustain, gate=1, amp, freq, f2, fsaw;
	var sig, env, fenv, penv, nenv;

	env=EnvGen.kr(Env([0,1,0.2,1,0],[0.1,0.2,0.6,0.1],\welch),gate,timeScale:sustain,doneAction:2);
	fenv=EnvGen.kr(Env([freq*4, freq],[sustain*0.04],\welch),gate);
	penv=EnvGen.kr(Env([0,-1,1,0,-1,1],[0.2,0.05,0.5,0.2,0.05]),gate,timeScale:sustain);
	nenv=EnvGen.kr(Env([0,0,1,0,0,1,0],[0.2,0.05,0.05,0.6,0.05,0.05]),gate,timeScale:sustain);

	sig=Mix.fill(15, {arg i;
		SinOsc.ar(SinOsc.kr(LFNoise0.kr(500).range(fenv,fenv+i)).range(fenv,fenv+i),[0,i,pi],add:GrayNoise.ar(nenv))})*SinOsc.ar(fsaw);
	sig=Pan2.ar(Limiter.ar(sig+WhiteNoise.ar(0.1),1)*amp,penv);

}).add;


ProxySynthDef(\imp, {
	arg  sustain,  amp, pan, den,hpf;
	var sig, env, fenv;
	env=EnvGen.kr(Env([0,1,0],[0.2,0.001],\welch),timeScale:sustain,doneAction:2);

	sig=Impulse.ar(den).lag([0.002,0.02,0.03,0.003]);
	sig=Pan2.ar(sig, SinOsc.kr(0.5));
	sig=sig*env*amp;

}).add;

ProxySynthDef(\zum, {
	arg  sustain,  amp, pan;
	var sig,sig1,sig2, env, fenv, renv;

	env=EnvGen.kr(Env([0,1,0],[0.001,0.2],\welch),timeScale:sustain,doneAction:2);
	fenv=EnvGen.kr(Env([\freq.kr, \freq.kr*4, \freq.kr],[0.01,0.12]),timeScale:sustain);
	renv=EnvGen.kr(Env([1,0.1,1],[0.001,0.8],\welch),timeScale:sustain,doneAction:2);

	sig1=LFPulse.ar(fenv);
	sig2=BPF.ar(WhiteNoise.ar(1),rq:renv);
	sig=sig1+sig2;
	sig=Pan2.ar(sig, pan);
	sig=sig*env*amp;

}).add;

ProxySynthDef(\tss, {
	arg  sustain,  amp, pan, hpf;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,1,0],[0.01,0.1,0.01]),timeScale:sustain,doneAction:2);
	fenv=EnvGen.kr(Env([\freq.kr,\freq.kr*4,\freq.kr],[0.01,0.2],\welch),timeScale:sustain,doneAction:2);
	sig=HPF.ar(Clip.ar(WhiteNoise.ar(1),0.6,1)-0.6,hpf+fenv);
	sig=AllpassN.ar(sig,0.04,0.02);
	sig=Pan2.ar(Splay.ar(sig),pan);
	sig=sig*env*amp;
}).add;

ProxySynthDef(\tss808, {
	arg  sustain,freq=230, hpf=6500,amp, pan=0,release=0.15,  fxb=0, fxv=0, bbcb=0, bbcv=0;
	var pulse, sig, env, freqs;
	freqs = [freq, freq*1.4471, freq*1.617, freq*1.9265, freq*2.5028, freq*2.6637];
	pulse = Mix.ar(Pulse.ar(freqs, {0.9.rand}!6, mul: 0.15));
	sig = RHPF.ar(RHPF.ar(pulse, hpf), hpf);
	env = EnvGen.kr(Env.perc(0,release),timeScale:sustain,doneAction:2);
	sig = Pan2.ar(Mix.new(sig*env*amp),pan);
}).add;


ProxySynthDef(\ju, {
	arg sustain, amp, f2;
	var sig, fenv,fenv2, env;

	env=EnvGen.kr(Env(),timeScale:sustain,doneAction:2);
	fenv= EnvGen.kr(Env([\freq.kr, \freq.kr*4],[sustain*0.5]),timeScale:sustain);
	fenv2= EnvGen.kr(Env([f2*2, f2],[sustain*0.7]),timeScale:sustain);

	sig=BPF.ar(WhiteNoise.ar(amp)+BrownNoise.ar(amp),fenv,rq:SinOsc.kr(fenv2).range(0.001,1));
	sig=FreeVerb.ar(sig,room:2,damp:0.1,add:sig);
	sig=Pan2.ar(sig*env,SinOsc.kr(fenv));
	sig=FreeVerb.ar(sig,room:2);

	sig=sig*env*amp;

}).add;


ProxySynthDef(\zit, {
	arg  sustain,  amp, pan, hpf, noise;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,0],[0.1,0.3]),timeScale:sustain,doneAction:2);
	sig=LFSaw.ar(\freq.kr+fib(10).reciprocal)+GrayNoise.ar(noise);
	sig=Clip.ar(Splay.ar(sig),0.5,1);
	sig=Pan2.ar(HPF.ar(sig,hpf), pan);
	sig=sig*env*amp;

}).add;


ProxySynthDef(\noiserand, {
	arg  sustain,  amp, pan, hpf, noise;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,0],[3,0.01],[\lin, \exp]),timeScale:sustain,doneAction:2);

	sig=(WhiteNoise.ar(LFNoise0.kr(3).range(0.1,0.6))*LFSaw.kr(LFNoise0.kr(20).range(0.5,4)))+GrayNoise.ar(LFNoise0.kr(5).range(0.1,0.8));
	sig=sig+BPF.ar(GrayNoise.ar(1!2),\freq.kr,LFNoise0.kr(0.5).range(0.2,2));
	sig=FreeVerb.ar(sig,LFNoise0.kr(20).range(0.3,1),LFNoise0.kr(3).range(0.5,2),LFNoise0.kr(3).range(0.2,1),0.2,add:sig);
	sig=Clip.ar(Splay.ar(sig),-1,1);
	sig=Pan2.ar(sig, SinOsc.kr(0.25));
	sig=sig*env*amp;

}).add;


ProxySynthDef(\rrr2, {
	arg  sustain,  amp, pan, lag;
	var sig, env, lenv, fenv;

	lenv=EnvGen.kr(Env([lag, lag+0.02, lag+0.05],[0.01,4]),timeScale:sustain);
	env=EnvGen.kr(Env([0,1,0],[0.5,2]),timeScale:sustain,doneAction:2);
	fenv=EnvGen.kr(Env([4,6,2,3]/5*\freq.kr,[4,[0.2,5,1],1].convertRhythm),timeScale:sustain,doneAction:2);

	sig=LFSaw.ar([fenv, fenv+[1,2,3,5,8].reciprocal]).lag([0.01,0.02]);
	sig=CompanderD.ar(Pan2.ar(sig,SinOsc.ar(0.25/2)));
	sig=Limiter.ar(sig,0.5);
	sig=sig*env*amp;

}).add;



ProxySynthDef(\oto, {
	arg  sustain,  amp, dist=1200,lpfmul=0.5;
	var sig, env, env2;

	env=EnvGen.kr(Env([0,1,0],[4,0.02]),timeScale:sustain,doneAction:2);
	env2= EnvGen.kr( Env([0,1,0],[0.05,1]),timeScale: sustain/2, doneAction: 2 );
	sig = RLPF.ar( LFSaw.ar( \freq.kr, mul: env2), LFSaw.kr(0.015,mul: 36,add: 55).midicps, 0.2 ).lag(0.02);
	sig = AllpassN.ar(sig, [0.05, 0.05, 0.08], [0.05.rand, 0.05.rand], 4);
	sig = LPF.ar(sig, [200, 400, 600, 100], lpfmul);
	sig = tanh(sig * dist);
	sig=sig*env*amp;

}).add;



ProxySynthDef(\noiselong, {
	arg  sustain,  amp, pan;
	var sig, env;

	env=EnvGen.kr(Env([0,1,0],[1,0.01]),timeScale:sustain,doneAction:2);
	sig=DFM1.ar(WhiteNoise.ar(0.7),\freq.kr,fib(10),type:0);
	sig=Pan2.ar(sig,SinOsc.kr(pan));
	sig=sig*env*amp;

}).add;

ProxySynthDef(\blow, {
	arg  sustain,  amp, pan;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,0],[0.1,4]),timeScale:sustain,doneAction:2);
	sig=WhiteNoise.ar(LFNoise1.kr(10).range(0.01,0.1))+PinkNoise.ar(LFNoise2.kr(4).range(0.01,0.1));
	sig=BPF.ar(sig,[\freq.kr.lag(0.2),\freq.kr.lag(0.02),\freq.kr.lag(2)]+fib(10),LFNoise2.kr(200).range(0.001,0.01),5);
	sig=Limiter.ar(sig*env*amp,0.8);

}).add;


ProxySynthDef(\mel, {
	arg  sustain,  amp;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,0],[0.01,0.5]),timeScale:sustain,doneAction:2);
	sig=LFSaw.ar([\freq.kr,\freq.kr.lag(0.2),\freq.kr.lag(0.4)]).lag([0.02,0.04]);
	sig=sig*LFPulse.ar([13,15]);
	sig=LPF.ar(sig,1800);
	sig=sig*env*amp;

}).add;


ProxySynthDef(\kickfill,{
	arg  sustain,  amp, pan=0, top=40;
	var sig, env, fenv;

	env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,2.5],[3,-3],curve:'exp'),timeScale:sustain, doneAction:2);

	sig = Mix.fill(10,{|n| SinOsc.ar(\freq.kr,env**(n+10)*8pi)});
	sig = Pan2.ar(sig * env*amp,pan);
	Splay.ar(sig,0.75,0.25);
}).add;

ProxySynthDef(\kick,{
	arg  sustain,  amp, pan=0, top=40;
	var sig, env, fenv;

	env = EnvGen.ar(Env([0.001,1,0.0001],[0.002,2.5],[3,-3],curve:'exp'),timeScale:sustain, doneAction:2);
	sig = SinOsc.ar(\freq.kr,env**top*8pi);
	sig = Pan2.ar(sig * env*amp,pan);
	Splay.ar(sig,0.75,0.25);
}).add;



ProxySynthDef(\aural, {
	arg  sustain,  amp;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,0],[1,6]),timeScale:sustain,doneAction:2);
	sig=SinOsc.ar(\freq.kr+LFNoise0.ar([100,160]).range(2,40).round(2));
	sig=sig+BPF.ar(PinkNoise.ar([0.4,0.6]),\freq.kr,0.1);
	sig=sig*env*amp;

}).add;


//LFPulse.ar(fenv,0,0.5,add:-0.5)+

ProxySynthDef(\tum, {
	arg  sustain,  amp, pan, rq;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0.5,1,0.5,0],[0.001,0.06,0.26],[-4,-2,-4]),timeScale:sustain,doneAction:2);
	fenv= EnvGen.kr(Env([\freq.kr,\freq.kr*\top.kr(4), \freq.kr],[0.005,0.026]));
	sig=SinOsc.ar(fenv,env**20*8pi);
	sig=sig+BPF.ar(GrayNoise.ar(1!2),fenv,rq);

	sig=Pan2.ar(Splay.ar(sig)*env*amp, pan);

}).add;


ProxySynthDef(\bum, {
	arg  sustain,  amp, noise;
	var sig, env, fenv;

	env=EnvGen.kr(Env([0,1,0],[0.01,[0.2,0.6]]),timeScale:sustain,doneAction:2);
	fenv= EnvGen.kr(Env([\freq.kr,\freq.kr*4, \freq.kr],[0.001,0.05]));
	sig=SinOsc.ar(fenv!2+fib(10).reciprocal)+HPF.ar(GrayNoise.ar(noise!2),200);
	sig=FreeVerb.ar(sig,mix:0.3,room:1,damp:1);
	sig=Splay.ar(sig)*env*amp;

}).add;

ProxySynthDef(\drum, {
	arg  sustain,  amp, rq;
	var sig, env,  fenv;

	env=EnvGen.kr(Env.perc(0.01,0.2),timeScale:sustain,doneAction:2);
	fenv= EnvGen.kr(Env([\freq.kr,\freq.kr*5, \freq.kr],[0.01,0.5]));
	sig=BPF.ar(GrayNoise.ar(0.8)+BrownNoise.ar(1),fenv+fib(5).reciprocal,rq)*20;
	sig=Clip.ar(sig*env*amp,-0.5,0.6);

}).add;



ProxySynthDef(\voice, {
	arg  sustain, amp;
	var sig,env;

	env=EnvGen.kr(Env.perc(2,4),timeScale:sustain,doneAction:2);
	sig=Gendy4.ar(1,1,1,1,\freq.kr,\freq.kr+100,0.01,0.05,10);
	sig=Pan2.ar(sig,SinOsc.kr(0.125));
	sig=sig*env*amp;

}).add;




ProxySynthDef(\zzz, {
	arg sustain, amp;
	var sig, env;
	env=EnvGen.kr(Env([0,1,0],[0.1,6], [\welch,\welch]),timeScale:sustain,doneAction:2);
	sig=CombL.ar(GrayNoise.ar(0.5),1/\freq.kr,1/(\freq.kr-10),4);
	sig=OnePole.ar(sig,0.2)+FreeVerb.ar(sig,room:0.2);
	sig=Limiter.ar(CompanderD.ar(Lag.ar([sig, DelayL.ar(sig,0.4,0.02)],0.002)),0.5);
	sig=sig*env*amp;
}).add;

)
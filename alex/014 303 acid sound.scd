(ProxySynthDef( "sc303" , {  arg  freq=440, wave=0, ctf=100, res=0.2,
		sus=0, dec=1.0, env=1000, gate=0, amp=0.2, sustain=1;
	 var  filEnv, volEnv, waves;

	volEnv =  EnvGen.ar( Env.new([10e-10, 1, 1, 10e-10], [0.01, sus, dec],  'exp' ),timeScale:sustain, doneAction:2);
	filEnv =  EnvGen .ar( Env.new([10e-10, 1, 10e-10], [0.01, dec],  'exp'),timeScale:sustain,doneAction:2);

	waves = [Saw.ar(freq, volEnv), Pulse.ar(freq, 0.5, volEnv)];
	 RLPF.ar(Select.ar(wave, waves), ctf + (filEnv * env), res).dup * amp;
}).send(s);)

p=ProxySpace.push(s);
(
~test[0]=Pbind(\instrument, \sc303,\dur, 0.2, \amp,Pseq([0.2,0.5],inf), \sustain,0.2, \wave, [0,1], \octave, Prand([5,6],inf)-1, \degree, [4,8,8,6,2], \strum, 0.2);
~test[30]=\filter->{arg in; GVerb.ar(in,40,0)};
~test.play;
~test.clear;
~test[0]=nil;
)
8*0.25
(
~test1[0]=Pbind(\instrument, \sc303,\dur,0.25, \sus, 0, \amp,0.04, \sustain, 0.4, \wave, 0,
	\freq, Prand([Pn(440,32),Pn(640,64)],inf)+Pseq([10,20,40,60,120,20,40,180],inf)
);)
~test1[30]=\filter->{arg in; GVerb.ar(in,40,6,mul:SinOsc.kr(1/64).range(0.05,0.4),add:in)};
~test1.play;
~test1.clear;
~test1[30]=nil;

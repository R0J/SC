(ProxySynthDef( "sc303" , {  arg  freq=440, wave=0, ctf=100, res=0.2,
		sus=0, dec=1.0, env=1000, gate=0, amp=0.2;
	 var  filEnv, volEnv, waves;

	volEnv =  EnvGen.ar( Env.new([10e-10, 1, 1, 10e-10], [0.01, sus, dec],  'exp' ), doneAction:2);
	filEnv =  EnvGen .ar( Env.new([10e-10, 1, 10e-10], [0.01, dec],  'exp'),doneAction:2);

	waves = [Saw.ar(freq, volEnv), Pulse.ar(freq, 0.5, volEnv)];
	 RLPF.ar(Select.ar(wave, waves), ctf + (filEnv * env), res).dup * amp;
}).send(s);)

p=ProxySpace.push(s);
(
~test[0]=Pbind(\instrument, \sc303,\dur, 0.2, \amp,Pseq([0.2,0.5],inf), \wave, [0,1], \octave, Prand([5,6],inf)-1, \degree, [4,8,8,6,2], \strum, 0.2);
~test[30]=\filter->{arg in; GVerb.ar(in,40,0)};
~test.play;
~test.clear;
~test[30]=nil;
)

(
~test1[0]=Pbind(\instrument, \sc303,\dur,1, \sus, 0, \amp,0.8, \wave, 0, \freq, 45);
~test1[30]=\filter->{arg in; GVerb.ar(in,40,0)};
~test1.play;
~test1.clear;
~test1[30]=nil;
)
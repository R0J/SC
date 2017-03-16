///////////////////////////////
///////////////////////////////
///////////////////////////////
/////////DRONES

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////string bow alternate synth

~combdrone.clear;
~combdrone.stop(freeTime:10);
(
~combdrone.play(vol:0.0051, fadeTime:4, numChannels:2);
~combdrone.quant=16;)
(~combdrone[0]=Pbind(
	\instrument, \string,
	\amp, 0.5,
	\legato, Prand([0.8,0.7,0.6],inf),
	\strum, 0.25,
	\pan, 0,
	\octave, Prand([3,[3,4],4,5],inf)+1,
	\degree,Pseq([2,4,[6,2],4],inf),
	\dur,Prand([8,Rest(8),16],inf),
);
~combdrone[52]=\filter->{arg in; CombC.ar(in, 16,[4,2],16)};
~combdrone[53]=\filter->{arg in; HPF.ar(in,40)};
~combdrone[54]=\filter->{arg in; LPF.ar(in,300)};
~combdrone[55]=\set->Pbind(\args, [\lpfc], \dur, 8, \lpfc, Pseq([280,420,400,440,480],inf));
~combdrone[56]=\filter->{arg in; Limiter.ar(GVerb.ar(Mix.new(in),[2,40,6],[16,8,12],mul:0.1)+in.softclip,0.4)};
)
~combdrone[56]=nil;
~combdrone[59]=\filter->{arg in; Compander.ar(in, ~tum01.ar, 0.1, 1, 0.1) };



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////aural

~aural1.clear;
~aural1.stop(fadeTime:6);
(
~aural1.play(vol:0.02, fadeTime:4, numChannels:2);
~aural1.quant=16;)
(~aural1[0]=Pbind(
	\instrument, \aural,
	\amp,Prand([0.8,0.4,0.6],inf)/2,
	\legato, 0.5,
	\octave, Pseq([[5,6],[4,3],[5,4],[3,6]],inf)+1,
	\degree, Pseq([[1,5],[4,6],[6,2],[2,4],[2,6],[8,2]],inf),
	\dur,4,// Pseq([Pn(8,2),Pn(16,2)],inf),
);
~aural1[5]=\filter->{arg in; GVerb.ar(in,[40,60],[18,14],mul:0.4)+in};
//~aural1[6]=\filter->{arg in; LPF.ar(in,\lpf3.kr(2000,4))};
//~aural1[7]=\set->Pbind(\args, [\lpf], \dur,1, \lpf, Pseq([1000,600,800,200],inf));)

~aural1[98]=\filter->{arg in; Compander.ar(in, ~tum01.ar, 0.1, 1, 0.1) };

)

~aural1[98]=nil;

p.gui;




//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// BOW LONG continous


~bowcont.clear;
~bowcont.stop(freeTime:60);
(
~bowcont.play(vol:0.001, fadeTime:4, numChannels:2);
~bowcont.quant=16;)
(~bowcont[0]=Pbind(
	\instrument, \bow,
	\dur, Pseq([2,Rest(2)],inf),
	\legato, 2,
	\octave, Pseq([[3,1],[4,1],[2,3],[1,2],[4,2]],inf),
	\degree, [2,1],
	\amp, 0.8,
);

~bowcont[30]=\filter->{arg in; GVerb.ar(in,[2,2],[16,14],maxroomsize:120,add:in,mul:0.2)+in};
~bowcont[32]=\filter->{arg in; FreeVerb.ar(in, room:4,damp:0.5)};
~bowcont[35]=\filter->{arg in; Limiter.ar(in,0.7)};

~bowcont[37]=\filter->{arg in; LPF.ar(in,1420)};
~bowcont[80]=\filter->{arg in; Compander.ar(in, ~tum01.ar, 0.1, 1, 0.1) };

)

~bowcont[7]=nil;
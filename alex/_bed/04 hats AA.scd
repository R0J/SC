//////////////////////////////
//////////////////////////////
//////////////////////////////
/////////HATS
//////////////////////////////






///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////EVERY 0.25
~tss25.clear;
~tss25.stop(fadeTime:40);

(

~tss25.play(vol:0.8, fadeTime:1, numChannels:2);
~tss25.quant=16;)
(~tss25[0]=Pbind(
	\instrument, \tss808,
	\amp,1,
	\dur,1,
	\hpf,10000,
	\dur,4,
	\legato,0.2,

);
~tss25[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,20,4,mul:0.8,add:ts*2),0.9)};
)
~tss25[7]=nil;



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TU-TU-TUM HORSY
~tsshorsy.clear;
~tsshorsy.stop(fadeTime:40);

(
~tsshorsy.play(vol:0.2, fadeTime:1, numChannels:2);
~tsshorsy.quant=16;)
(~tsshorsy[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([0,0.2,0.4,1,    1,1,0,1,  0,1,1, 0,    0,0, 0.8,1],inf),
	\amp,1,
	\dur,0.25,
	\legato,0.4,
	\hpf,Pseq([Pn(8000,64),Pn(8000,64),Pn(9200,64), Pn(14000,16),Pn(12200,16)],inf),

);
~tsshorsy[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,20,4,mul:0.8,add:ts*2),0.9)};
)
~tsshorsy[7]=nil;

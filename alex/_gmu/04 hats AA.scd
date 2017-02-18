//////////////////////////////
//////////////////////////////
//////////////////////////////
/////////HATS
//////////////////////////////






///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////EVERY 0.25
~tss25.clear;
~tss25.stop(fadeTime:40);

(
~tss25.play(vol:0.008, fadeTime:1, numChannels:2);
~tss25.quant=16;)
(~tss25[0]=Pbind(
	\instrument, \tss808,
	\amp,1,
	\dur,0.25,
	\legato,1,
	\hpf,Pseq([Pn([8000,6000],64),Pn([9000,4900],64),Pn([9200,8700],64), Pn([14000,11000],16)],inf),

);
~tss25[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,20,0.2,mul:0.8,add:ts*2),0.9)};
)
~tss25[7]=nil;








///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////TU-TU-TUM HORSY
~tsshorsy.clear;
~tsshorsy.stop(fadeTime:40);

(
~tsshorsy.play(vol:0.4, fadeTime:1, numChannels:2);
~tsshorsy.quant=16;)
(~tsshorsy[0]=Pbind(
	\instrument, \tss808,
	\amp,Pseq([1,0.1,1,1],inf),
	\dur,0.25,
	\legato,1,
	\hpf,Pseq([Pn(8000,64),Pn(8000,64),Pn(9200,64), Pn(14000,16),Pn(2200,16)],inf),

);
~tsshorsy[6]=\filter->{arg ts; Limiter.ar(GVerb.ar(ts,20,1,mul:0.8,add:ts*2),0.9)};
)
~tsshorsy[7]=nil;

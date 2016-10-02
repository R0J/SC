p = ProxySpace.push(s).makeTempoClock

(
e = ();
e.resetClock = {|event, cislo|

	Task({
		var tClock;
		var beat = currentEnvironment.clock.beats;
		var mod;
		var tolerance = 0.01;

		TempoClock.default.timeToNextBeat(1).wait;

		tClock = TempoClock.default.beats;
		mod = tClock % 1;

		if((mod>tolerance),
			{
				// currentEnvironment.clock.timeToNextBeat(1).wait;
				p.clock.beats_(0);
				"mod > tolerance".postln;

			}
		);

		tClock = TempoClock.default.beats;
		beat = currentEnvironment.clock.beats;
		("\ntClock" + tClock).postln;
		("beat" + beat).postln;
		("mod" + mod).postln;
	}).play;
	// "pozdrav".postln;
}
)

TempoClock.default.sched(0, { e.resetClock(100); 1}; )
TempoClock.default.tempo_(60/60);

p.clock.tempo_(127/60)



(
TempoClock.default.beats.postln;
p.clock.beats.postln;
)


)

(
OSCdef.newMatching(\msg_resetClock, {|msg, time, addr, recvPort|
	var msgType = msg[0];
	var sender = msg[1];

	events.aliveAnsw(sender.asSymbol);
	events.clockTempoSet(currentEnvironment[\tempo].clock.tempo*60);

}, '/user/join', nil).permanent_(true);

)
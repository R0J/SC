+ ProxySpace {

	connect {|userName|
		NetAddr.broadcastFlag = true;

		this.prGetLibrary(userName);


		this.sendNetMsg(\newConnection);
		this.sendNetMsg(\infoNetIP);
	}

	metro {|quant = 1, freq = 800|
		var metro = this.prGetLibrary.at(\metro);

		if(metro.isNil, {
			this.prGetLibrary.put(\metro, Task({
				TempoClock.default.timeToNextBeat(quant).wait;
				{
					Synth(\metronom, [\freq: freq]);
					TempoClock.all.do({|oneClock|
						("Merto tick at beats:" + TempoClock.default.beats).postln;
					});
					quant.wait;
				}.loop;
			}).play;
			);
		},{
			metro.stop;
			this.prGetLibrary.put(\metro, nil);
		});
	}

	prSenderCheck{ |msg|
		var library = this.prGetLibrary.at(\NetLibrary);
		var sender = msg[1];
		// ("msg:" + msg).postln;
		if(
			(sender.asSymbol == library.at(\userName).asSymbol),
			{ "je to moje zprava".postln; ^false; },
			{ "neni to moje zprava".postln; ^true; }
		);
	}

	prGetLibrary {|userName|
		var library = this.class.all.at(\NetLibrary);

		if( library.isNil, {
			var metroDef = { |freq|
				var sig = SinOsc.ar(freq!2);
				var env = Env([0,1,0], [0.005, 0.05], [5,-3]);
				var aEnv = EnvGen.kr(env, doneAction:2);
				Out.ar(0, sig * aEnv);
			};
			metroDef.asSynthDef(name:\metronom).add;


			this.class.all.put(\NetLibrary, IdentityDictionary.new);
			library = this.class.all.at(\NetLibrary);
			library.put(\userName, userName.asSymbol);
			"\nProxySpace NetLibrary prepared".postln;

			this.prInitReceiveMsg;
			this.prInitSendMsg;
		});

		^library;
	}

	prInitSendMsg {
		var events = ();
		var sender = this.prGetLibrary.at(\userName).asSymbol;
		var broadcastAddr = NetAddr("255.255.255.255", NetAddr.langPort);

		events.infoNetIP = {|event| broadcastAddr.sendMsg('/user/infoNetIP', sender); };

		events.newConnection = {|event| broadcastAddr.sendMsg('/user/newConnection', sender); };
		events.loged = {|event| broadcastAddr.sendMsg('/user/loged', sender); };

		events.time = {|event| broadcastAddr.sendMsg('/clock/time', sender); };
		events.setTime = {|event, setTime| broadcastAddr.sendMsg('/clock/setTime', sender, setTime); };

		this.prGetLibrary.put(\events, events);
	}

	sendNetMsg {|type, args|
		var event = this.prGetLibrary.at(\events).at(type.asSymbol);
		if( event.notNil, { event.value(event, args); });
		^nil;
	}

	prInitReceiveMsg {
		var aaa = this.class.all.at(\NetLibrary).at(\events);

		OSCdef.newMatching(\msg_infoNetIP, {|msg, time, addr, recvPort|
			var broadcastIP = addr.ip.split($.).put(3,255).join(".");
			this.prSenderCheck(msg);
			("NetIP:" + addr.ip + "; BroadcastIP:" + broadcastIP).postln;
		},  '/user/infoNetIP', nil).permanent_(true);

		OSCdef.newMatching(\msg_newConnection, {|msg, time, addr, recvPort|
			var msgType = msg[0];
			var sender = msg[1];
			this.prSenderCheck(msg);
			"Player % has joined to session".format(sender).warn;
			this.sendNetMsg(\loged);
			// events.clockTempoSet(currentEnvironment[\tempo].clock.tempo*60);
		}, '/user/newConnection', nil).permanent_(true);

		OSCdef.newMatching(\msg_loged, {|msg, time, addr, recvPort|
			var msgType = msg[0];
			var sender = msg[1];
			var args = msg[2];
			// msg.postln;
			"Player % is here too".format(sender).warn;
			// events.clockTempoSet(currentEnvironment[\tempo].clock.tempo*60);
		}, '/user/loged', nil).permanent_(true);

		OSCdef.newMatching(\msg_time, {|msg, time, addr, recvPort|
			var msgType = msg[0];
			var sender = msg[1];
			// var beat = msg[2];
			// msg.postln;
			"Current proxy clock beat is %".format(TempoClock.default.beats).postln;
		}, '/clock/time', nil).permanent_(true);

		OSCdef.newMatching(\msg_setTime, {|msg, time, addr, recvPort|
			var msgType = msg[0];
			var sender = msg[1];
			var syncQuant = msg[2];
			this.restartClock;
			/*
			var currentBeat = currentEnvironment.clock.beats;
			var lastQuant = (currentBeat/syncQuant).floor;
			var nextQuant = (currentBeat/syncQuant).ceil;
			// var targetQuant = lastQuant * syncQuant;
			var targetQuant = nextQuant * syncQuant;
			var syncTimeAt = targetQuant - currentBeat;

			"Sync of clock at % beats".format(syncTimeAt).postln;
			currentEnvironment.clock.sched(syncTimeAt, {
			currentEnvironment.clock.beats = targetQuant;
			"Current proxy is set to beats %".format(currentEnvironment.clock.beats).postln;
			});
			*/
		}, '/clock/setTime', nil).permanent_(true);
	}

	restartClock {
		TempoClock.allClocksRestart;

		/*
		var oldClock = TempoClock.default;
		var newClock = TempoClock.new(oldClock.tempo);
		var oldQueue = oldClock.queue;

		newClock.permanent_(true);

		Task{
		oldQueue.do({|oneVal|
		// ("oneVal:"+oneVal).postln;
		case
		{oneVal.isKindOf(Function)} {
		"jsem Function".postln;
		("oneVal.isPlaying:"+oneVal.isPlaying).postln;
		newClock.sched(0, oneVal);
		}
		{oneVal.isKindOf(EventStreamPlayer)} {
		"jsem EventStreamPlayer".postln;
		newClock.sched(0, oneVal);
		}
		{oneVal.isKindOf(Task)} {
		"jsem Task".postln;
		newClock.sched(0, oneVal);
		}
		;
		});
		0.2.wait;
		// newQueue = newClock.queue;
		TempoClock.default = newClock;
		oldClock.stop;
		}.play;


		("oldQueue:" + oldQueue).postln;
		// ("newQueue:" + newQueue).postln;
		*/
	}

	moveNodeToTail {|nodeName|
		var nodeproxy = this.doFunctionPerform(nodeName.asSymbol);
		this.clock.sched(0, {

			if (nodeproxy.typeStr.asSymbol == 'ar 2')
			{
				nodeproxy.asTarget.moveToTail();
				// "posunu mastra na konec".postln;
			};
			1; // cas pro opakovane volani
		});
	}
}

+ TempoClock {

	*allClocksRestart {
		var allClocks = this.all;
		("allClocks: " + allClocks).postln;

		allClocks.do({|oneClock|
			var queue = oneClock.queue;

			("queue: " + queue).postln;

			if (queue.size > 1) {
				forBy(1, queue.size-1, 3) {|i|
					var time = queue[i];
					var item = queue[i+1];
					("time[i]: " + queue[i]).postln;
					("item[i+1]: " + queue[i+1]).postln;

					queue[i] = 0;
					// queue[i+1].removedFromScheduler(releaseNodes)
				};

				("queue: " + queue).postln;
				oneClock.beats = 0;
			};
		});

		("TempoClock restart").postln;
	}


}



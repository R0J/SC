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
					// TempoClock.all.do({|oneClock|
					// ("Merto tick at beats:" + oneClock.beats).postln;
					// });
					("\nTempoClock.default.beats:" + TempoClock.default.beats).postln;
					("currentEnvirnment.clock.beats:" + currentEnvironment.clock.beats).postln;

					quant.wait;
				}.loop;
			}).play;
			);
		},{
			metro.stop;
			this.prGetLibrary.put(\metro, nil);
		});
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
			currentEnvironment.clock.beats =  TempoClock.default.beats;

			this.prInitReceiveMsg;
			this.prInitSendMsg;
		});
		^library;
	}

	prPostLibrary {
		var library = this.prGetLibrary;
		library.associationsDo({|assoc| "\t- % -> %".format(assoc.key,assoc.value).postln; })
		^nil;
	}

	prInitSendMsg {
		var events = ();
		var sender = this.prGetLibrary.at(\userName).asSymbol;
		// var broadcastAddr = NetAddr("255.255.255.255", NetAddr.langPort);
		var broadcastAddr = NetAddr("10.0.0.255", NetAddr.langPort);

		events.infoNetIP = {|event| broadcastAddr.sendMsg('/user/infoNetIP', sender); };

		events.newConnection = {|event| "events.newConnection send".postln; broadcastAddr.sendMsg('/user/newConnection', sender); };
		events.loged = {|event| "events.loged send".postln;  broadcastAddr.sendMsg('/user/loged', sender); };

		events.time = {|event| "events.time send".postln; broadcastAddr.sendMsg('/clock/time', sender, TempoClock.default.beats); };
		events.timeAnswer = {|event| broadcastAddr.sendMsg('/clock/time/answer', sender, TempoClock.default.beats); };
		events.timeSync = {|event, setTime| "events.timeSync send".postln; broadcastAddr.sendMsg('/clock/sync', sender); };

		this.prGetLibrary.put(\events, events);
	}

	sendNetMsg {|type, args|
		var event = this.prGetLibrary.at(\events).at(type.asSymbol);
		if( event.notNil, { event.value(event, args); });
		^nil;
	}

	prSenderCheck{ |msg|
		var library = this.prGetLibrary;
		var sender = msg[1];
		if((sender.asSymbol == library.at(\userName).asSymbol),	{ ^false; }, { ^true; });
	}

	prInitReceiveMsg {

		OSCdef.newMatching(\msg_infoNetIP, {|msg, time, addr, recvPort|
			var broadcastIP = addr.ip.split($.).put(3,255).join(".");
			this.prSenderCheck(msg);
			("NetIP:" + addr.ip + "; BroadcastIP:" + broadcastIP).postln;
		},  '/user/infoNetIP', nil).permanent_(true);

		OSCdef.newMatching(\msg_newConnection, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(msg), {
				var sender = msg[1];
				"Player % has joined to session".format(sender).warn;
				this.sendNetMsg(\loged);
			});
		}, '/user/newConnection', nil).permanent_(true);

		OSCdef.newMatching(\msg_loged, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(msg), {
				var sender = msg[1];
				"Player % is here too".format(sender).warn;
			});
		}, '/user/loged', nil).permanent_(true);

		OSCdef.newMatching(\msg_time, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(msg), {
				var msgType = msg[0];
				var sender = msg[1];
				var otherTime = msg[2];
				var myTime = TempoClock.default.beats;
				(
					"% - My clock beats"
					"\n% - % clock beats"
					"\ndifferent: %"
				).format(myTime, otherTime, sender, (myTime - otherTime).asFloat).postln;
				this.sendNetMsg(\timeAnswer);
			});
		}, '/clock/time', nil).permanent_(true);

		OSCdef.newMatching(\msg_timeAnswer, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(msg), {
				var msgType = msg[0];
				var sender = msg[1];
				var otherTime = msg[2];
				var myTime = TempoClock.default.beats;
				(
					"% - My clock beats"
					"\n% - % clock beats"
					"\ndifferent: %"
				).format(myTime, otherTime, sender, (myTime - otherTime).asFloat).postln;
			});
		}, '/clock/time/answer', nil).permanent_(true);

		OSCdef.newMatching(\msg_timeSync, {|msg, time, addr, recvPort|
			// var msgType = msg[0];
			// var sender = msg[1];
			// var syncQuant = msg[2];
			// if(this.prSenderCheck(msg), {
				TempoClock.allClocksRestart;
		// });

		}, '/clock/sync', nil).permanent_(true);
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


					case
					{item.isKindOf(EventStreamPlayer)} { "jsem EventStreamPlayer".postln; queue[i+1].reset;}
					;

					queue[i] = 1;
					// queue[i+1].removedFromScheduler(releaseNodes)
				};

			};
			("queue: " + queue).postln;
			oneClock.beats = 0;
		});

		("TempoClock restart").postln;
	}


}



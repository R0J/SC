+ ProxySpace {

	connect {|userName = nil|
		// NetAddr.broadcastFlag = true;
		// this.prGetLibrary(userName);
		// this.prGetBroadcastIP;
	}

	metro {|quant = 1, freq = 800|
		var metro = this.prGetLibrary.at(\metro);
		var isShared = this.prGetLibrary.at(\sharedCode);

		if(metro.isNil, {
			var code = Task({
				TempoClock.default.timeToNextBeat(quant).wait;
				{
					Synth(\metronom, [\freq: freq, \metronomTrig, 1]);
					("\nTempoClock.default.beats:" + TempoClock.default.beats).postln;
					("currentEnvirnment.clock.beats:" + currentEnvironment.clock.beats).postln;
					quant.wait;
				}.loop;
			}).play;
			this.prGetLibrary.put(\metro, code);
		},{
			metro.stop;
			this.prGetLibrary.put(\metro, nil);
		});
	}

	time { this.sendNetMsg(\clock_beats); ^nil; }

	restartClock { this.sendNetMsg(\clock_sync); ^nil;}

	sharedCode{
		var isShared = this.prGetLibrary.at(\sharedCode);
		if(isShared.not,
			{
				this.prGetLibrary.put(\sharedCode, true);
				thisProcess.interpreter.codeDump = { |code| ("Evaluated code:" + code ).postln;  this.sendNetMsg(\code_evaluate, code); };
			},{
				this.prGetLibrary.put(\sharedCode, false);
				thisProcess.interpreter.codeDump = nil;
			}
		);
	}

	prGetLibrary {|userName|
		var library = this.class.all.at(\NetLibrary);

		if( library.isNil, {
			var metroDef = { |freq|
				var metronomTrig = \metronomTrig.tr(0);
				var sig = SinOsc.ar(freq!2);
				var env = Env([0,1,0], [0.005, 0.05], [5,-3]);
				var aEnv = EnvGen.kr(env, doneAction:2);
				SendTrig.kr(metronomTrig);
				Out.ar(0, sig * aEnv);
			};
			metroDef.asSynthDef(name:\metronom).add;

			currentEnvironment.clock.beats = TempoClock.default.beats;

			this.class.all.put(\NetLibrary, IdentityDictionary.new);
			library = this.class.all.at(\NetLibrary);
			if(userName.notNil, { library.put(\userName, userName.asSymbol); });
			this.prGetLibrary.put(\sharedCode, false);
			"\nProxySpace NetLibrary prepared".postln;
		});
		^library;
	}

	prGetBroadcastIP {

		OSCdef.newMatching(\msg_getNetIP, {|msg, time, addr, recvPort|
			var library = this.prGetLibrary;
			var broadcastIP = addr.ip.split($.).put(3,255).join(".");
			("prGetBroadcastIP NetIP:" + addr.ip + "; BroadcastIP:" + broadcastIP).postln;

			library.put(\broadcastAddr, broadcastIP.asSymbol);
			library.put(\userIP, addr.ip.asSymbol);
			this.prInitReceiveMsg;
			this.prInitSendMsg;
			this.sendNetMsg(\user_newConnection);

		},  '/user/getNetIP', nil).oneShot;

		NetAddr("255.255.255.255", NetAddr.langPort).sendMsg('/user/getNetIP');
	}

	prPostLibrary {
		var library = this.prGetLibrary;
		library.associationsDo({|assoc| "\t- % -> %".format(assoc.key,assoc.value).postln; })
		^nil;
	}

	prInitSendMsg {
		var library = this.prGetLibrary;
		var events = ();
		var sender = this.prGetLibrary.at(\userIP).asSymbol;
		var broadcastAddr = NetAddr( library.at(\broadcastAddr).asString, NetAddr.langPort) ;

		events.user_newConnection = {|event| broadcastAddr.sendMsg('/user/newConnection', sender); };
		events.user_loged = {|event|  broadcastAddr.sendMsg('/user/loged', sender); };

		events.clock_beats = {|event| broadcastAddr.sendMsg('/clock/beats', sender, TempoClock.default.beats); };
		events.clock_beats_answer = {|event| broadcastAddr.sendMsg('/clock/beats/answer', sender, TempoClock.default.beats); };
		events.clock_sync = {|event, setTime| "events.clock_sync send".postln; broadcastAddr.sendMsg('/clock/sync', sender); };

		events.code_evaluate = {|event, code| broadcastAddr.sendMsg('/code/evaluate', sender, code); };

		this.prGetLibrary.put(\events, events);
	}

	sendNetMsg {|type, arg1, arg2, arg3 |
		var event = this.prGetLibrary.at(\events).at(type.asSymbol);
		if( event.notNil, { event.value(event, arg1, arg2, arg3); });
		^nil;
	}

	prSenderCheck{ |addr|
		var library = this.prGetLibrary;
		var senderIP = addr.ip;

		if((senderIP.asSymbol == library.at(\userIP).asSymbol),	{ ^false; }, { ^true; } );
	}

	prInitReceiveMsg {
		var library = this.prGetLibrary;
		var broadcastAddr = NetAddr( library.at(\broadcastAddr).asString, NetAddr.langPort) ;

		OSCdef.newMatching(\user_newConnection, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1];
				"Player % has joined to session".format(sender).warn;
				this.sendNetMsg(\user_loged);
			});
		}, '/user/newConnection', nil).permanent_(true);

		OSCdef.newMatching(\user_loged, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1];
				"Player % is here too".format(sender).warn;
			});
		}, '/user/loged', nil).permanent_(true);

		OSCdef.newMatching(\clock_beats, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var msgType = msg[0];
				var sender = msg[1];
				var otherTime = msg[2];
				var myTime = TempoClock.default.beats;
				(
					"% - My clock beats"
					"\n% - % clock beats"
					"\ndifferent: %"
				).format(myTime, otherTime, sender, (myTime - otherTime).asFloat).postln;
				[msg, time, addr, recvPort].postln;
				this.sendNetMsg(\clock_beats_answer);
			});
		}, '/clock/beats', nil).permanent_(true);

		OSCdef.newMatching(\clock_beats_Answer, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
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
		}, '/clock/beats/answer', nil).permanent_(true);

		OSCdef.newMatching(\clock_sync, {|msg, time, addr, recvPort|
			TempoClock.allClocksRestart;
		}, '/clock/sync', nil).permanent_(true);

		OSCdef.newMatching(\msg_code_evaluate, {|msg, time, addr, recvPort|
			var msgType = msg[0];
			var sender = msg[1];
			var code = msg[2];
			// [msg, time, addr, recvPort].postln;

			"\n\nCodeExecute from %\n%".format(sender,  code).postln;

			if(this.prSenderCheck(addr), {
				// weak protection
				"code begins with p. -> %".format(code.asString.beginsWith("p.")).postln;
				if(code.asString.beginsWith("p.").not, {
					thisProcess.interpreter.interpret(code.asString);
				});
			});
		}, '/code/evaluate', nil).permanent_(true);
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

					queue[i] = 0;
					// queue[i+1].removedFromScheduler(releaseNodes)
				};

			};
			("queue: " + queue).postln;
			oneClock.beats = 0;
		});

		("TempoClock restart").postln;
	}


}



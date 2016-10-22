NetProxy : ProxySpace {
	// NetProxy {

	var userName;
	var netIP, broadcastIP;
	var netAddrs;
	var sendMsg;

	var timeMaster;
	var metronom;

	*connect { |name = nil|
		// *new { |name = nil|
		// var proxyspace = super.new.push(Server.default).makeTempoClock;
		// super.push(Server.default).makeTempoClock;
		// ^proxyspace;
		var proxyspace = super.push(Server.default);

		Server.default.waitForBoot({
			proxyspace.makeTempoClock;
			proxyspace.initNet(name);
		});
		^proxyspace;
	}

	initNet {|name|
		NetAddr.broadcastFlag = true;
		// currentEnvironment.clock.beats = TempoClock.default.beats;

		TempoClock.setAllClocks(0, currentEnvironment.clock.tempo);

		sendMsg = ();
		netAddrs = IdentityDictionary.new();

		if(name.notNil, { userName = name; }, {
			userName = Platform.case(
				\osx,       { "whoami".unixCmdGetStdOut.replace("\n", ""); },
				\linux,     { "whoami".unixCmdGetStdOut.replace("\n", ""); },
				\windows,   { "echo %username%".unixCmdGetStdOut.replace("\n", ""); }
			);
		});

		timeMaster = true;
		metronom = nil;

		this.prMetronomDef;
		this.prGetBroadcastIP;
	}

	time { sendMsg.clock_get; ^nil; }

	metro {|quant = 1, freq = 800|
		if(metronom.isNil, {
			metronom = TempoClock.new(currentEnvironment.clock.tempo);
			metronom.sched(currentEnvironment.clock.timeToNextBeat(quant), {
				Synth(\metronom, [\freq: freq, \metronomTrig, 1]);
				("\nTempoClock.default.beats:" + TempoClock.default.beats).postln;
				("currentEnvirnment.clock.beats:" + currentEnvironment.clock.beats).postln;
				quant;
			});
		},{
			metronom.stop;
			metronom = nil;
		});
	}

	prGetBroadcastIP {

		OSCdef.newMatching(\msg_getNetIP, {|msg, time, addr, recvPort|
			var broadcastIP = addr.ip.split($.).put(3,255).join(".");
			netIP = addr.ip.asSymbol;
			broadcastIP = broadcastIP.asSymbol;
			this.prInitSendMsg;
			this.prInitReceiveMsg;
			("\nNetProxy init done...\nUserName:" +  userName + "; NetIP:" + addr.ip + "; BroadcastIP:" + broadcastIP).postln;

			NetAddr( broadcastIP.asString, NetAddr.langPort).sendMsg('/user/connected', userName);

		},  '/user/getNetIP', nil).oneShot;

		NetAddr("255.255.255.255", NetAddr.langPort).sendMsg('/user/getNetIP');
	}

	prInitSendMsg {

		sendMsg.user_loged = {|event, target|
			("sendMsg.user_loged to target % send").format(target).postln;
			netAddrs.at(target.asSymbol).sendMsg('/user/loged', userName);
		};

		sendMsg.user_timeMaster = {|event, target|
			("sendMsg.user_timeMaster to target % send").format(target).postln;
			netAddrs.at(target.asSymbol).sendMsg('/user/timeMaster', userName);
		};

		sendMsg.clock_set = {|event|
			netAddrs.keysValuesDo {|key, target|
				("sendMsg.clock_set to target % send").format(key).postln;
				target.sendMsg('/clock/set', userName, currentEnvironment.clock.beats, currentEnvironment.clock.tempo);
			};
		};

		sendMsg.clock_get = {|event|
			netAddrs.keysValuesDo {|key, target|
				("sendMsg.clock_get to target % send").format(key).postln;
				target.sendMsg('/clock/get', userName, currentEnvironment.clock.beats);
			};
		};

		sendMsg.clock_get_answer = {|event, target|
			("sendMsg.clock_get_answer to target % send").format(target).postln;
			netAddrs.at(target.asSymbol).sendMsg('/clock/get/answer', userName, currentEnvironment.clock.beats);
		};

		/*
		sendMsg.clock_sync = {|event, setTime| "events.clock_sync send".postln; broadcastAddr.sendMsg('/clock/sync', sender); };
		sendMsg.code_evaluate = {|event, code| broadcastAddr.sendMsg('/code/evaluate', sender, code); };
		*/

	}
	prInitReceiveMsg {

		OSCdef.newMatching(\user_connected, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				"Player % has joined to session".format(sender).warn;
				netAddrs.put(sender, addr);
				sendMsg.user_loged(sender);
				if(timeMaster) {
					sendMsg.user_timeMaster(sender);
					sendMsg.clock_set;
				};
				netAddrs.postln;
			});
		}, '/user/connected', nil).permanent_(true);

		OSCdef.newMatching(\user_loged, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				netAddrs.put(sender, addr);
				"Player % is here too".format(sender).warn;
				netAddrs.postln;
			});
		}, '/user/loged', nil).permanent_(true);

		OSCdef.newMatching(\user_timeMaster, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				timeMaster = false;
				"Player % is time master".format(sender).warn;
			});
		}, '/user/timeMaster', nil).permanent_(true);

		OSCdef.newMatching(\clock_set, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				var newTime = msg[2];
				var newTempo = msg[3];
				[msg, time, addr, recvPort].postln;

				TempoClock.setAllClocks(newTime, newTempo);
				// currentEnvironment.clock.tempo = newTempo;
				// TempoClock.default.tempo = newTempo;
				/*
				Server.default.bind({
				// currentEnvironment.clock.beats_(newTime);
				TempoClock.default.beats_(newTime);
				currentEnvironment.clock.beats = TempoClock.default.beats;
				});
				*/
				"Player % set clock at beat %".format(sender, newTime).warn;
				// ("currentEnvironment.clock.beats" + currentEnvironment.clock.beats).postln;
			});
		}, '/clock/set', nil).permanent_(true);

		OSCdef.newMatching(\clock_get, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				var senderTime = msg[2];
				var yourTime = currentEnvironment.clock.beats;
				sendMsg.clock_get_answer(sender);
				"Yours time: %\n% time: %\ndifference: %".format(yourTime, sender, senderTime, (yourTime - senderTime)).warn;
			});
		}, '/clock/get', nil).permanent_(true);

		OSCdef.newMatching(\clock_get_answer, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				var senderTime = msg[2];
				var yourTime = currentEnvironment.clock.beats;
				// currentEnvironment.clock.beats_(newTime);
				// TempoClock.default.beats_(yourTime);
				"Yours time: %\n% time: %\ndifference: %".format(yourTime, sender, senderTime, (yourTime - senderTime)).warn;
			});
		}, '/clock/get/answer', nil).permanent_(true);

		OSCdef.newMatching(\clock_get_answer, {|msg, time, addr, recvPort|
			// o = OSCFunc({ arg msg, time;
			var metronomTime = currentEnvironment.clock.beats;
			"Metronom answer time: %".format(metronomTime).postln;
		},'/tr', Server.default.addr);


		/*

		OSCdef.newMatching(\clock_sync, {|msg, time, addr, recvPort|
		// TempoClock.allClocksRestart;
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
		*/
	}


	prSenderCheck{ |addr| if((addr.ip.asSymbol == netIP.asSymbol), { ^false; }, { ^true; } ); }

	prMetronomDef {
		{ |freq|
			var metronomTrig = \metronomTrig.tr(0);
			var sig = SinOsc.ar(freq!2);
			var env = Env([0,1,0], [0.005, 0.05], [5,-3]);
			var aEnv = EnvGen.kr(env, doneAction:2);
			SendTrig.kr(metronomTrig);
			Out.ar(0, sig * aEnv);
		}.asSynthDef(name:\metronom).add;
	}
}

+ TempoClock {

	*setAllClocks {|targetTime, targetTempo|
		var allClocks = this.all;
		("allClocks: " + allClocks).postln;

		allClocks.do({|oneClock|
			var queue = oneClock.queue;

			("queue: " + queue).postln;

			if (queue.size > 1) {
				forBy(1, queue.size-1, 3) {|i|
					var time = queue[i];
					var item = queue[i+1];
					// ("time[i]: " + queue[i]).postln;
					// ("item[i+1]: " + queue[i+1]).postln;

					// case
					// {item.isKindOf(EventStreamPlayer)} { "jsem EventStreamPlayer".postln; queue[i+1].reset;}
					// ;

					queue[i] = targetTime;
					// queue[i+1].removedFromScheduler(releaseNodes)
				};

			};
			// ("queue: " + queue).postln;
			oneClock.beats = targetTime;
			oneClock.tempo = targetTempo;
			("oneClock.beats:" + oneClock.beats).postln;
		});

		("TempoClock restart").postln;
	}


}
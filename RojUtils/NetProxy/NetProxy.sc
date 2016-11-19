NetProxy : ProxySpace {

	classvar isConnected = false;

	var userName;
	var netIP, broadcastIP;
	var netAddrs;
	var sendMsg;

	// var timeMaster;

	var metronom;
	var metronomSynth = nil;
	var metroQuant, metroFreq;

	var oscScTrigAddr, oscScTrigClock;

	var fnc_onRestartClock;

	*push { |bpm = 127|
		var proxyspace = super.push(Server.default);
		isConnected = false;
		Server.default.waitForBoot({
			Server.default.latency = 0.0;
			proxyspace.disconnect;
			proxyspace.makeTempoClock;
			proxyspace.prMetronomDef;
			TempoClock.setAllClocks(0, bpm/60);
			// CmdPeriod.add(proxyspace);
			"\nNetProxy init done".postln;
		});
		^proxyspace;
	}

	*connect { |name = nil|
		var proxyspace = this.push();
		Server.default.doWhenBooted({
			proxyspace.initNet(name);
			"\nNetProxy connected".postln;
		});
		^proxyspace;
	}

	disconnect {
		// CmdPeriod.removeAll;
		OSCdef.freeAll;

		if(isConnected) { sendMsg.user_exit };

		userName = nil;
		netIP = nil;
		broadcastIP = nil;
		netAddrs = nil;
		sendMsg = nil;

		if(metronomSynth.notNil)
		{
			metronom.clear;
			metronom = nil;
			metronomSynth = nil;
		};

		if(isConnected) {"\nNetProxy disconnected".postln;};
		isConnected = false;

		^nil;
	}

	initNet {|name|
		NetAddr.broadcastFlag = true;

		sendMsg = ();
		netAddrs = IdentityDictionary.new();

		if(name.notNil, { userName = name; }, {
			userName = Platform.case(
				\osx,       { "whoami".unixCmdGetStdOut.replace("\n", ""); },
				\linux,     { "whoami".unixCmdGetStdOut.replace("\n", ""); },
				\windows,   { "echo %username%".unixCmdGetStdOut.replace("\n", ""); }
			);
		});

		// timeMaster = true;
		metronom = nil;
		oscScTrigClock = nil;
		isConnected = true;

		this.prGetBroadcastIP;
	}
	/*
	cmdPeriod {
	"NetProxy cmpPeriod protection".warn;
	("\t- tempoClock.default.beats:" + TempoClock.default.beats).postln;
	("\t- currentEnvirnment.clock.beats:" + currentEnvironment.clock.beats).postln;
	("\t- metronomSynth:" + metronomSynth).postln;
	if(metronomSynth.notNil){
	metronom.stop;
	metronom = nil;
	metronomSynth = nil;
	this.metro(metroQuant, metroFreq);
	};

	// Server.freeAll;
	Server.hardFreeAll;
	Server.resumeThreads;
	}
	*/

	name { if(isConnected, { ^userName.asSymbol; },{ ^nil; }) }

	players {
		if(isConnected,
			{
				"\nyour profile:".postln;
				("\t- name:" + userName).postln;
				("\t- addr:" + netIP).postln;

				"\nother profiles:".postln;
				netAddrs.sortedKeysValuesDo({|playerName, playerNet|
					("\t- name:" + playerName).postln;
					("\t- addr:" + playerNet.ip + "\n").postln;
				});
		}, { ^nil });
	}

	bpm { |bpm = nil|
		if((bpm.notNil), {
			{
				("\BPM set:" + bpm).postln;
				currentEnvironment.clock.timeToNextBeat(1).wait;
				TempoClock.setAllClocks(currentEnvironment.clock.beats, bpm/60);
				if(isConnected) { sendMsg.clock_set; };
			}.fork;
		},{
			("\Current BPM is:" + (currentEnvironment.clock.tempo * 60)).postln;
			^nil;
		});
	}

	time {
		if(isConnected) { sendMsg.clock_get; };

		("\nTempoClock.default.beats:" + TempoClock.default.beats).postln;
		("currentEnvirnment.clock.beats:" + currentEnvironment.clock.beats).postln;
		^nil;
	}

	restartClock {
		if(isConnected) { sendMsg.clock_restart; };
		TempoClock.setAllClocks(0, currentEnvironment.clock.tempo);
		fnc_onRestartClock.value;
	}

	metro {|quant = 1, freq = 800|
		metroQuant = quant;
		metroFreq = freq;
		if(metronom.isNil, {
			metronom = TempoClock.new(currentEnvironment.clock.tempo);
			metronom.sched(currentEnvironment.clock.timeToNextBeat(quant), {
				metronomSynth = Synth(\metronom, [\freq: freq, \metronomTrig, 1]);
				// NodeWatcher.register(metronomSynth.asTarget);
				("\nTempoClock.default.beats:" + TempoClock.default.beats).postln;
				("currentEnvirnment.clock.beats:" + currentEnvironment.clock.beats).postln;
				quant;
			});
		},{
			metronom.stop;
			metronom = nil;
			metronomSynth = nil;
		});
	}

	oscTrig { |quant = 1|

		if(oscScTrigClock.isNil, {
			oscScTrigClock = TempoClock.new(currentEnvironment.clock.tempo);
			oscScTrigClock.sched(currentEnvironment.clock.timeToNextBeat(quant), {
				oscScTrigAddr.sendMsg('/scTrig', quant, currentEnvironment.clock.tempo*60);
				quant;
			});
		},{
			oscScTrigClock.stop;
			oscScTrigClock = nil;
		});
	}

	prGetBroadcastIP {

		OSCdef.newMatching(\msg_getNetIP, {|msg, time, addr, recvPort|
			var broadcastIP = addr.ip.split($.).put(3,255).join(".");
			netIP = addr.ip.asSymbol;
			broadcastIP = broadcastIP.asSymbol;
			this.prInitSendMsg;
			this.prInitReceiveMsg;
			// ("\nUserName:" +  userName + "; NetIP:" + addr.ip + "; BroadcastIP:" + broadcastIP).postln;

			oscScTrigAddr = NetAddr( broadcastIP.asString, 10000);
			NetAddr( broadcastIP.asString, NetAddr.langPort).sendMsg('/user/connected', userName);

		},  '/user/getNetIP', nil).oneShot;

		NetAddr("255.255.255.255", NetAddr.langPort).sendMsg('/user/getNetIP');
	}

	prInitSendMsg {

		sendMsg.user_loged = {|event, target|
			("sendMsg.user_loged to target % send").format(target).postln;
			netAddrs.at(target.asSymbol).sendMsg('/user/loged', userName);
		};

		sendMsg.user_exit = {|event|
			netAddrs.keysValuesDo {|key, target|
				("sendMsg.user_exit to target % send").format(key).postln;
				target.sendMsg('/user/exit', userName);
			};
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

		sendMsg.clock_restart = {|event|
			netAddrs.keysValuesDo {|key, target|
				("sendMsg.clock_restart to target % send").format(key).postln;
				target.sendMsg('/clock/restart', userName, currentEnvironment.clock.tempo);
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

		// sendMsg.code_evaluate = {|event, code| broadcastAddr.sendMsg('/code/evaluate', sender, code); };
	}

	prInitReceiveMsg {

		OSCdef.newMatching(\user_connected, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				"Player % has joined to session".format(sender).warn;
				netAddrs.put(sender, addr);
				sendMsg.user_loged(sender);
				// if(timeMaster) {
				// sendMsg.user_timeMaster(sender);
				sendMsg.clock_set;
				// };
				this.players;
			});
		}, '/user/connected', nil).permanent_(true);

		OSCdef.newMatching(\user_loged, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				netAddrs.put(sender, addr);
				"Player % is here too".format(sender).warn;
				this.players;
			});
		}, '/user/loged', nil).permanent_(true);

		OSCdef.newMatching(\user_disconnected, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				netAddrs.removeAt(sender);
				"Player % leaved from session".format(sender).warn;
				this.players;
			});
		}, '/user/exit', nil).permanent_(true);

		OSCdef.newMatching(\user_timeMaster, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				// timeMaster = false;
				"Player % is time master".format(sender).warn;
			});
		}, '/user/timeMaster', nil).permanent_(true);

		OSCdef.newMatching(\clock_set, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				var newTime = msg[2];
				var newTempo = msg[3];
				TempoClock.setAllClocks(newTime, newTempo);
				"Player % set clock at beat % and tempo %".format(sender, newTime, newTempo * 60).warn;
			});
		}, '/clock/set', nil).permanent_(true);

		OSCdef.newMatching(\clock_restart, {|msg, time, addr, recvPort|
			if(this.prSenderCheck(addr), {
				var sender = msg[1].asSymbol;
				var senderTempo = msg[2];
				TempoClock.setAllClocks(0, senderTempo);
				fnc_onRestartClock.value;
				"Player % restart all clock".format(sender).warn;
			});
		}, '/clock/restart', nil).permanent_(true);

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
				"Yours time: %\n% time: %\ndifference: %".format(yourTime, sender, senderTime, (yourTime - senderTime)).warn;
			});
		}, '/clock/get/answer', nil).permanent_(true);

		OSCdef.newMatching(\metronom_answer, {|msg, time, addr, recvPort|
			var metronomTime = currentEnvironment.clock.beats;
			"Metronom answer time: %".format(metronomTime).postln;
		},'/tr', Server.default.addr);


		/*

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

	addOnRestartClock { |function|
		Server.default.waitForBoot({
			fnc_onRestartClock = function;
		});
	}
	removeOnRestartClock { fnc_onRestartClock = nil; }

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

		allClocks.do({|oneClock|
			var queue = oneClock.queue;

			if (queue.size > 1) {
				forBy(1, queue.size-1, 3) {|i|
					var time = queue[i];
					var item = queue[i+1];
					queue[i] = targetTime;
				};

			};
			oneClock.beats = targetTime;
			oneClock.tempo = targetTempo;
		});
	}
}
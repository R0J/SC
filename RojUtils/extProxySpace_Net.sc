+ ProxySpace {

	connect {|userName|
		NetAddr.broadcastFlag = true;

		this.prGetLibrary(userName);
		this.prInitReceiveMsg;
		this.prInitSendMsg();

		this.sendNetMsg(\newConnection);
		this.sendNetMsg(\infoNetIP);
	}

	sendNetMsg {|type, aaaa|
		var library = this.prGetLibrary;
		var events = library.at(\events);
var e = events.at(type.asSymbol);

		// ("sendNetMsg e:" + e).postln;

		if( events.notNil, { e.value(\ahoj); });
	}

	prSenderCheck{ |msg|
		var library = this.class.all.at(\NetLibrary);
		var sender = msg[1];
		("msg:" + msg).postln;
		if(
			(sender.asSymbol == library.at(\userName).asSymbol),
			{ "je to moje zprava".postln; },
			{ "neni to moje zprava".postln; }
		);
	}

	prGetLibrary {|userName|
		var library = this.class.all.at(\NetLibrary);

		if( library.isNil, {
			this.class.all.put(\NetLibrary, IdentityDictionary.new);
			library = this.class.all.at(\NetLibrary);
			library.put(\userName, userName.asSymbol);
			"\nProxySpace NetLibrary prepared".postln;
		});

		^library;
	}

	prInitSendMsg {
		var events = ();
		var sender = this.prGetLibrary.at(\userName).asSymbol;
		var broadcastAddr = NetAddr("255.255.255.255", NetAddr.langPort);

		events.infoNetIP = {|event| broadcastAddr.sendMsg('/user/infoNetIP', sender); };

		events.newConnection = {|event| broadcastAddr.sendMsg('/user/newConnection', sender); };
		events.loged = {|event, aaa| ("aaa:" + aaa).postln;   broadcastAddr.sendMsg('/user/loged', sender, aaa); };

		this.prGetLibrary.put(\events, events);
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
			// this.sendNetMsg(\loged, "testArgs");
			// aaa.loged("pozdrav2");
			this.class.all.at(\NetLibrary).at(\events).loged("testArgs");
			// events.clockTempoSet(currentEnvironment[\tempo].clock.tempo*60);
		}, '/user/newConnection', nil).permanent_(true);

		OSCdef.newMatching(\msg_loged, {|msg, time, addr, recvPort|
			var msgType = msg[0];
			var sender = msg[1];
			// var args = msg[2];
			msg.postln;
			"Player % is loged too".format(sender).warn;
			// events.clockTempoSet(currentEnvironment[\tempo].clock.tempo*60);
		}, '/user/loged', nil).permanent_(true);
	}

	moveNodeToTail {|nodeName|
		var nodeproxy = this.doFunctionPerform(nodeName.asSymbol);
		this.clock.sched(0, {

			if (nodeproxy.typeStr.asSymbol == 'ar 2')
			{
				nodeproxy.asTarget.moveToTail();
				"posunu mastra na konec".postln;
			};
			1; // cas pro opakovane volani
		});
	}
}
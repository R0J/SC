+ ProxySpace {

	connect {|userName|
		NetAddr.broadcastFlag = true;

		this.prGetLibrary(userName);
		this.prInitReceiveMsg;
		this.prInitSendMsg();

		this.sendNetMsg(\join);
		this.sendNetMsg(\getMyIP);
	}



	sendNetMsg {|type|
		var library = this.prGetLibrary;
		var events = library.at(\events);

		if( events.notNil, { events.at(type.asSymbol).value; });
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
/*
	prNetAddress {
		var net = Dictionary.new;
		// thisProcess.openUDPPort(8080);

		net.put( \test,	NetAddr("127.0.0.1", 57120) );
		/*
		this.accounts.do({ |profil|
		// (this.name.asString != profil.key.asString).if({
		net.put(
		profil.key.asSymbol,
		NetAddr(profil.value.asString, NetAddr.langPort)
		// NetAddr(profil.value.asString, 8080)
		);
		// });
		});
		*/
		^net;
	}
*/


	prInitSendMsg {
		var events = ();
		var sender = this.prGetLibrary.at(\userName).asSymbol;
		var broadcastAddr = NetAddr("255.255.255.255", NetAddr.langPort);

		events.getMyIP = {|event| broadcastAddr.sendMsg('/user/getMyIP'); };

		events.join = {|event| broadcastAddr.sendMsg('/user/join', sender); };

		/*
		events.join = {|event| net.keysValuesDo {|key, target|
			target.sendMsg('/user/join', name.asSymbol);
		}};
		*/

		events.connect = {|event|
			var broadcastAddr2 = this.prGetLibrary.at(\broadcastNet);
		("broadcastAddr2:" + broadcastAddr2).postln;
			broadcastAddr2.sendMsg('/user/connect', sender);

		};
		// events.connect = {|event| this.prGetLibrary.at(\broadcastNet).sendMsg('/user/connect', name.asSymbol) };

		this.prGetLibrary.put(\events, events);
	}


	prInitReceiveMsg {

		OSCdef.newMatching(\msg_myIP, {|msg, time, addr, recvPort|
			var broadcastIP = addr.ip.split($.).put(3,255).join(".");
			("MyNetIP: " + addr.ip).postln;
			("broadcastIP: " + broadcastIP).postln;
			this.prGetLibrary.put(\broadcastNet, NetAddr.new(broadcastIP, 57120));

		},  '/user/getMyIP', nil).permanent_(true);


		OSCdef.newMatching(\msg_join, {|msg, time, addr, recvPort|
			var msgType = msg[0];
			var sender = msg[1];
			"Player % has joined to session".format(sender).warn;
			// events.aliveAnsw(sender.asSymbol);
			// events.clockTempoSet(currentEnvironment[\tempo].clock.tempo*60);

		}, '/user/join', nil).permanent_(true);

		OSCdef.newMatching(\msg_test, {|msg, time, addr, recvPort|
			var msgType = msg[0];
			var sender = msg[1];
			"Player % send broadcast Msg".format(sender).warn;
			// events.aliveAnsw(sender.asSymbol);
			// events.clockTempoSet(currentEnvironment[\tempo].clock.tempo*60);

		}, '/user/connect', nil).permanent_(true);
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
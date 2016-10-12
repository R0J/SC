+ ProxySpace {

	pozdrav {
		"ahoj".postln;
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
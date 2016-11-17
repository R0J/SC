+ NodeProxy {

	post {
		var library = this.prGetLibrary;
		{
			0.1.wait;
			if(library.notNil) {
				var envFolder = library.atPath([this.envirKey.asSymbol, \envelopes]);
				var cyclesFolder = library.atPath([this.envirKey.asSymbol, \cycles]);
				var stageFolder = library.atPath([this.envirKey.asSymbol, \stages]);

				"\n\nNodeProxy qMachine post \n-----------------------".postln;

				if(envFolder.notNil) {
					"envelopes:".postln;
					library.atPath([this.envirKey.asSymbol,\envelopes]).sortedKeysValuesDo({|oneControlNames|
						("\t \\" ++ oneControlNames).postln;
						library.atPath([this.envirKey.asSymbol,\envelopes, oneControlNames.asSymbol]).sortedKeysValuesDo({|oneEnvName|
							var oneEnv = library.atPath([this.envirKey.asSymbol, \envelopes, oneControlNames.asSymbol, oneEnvName.asSymbol]);
							("\t\t \\" ++ oneEnvName ++ " -> NodeEnv [ dur:" + oneEnv.duration + "]").postln;
							oneEnv.print(3);
						});
					});
				};

				if(cyclesFolder.notNil) {
					"cycles:".postln;
					cyclesFolder.sortedKeysValuesDo({|oneCycleNames|
						var oneCycle = library.atPath([this.envirKey.asSymbol,\cycles] ++ oneCycleNames.asSymbol);
						("\t\\" ++ oneCycleNames ++ " -> NodeCycle [ dur:" + oneCycle.timeline.duration + "]").postln;
						oneCycle.timeline.print(2);
					});
				};

				if(stageFolder.notNil) {
					"stages:".postln;
					stageFolder.sortedKeysValuesDo({|oneStageNames|
						var oneStage = library.atPath([this.envirKey.asSymbol,\stages] ++ oneStageNames.asSymbol);
						("\t\\" ++ oneStageNames ++ " -> NodeStage [ dur:" + oneStage.timeline.duration + "]").postln;
						oneStage.timeline.print(2);
					});
				};
			};
		}.fork;
	}

	env { |controlName, envelopeName = nil|
		var nEnv = nil;

		NodeComposition.addNode(this);

		if(controlName.notNil) {
			// var library = this.prGetLibrary(controlName);

			if(envelopeName.isNil,
				{ nEnv = NodeEnv(this.envirKey, controlName.asSymbol, \default); },
				{ nEnv = NodeEnv(this.envirKey, controlName.asSymbol, envelopeName.asSymbol); }
			);

			NodeComposition.addEnvelope(nEnv);
			// NodeComposition.addBus(this, controlName);
			};
		// this.post;
		^nEnv;
	}

	cycle { |cycleName = nil|
		// var library = this.prGetLibrary;
		var nCycle;
		if(cycleName.isNil) { cycleName = \default; };
		// this.post;
		nCycle = NodeCycle(this.envirKey, cycleName.asSymbol);
		NodeComposition.addCycle(nCycle);
		^nCycle;
	}

	stage { |stageName = nil|
		// var library = this.prGetLibrary;
		var nStage;
		if(stageName.isNil) { stageName = \default; };
		// this.post;
		// ^NodeStage(this.envirKey, stageName.asSymbol);
		nStage = NodeStage(this.envirKey, stageName.asSymbol);
		NodeComposition.addStage(nStage);
		^nStage;
	}

/*
	prGetLibrary {|controlName = nil|
		var library = Library.at(\qMachine);

		if(library.isNil) {
			Library.put(\qMachine, MultiLevelIdentityDictionary.new);
			library = Library.at(\qMachine);

			("NodeMap library qMachine prepared").postln;
		};

		if(library.at(\node).isNil)
		{
			library.put(this.envirKey.asSymbol, \node, this);
		};

		if(controlName.notNil)
		{
			var busPath = [this.envirKey.asSymbol, \buses, controlName.asSymbol];
			if(library.atPath(busPath).isNil)
			{
				library.putAtPath(busPath, Bus.control(Server.default, 1));
			};
		};

		^library;
	}
*/

}
















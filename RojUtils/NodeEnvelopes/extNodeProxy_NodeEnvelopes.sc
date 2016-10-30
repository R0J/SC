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

	env { |controlName = nil|
		var library = this.prGetLibrary;
		var nEnv = nil;
		var nCycle, nStage;

		if(controlName.notNil) {
			nEnv = NodeEnv(this.envirKey, controlName.asSymbol, \default);
			nCycle = NodeCycle(this.envirKey, \default);
			nStage = NodeStage(this.envirKey, \default);
		};
		this.post;
		^nEnv;
	}

	cycle { |cycleName = nil|
		var library = this.prGetLibrary;
		if(cycleName.isNil) { cycleName = \default; };
		this.post;
		^NodeCycle(this.envirKey, cycleName.asSymbol);
	}

	stage { |stageName = nil|
		var library = this.prGetLibrary;
		if(stageName.isNil) { stageName = \default; };
		this.post;
		^NodeStage(this.envirKey, stageName.asSymbol);
	}


	prGetLibrary {
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

		this.controlKeys.do({|oneControlName|
			var busPath = [this.envirKey.asSymbol, \buses, oneControlName.asSymbol];
			if(library.atPath(busPath).isNil)
			{
				library.putAtPath(busPath, Bus.control(Server.default, 1));
			};
		});

		^library;
	}


}
















NodeEnv {

	var <nodeName, <controlName, <envelopeName;

	var <>envelope;
	var synth, controlBusIndex;

	var <buffer;
	var controlRate;

	var <>setPlot = false;

	*new {|nodeName, controlName, envelopeName = \default|
		var nEnv = NodeComposition.getEnvelope(nodeName, controlName, envelopeName);

		if(nEnv.isNil,
			{ ^super.newCopyArgs(nodeName.asSymbol, controlName.asSymbol, envelopeName.asSymbol).init; },
			{ ^nEnv; }
		);
	}

	init {
		var bufferSynthDef;
		var bufSynthName = nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName;

		controlRate = Server.default.sampleRate / Server.default.options.blockSize;

		bufferSynthDef = {|cBus, bufnum, startTime = 0|
			var controlRate = Server.default.sampleRate / Server.default.options.blockSize;
			var buf = PlayBuf.kr(
				numChannels: 1,
				bufnum: bufnum,
				startPos: startTime * controlRate,
				rate: \tempoClock.kr(1),
				loop: 0
			);
			FreeSelfWhenDone.kr(buf);
			Out.kr(cBus,buf * \multiplicationBus.kr(1));
		};
		bufferSynthDef.asSynthDef(name:bufSynthName.asSymbol).add;

		envelope = nil;
		synth = nil;

		NodeComposition.addBus(this);
	}

	set {|env, fixDuration = nil|

		var node = NodeComposition.getNode(nodeName);

		this.envelope = env;
		if(fixDuration.notNil) { this.fixDur(fixDuration); };

		buffer = Buffer.alloc(
			server: Server.default,
			numFrames: (controlRate * this.duration).ceil,
			numChannels: 1,
		);
		buffer.loadCollection(this.envelope.asSignal((controlRate * this.duration).ceil));

		controlBusIndex = NodeComposition.getBus(nodeName, controlName);
		node.set(controlName.asSymbol, BusPlug.for(controlBusIndex));

		if(this.setPlot) { this.plot; };
		NodeComposition.updateTimes(nodeName, controlName, envelopeName);
		NodeComposition.library.postTree;
		^this;
	}

	remove {
		var path = [nodeName.asSymbol, \envelopes, controlName.asSymbol, envelopeName.asSymbol];
		NodeComposition.library.removeEmptyAtPath(path);

		path = [nodeName.asSymbol, \envelopes, controlName.asSymbol];
		if(NodeComposition.librar.atPath(path).isNil) {
			path = [nodeName.asSymbol, \buses, controlName.asSymbol];
			NodeComposition.library.atPath(path).free;
			NodeComposition.library.removeEmptyAtPath(path);
		};
		buffer.free;
		^nil;
	}

	duration {
		if(this.envelope.notNil,
			{ ^this.envelope.duration; },
			{ ^nil; }
		);
	}

	fixDur {|dur|
		if(this.envelope.notNil,
			{
				var envDur = this.duration;
				case
				{ dur < envDur } { this.set(this.envelope.crop(0, dur)); }
				{ dur.asSymbol == envDur.asSymbol } {  }
				{ dur > envDur } { this.set(this.envelope.extend(dur)); };

				if(this.setPlot) { this.plot; };
			},
			{ ("Envelope of" + nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName + "is not defined").warn; }
		);
		^this;
	}

	print { |cntTabs = 0|
		var txt = "";
		var tabs = "";
		cntTabs.do({tabs = tabs ++ "\t"});

		if(envelope.notNil, {
			txt = txt ++ tabs ++ "- levels:" + envelope.levels ++ "\n";
			txt = txt ++ tabs ++ "- times:" + envelope.times ++ "\n";
			txt = txt ++ tabs ++ "- curves:" + envelope.curves ++ "\n";
		}, {
			txt = tabs ++ "Env (nil)\n";
		});

		txt.postln;
	}

	printOn { |stream| stream << this.class.name << " [\\" << controlName << ", \\" << envelopeName << ", dur:" << this.duration << "]"; }

	plot {|size = 400|
		var plotName = nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName;
		if(envelope.notNil,
			{ envelope.plotNamedEnv(plotName.asSymbol, size); },
			{ ("Envelope of" + nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName + "is not defined").warn; }
		);
		^this;
	}

	trig {|targetGroup, targetBus|
		var bufSynthName = nodeName ++ "_" ++ controlName ++ "_" ++ envelopeName;

		synth = Synth(bufSynthName.asSymbol, [
			\cBus: controlBusIndex,
			\bufnum: buffer.bufnum,
			\startTime, 0,
			\tempoClock, currentEnvironment.clock.tempo,
			\multiplicationBus, targetBus.asMap
		], targetGroup);

		// ("Synth trig to controlBus_" ++ controlBusIndex + "by synth:" + synth.nodeID + "env:" + this.print(1)).postln;
	}
}
+ NodeProxy {

	sdef { |controlName, index|
		// this.key.postln;
		// this.nodeMap.postln;
		var name = [this.key.asSymbol, controlName.asSymbol];
		var sDef = Sdef(name);
		this.map(controlName.asSymbol, sDef.kr);
		^Sdef(name,index);
	}
}


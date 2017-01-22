+ NodeProxy {

	sdef { |controlName, index|
		var name = [this.key.asSymbol, controlName.asSymbol];
		var sDef = Sdef(name);
		sDef.setNode(this, controlName);
		^Sdef(name,index);
	}
}


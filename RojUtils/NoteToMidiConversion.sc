Note {
	*new{|noteArray|
		var list = List.new();
		noteArray.asArray.do({|oneSymbol| list.add(Note.midiConversion(oneSymbol)); })
		^list.asArray;
	}

	*midiConversion {|symbol|
		var midi;
		case
		{symbol.asSymbol == \c} { midi = 61.midicps; }
		{symbol.asSymbol == \cc} { midi = 62.midicps; }
		{symbol.asSymbol == \d} { midi = 63.midicps; }
		{symbol.asSymbol == \e} { midi = 64.midicps; }
		{symbol.asSymbol == \f} { midi = 65.midicps; }
		{symbol.asSymbol == \ff} { midi = 66.midicps; }
		{symbol.asSymbol == \g} { midi = 67.midicps; }
		{symbol.asSymbol == \gg} { midi = 68.midicps; }
		{symbol.asSymbol == \a} { midi = 69.midicps; }
		{symbol.asSymbol == \aa} { midi = 70.midicps; }
		{symbol.asSymbol == \b} { midi = 71.midicps; } ;
		^midi;
	}

}
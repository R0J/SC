Table {
	var <names;
	var columns;
	var cntRows, cntColumns;

	*new {|...colNames| ^super.new.init(colNames) }

	init {|columnNames|
		names = columnNames;
		cntRows = columnNames.size;
		cntColumns = -1;
		columns = IdentityDictionary.new;
		columnNames.do({|name| columns.put(name,  Order.new) });
	}

	size { ^[cntRows, cntColumns] }

	lines { ^(cntColumns + 1) }
	// names { ^columns.keys }

	put { |line, name, data|
		var order = columns.at(name.asSymbol);
		order.put(line, data);
		if(line > cntColumns) { cntColumns = line };
	}

	putLine { |line ... data|
		names.do({|oneKey, i|
			// "putLine line:% | oneKey: % | data: %".format(line, oneKey, data[i]).postln;
			this.put(line, oneKey, data[i]);
		})
	}

	addLine {|...data|
		var line = cntColumns + 1;
		names.do({|oneKey, i|
			// "putLine line:% | oneKey: % | data: %".format(line, oneKey, data[i]).postln;
			this.put(line, oneKey, data[i]);
		})
	}

	get { |name, line|
		var order = columns.at(name.asSymbol);
		^order.at(line);
	}

	getLine { |line|
		var list = List.new;
		names.do({|name| list.add(this.get(name, line)) });
		^list.asArray;
	}

	getName { |name|
		var list = Array.fill(this.lines, nil);
		columns.at(name.asSymbol).indicesDo({|data, line|
			// "line: %  data: %".format(line, data).postln;
			list.put(line, data);
		});
		^list;
	}

	print {
		"\nTable\nn:  %".format(this.names.asArray).postln;
		(cntColumns + 1).do({|line| "%:  %".format(line, this.getLine(line)).postln; });
	}

	printOn { |stream|	stream << this.class.name << "(" << cntRows << ", "<< if(cntColumns < 0) { "nil" } { cntColumns } << ")"; }

}
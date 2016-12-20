Table {
	var columns;
	var cntRows, cntColumns;

	*new {|...colNames| ^super.new.init(colNames) }

	init {|columnNames|
		cntRows = columnNames.size;
		cntColumns = -1;
		columns = IdentityDictionary.new;
		columnNames.do({|name| columns.put(name,  Order.new) });
	}

	size { ^[cntRows, cntColumns] }

	names { ^columns.keys }

	put { |name, line, data|
		var order = columns.at(name.asSymbol);
		order.put(line, data);
		if(line > cntColumns) { cntColumns = line };
	}

	putLine { |line ... data|
		columns.keys.do({|oneKey, i|
			// "putLine line:% | oneKey: % | data: %".format(line, oneKey, data[i]).postln;
			this.put(oneKey, line, data[i]);
		})
	}

	addLine {|...data|
		var line = cntColumns + 1;
		columns.keys.do({|oneKey, i|
			// "putLine line:% | oneKey: % | data: %".format(line, oneKey, data[i]).postln;
			this.put(oneKey, line, data[i]);
		})
	}

	get { |name, line|
		var order = columns.at(name.asSymbol);
		^order.at(line);
	}

	getLine { |line|
		var list = List.new;
		this.names.do({|name| list.add(this.get(name, line)) });
		^list.asArray;
	}

	getName { |name|
		var list = Array.fill(cntColumns, nil);
		columns[name.asSymbol].indicesDo({|data, line| list.put(line-1, data) });
		^list;
	}

	print {

		"\nTable.print\nnames: %".format(this.names.asArray).postln;
		(cntColumns + 1).do({|line| "%:  %".format(line, this.getLine(line)).postln; });
	}

	printOn { |stream|	stream << this.class.name << "(" << cntRows << ", "<< if(cntColumns < 0) { "nil" } { cntColumns } << ")"; }

}
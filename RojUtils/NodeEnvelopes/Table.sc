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
		order.put((line + 1), data);
		if(line > cntColumns) { cntColumns = line };
	}

	putLine { |line ... data|
		var arrData = Array.newFrom(data.flatten);
		columns.keys.do({|oneKey, i|
			// "putLine line:% | oneKey: % | data: %".format(line, oneKey, arrData[i]).postln;
			this.put(oneKey, line, arrData[i]);
		})
	}

	addLine {|...data|
		var arrData = Array.newFrom(data);
		this.putLine(cntColumns, arrData);
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

	print {	(cntColumns + 1).do({|line| this.getLine(line).postln; }); }

}
Table {
	var columns;
	var cntRows, cntColumns;

	*new {|...colNames| ^super.new.init(colNames) }

	init {|columnNames|
		cntRows = columnNames.size;
		cntColumns = 0;
		columns = IdentityDictionary.new;
		columnNames.postln;
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
		var arrData = Array.newFrom(data.flatten);
		columns.keys.do({|oneKey, i|
			// "putLine line:% | oneKey: % | data: %".format(line, oneKey, arrData[i]).postln;
			this.put(oneKey, line, arrData[i]);
		})
	}

	addLine {|...data|
		var arrData = Array.newFrom(data.flatten);
		cntColumns.postln;
		arrData.postln;
		this.putLine(cntColumns + 1, arrData);
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

}
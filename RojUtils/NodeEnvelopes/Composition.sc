Composition {

	classvar timeline;
	classvar currentStage;

	*initClass {
		timeline = Timeline.new();
		currentStage = \default;
	}

	*stage {|stageName|
		if(StageDef.exist(stageName))
		{ currentStage = stageName; }
		{ "StageDef ('%') not found".format(stageName).warn; }
	}


}
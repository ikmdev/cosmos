package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.constellation.Step;

public class SemanticChartProcessor implements ChartProcessor {

	@Override
	public Step getStep() {
		return Step.PROCESS_SEMANTICS;
	}

	@Override
	public String getProcessorName() {
		return "Semantic Charting";
	}

	@Override
	public void process(ChartingContext chartContext, int batchSize) {

	}
}

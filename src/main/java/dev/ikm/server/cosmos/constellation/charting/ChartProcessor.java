package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.constellation.Chart;

public interface ChartProcessor {


	String getProcessorName();

	void process(ChartingContext chartingContext);

}

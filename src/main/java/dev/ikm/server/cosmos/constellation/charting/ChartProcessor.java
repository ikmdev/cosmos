package dev.ikm.server.cosmos.constellation.charting;

import dev.ikm.server.cosmos.constellation.Step;
import dev.ikm.server.cosmos.ike.Facade;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Gatherers;

public interface ChartProcessor {

	Step getStep();

	String getProcessorName();

	void process(ChartingContext chartContext, int batchSize);

}

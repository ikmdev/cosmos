package dev.ikm.server.cosmos.constellation;

public interface ChartProcessor {

	ChartStep getStep();

	String getProcessorName();

	void process(ChartingContext chartContext, int batchSize);
}

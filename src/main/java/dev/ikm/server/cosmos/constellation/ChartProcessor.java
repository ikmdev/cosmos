package dev.ikm.server.cosmos.constellation;

public interface ChartProcessor {

	Step getStep();

	String getProcessorName();

	void process(ChartingContext chartContext, int batchSize);
}

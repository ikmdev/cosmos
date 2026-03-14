package dev.ikm.server.cosmos.constellation.charting;

import org.springframework.stereotype.Component;

@Component
public class DeleteChartProcessor implements ChartProcessor {

	@Override
	public String getProcessorName() {
		return "Delete Chart Processor";
	}

	@Override
	public void process(ChartingContext chartingContext) {
        // Here you would implement the logic to delete charts based on the charting context.
        // This is a placeholder implementation.
        chartingContext.progressUpdate().accept(100); // Update progress to 100% as an example
    }

}

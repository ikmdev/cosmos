package dev.ikm.server.cosmos.constellation;

import dev.ikm.tinkar.common.id.IntIdList;
import dev.ikm.tinkar.coordinate.navigation.calculator.NavigationCalculator;

import java.util.List;

public class HierarchyCharProcessor implements ChartProcessor{

	@Override
	public Step getStep() {
		return Step.PROCESS_HIERARCHY;
	}

	@Override
	public String getProcessorName() {
		return "Hierarchy Charting";
	}

	@Override
	public void process(ChartingContext chartContext, int batchSize) {
		NavigationCalculator navigationCalculator = chartContext.getChart().navigationCalculator();
		chartContext.getScopedConcepts().values()
				.stream()
				.flatMap(List::stream)
				.forEach(nid ->{
					navigationCalculator.parentsOf(nid).forEach(parentNid -> {

					});
				});
	}
}

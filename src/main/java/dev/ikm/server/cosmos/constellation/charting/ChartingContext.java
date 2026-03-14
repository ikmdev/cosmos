package dev.ikm.server.cosmos.constellation.charting;

import java.util.function.Consumer;

import dev.ikm.server.cosmos.constellation.Chart;

public record ChartingContext(Chart chart, Consumer<Integer> progressUpdate, int batchSize) {

}

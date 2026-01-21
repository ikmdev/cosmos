package dev.ikm.server.cosmos.api.plausability;

import dev.ikm.server.cosmos.api.coordinate.CalculatorService;
import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PlausibilityService {

	private final IkeRepository ikeRepository;
	private final CalculatorService calculatorService;

	//Concept to use for Plausibility
	private static final Concept ROCHE_COBAS_ALBUMIN_GEN2 = Concept.make(PublicIds.of(UUID.fromString("74b4fc01-2183-5bf6-b9ab-46dd8a8536ee")));

	//Patterns Dex uses
	private static final EntityProxy.Pattern TEST_PERFORMED_PATTERN = EntityProxy.Pattern.make(PublicIds.of(UUID.fromString("4289dbff-1557-4a1d-873a-7092b328615d")));
	private static final EntityProxy.Pattern POPULATION_REFERENCE_RANGE_PATTERN = EntityProxy.Pattern.make(PublicIds.of(UUID.fromString("e90e9d7a-0d75-40ea-b259-6b9b0d1b9b23")));

	//Components dealing with Roche Cobas Device where values come from
	private static final Semantic ROCHE_COBAS_TEST_PERFORMED = Semantic.make(PublicIds.of(UUID.fromString("ff593d26-9dce-4787-9f57-56a60b5bdb0b")));
	private static final Semantic ROCHE_COBAS_POP_REF_NEWBORN = Semantic.make(PublicIds.of(UUID.fromString("fad9088e-08e3-4573-91fe-cc37a9532a2e")));
	private static final Semantic ROCHE_COBAS_POP_REF_CHILD = Semantic.make(PublicIds.of(UUID.fromString("e2ed45cb-3471-4144-9051-a272af85a199")));
	private static final Semantic ROCHE_COBAS_POP_REF_TEEN = Semantic.make(PublicIds.of(UUID.fromString("ff9b2c74-6e85-4a61-b64a-807084d972f7")));
	private static final Semantic ROCHE_COBAS_POP_REF_ADULT = Semantic.make(PublicIds.of(UUID.fromString("e9dbe91e-84e2-49a1-8e4f-3e3f2e7b91b4")));

	//Field Meanings to extract values from
	private static final Concept POP_REF_RANGE_MIN_MEANING = Concept.make(PublicIds.of(UUID.fromString("1deaa2ff-0590-494e-b5ac-df68f4b9f03d")));
	private static final Concept POP_REF_RANGE_MAX_MEANING = Concept.make(PublicIds.of(UUID.fromString("ff70ecd3-4d49-4846-8d3b-7310064c375e")));
	private static final Concept POP_REF_RANGE_POPULATION_MEANING = Concept.make(PublicIds.of(UUID.fromString("b5ad02e2-017f-4d24-8354-6d00347eceb4")));
	private static final Concept POP_REF_RANGE_UCUM_UNITS_MEANING = Concept.make(PublicIds.of(UUID.fromString("80cd4978-314d-46e3-bc13-9980280ae955")));
//	private static final EntityProxy.Concept TEST_PERF_CODE_MEANING = EntityProxy.Concept.make(PublicIds.of(UUID.fromString("542ce1f0-68bf-4a9b-bd7b-9d585a69206b")));
//	private static final EntityProxy.Concept TEST_PERF_DETECTION_LIMIT_MEANING = EntityProxy.Concept.make(PublicIds.of(UUID.fromString("b8004f41-5c65-40a8-9b39-55502195e7f0")));

	private enum PopAge {
		NEWBORN("Newborn 0-4 days"),
		CHILD("Children (4 days-14 years)"),
		TEEN("Children (14-18 years)"),
		ADULT("Adults");

		private final String value;

		PopAge(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	@Autowired
	public PlausibilityService(IkeRepository ikeRepository, CalculatorService calculatorService) {
		this.ikeRepository = ikeRepository;
		this.calculatorService = calculatorService;
	}

	public PlausibilityResult calculateLabResultPlausibility(String dob, String testCode, String resultValue) {
		if (!testCode.equals("61151-7")) {
			return new PlausibilityResult(Plausibility.UNKNOWN);
		}

		PopAge populationAgeRange = convertDoBToPopulationHealthAgeRange(dob);

		Plausibility plausibility = switch (populationAgeRange) {
			case NEWBORN -> evaluateResultValueAgainstRanges(resultValue, ROCHE_COBAS_POP_REF_NEWBORN);
			case CHILD -> evaluateResultValueAgainstRanges(resultValue, ROCHE_COBAS_POP_REF_CHILD);
			case TEEN -> evaluateResultValueAgainstRanges(resultValue, ROCHE_COBAS_POP_REF_TEEN);
			case ADULT -> evaluateResultValueAgainstRanges(resultValue, ROCHE_COBAS_POP_REF_ADULT);
		};

		return new PlausibilityResult(plausibility);
	}

	private Plausibility evaluateResultValueAgainstRanges(String resultValue, Semantic popSemantic) {
		Latest<SemanticEntityVersion> latestPopRefSemantic = calculatorService.getStampCalculator().latest(popSemantic);
		String maxString = latestPopRefSemantic.get().fieldAsString(3);
		String minString = latestPopRefSemantic.get().fieldAsString(4);

		double value = Double.parseDouble(resultValue);
		double min = Double.parseDouble(minString);
		double max = Double.parseDouble(maxString);

		if (min <= value && value <= max) {
			return Plausibility.PLAUSIBLE;
		} else {
			return Plausibility.IMPLAUSIBLE;
		}
	}

	public PlausibilityResult calculateLabDevicePlausibility(String testCode, String refRangeLow, String refRangeHigh, String unit) {
		return new PlausibilityResult(Plausibility.PLAUSIBLE);
	}

	private PopAge convertDoBToPopulationHealthAgeRange(String formattedDoB) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
		LocalDateTime dob = LocalDateTime.parse(formattedDoB, formatter);
		LocalDateTime now = LocalDateTime.now();

		long days = ChronoUnit.DAYS.between(dob, now);
		long years = ChronoUnit.YEARS.between(dob, now);

		if (days < 4) {
			return PopAge.NEWBORN;
		} else if (years < 14) {
			return PopAge.CHILD;
		} else if (years < 18) {
			return PopAge.TEEN;
		} else {
			return PopAge.ADULT;
		}
	}
}

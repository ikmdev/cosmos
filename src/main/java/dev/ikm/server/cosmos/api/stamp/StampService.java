package dev.ikm.server.cosmos.api.stamp;

import dev.ikm.server.cosmos.ike.IkeRepository;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StampService {

	private final IkeRepository ikeRepository;

	@Autowired
	public StampService(IkeRepository ikeRepository) {
		this.ikeRepository = ikeRepository;
	}

	public StampChronology retrieveStampWithAllVersions(UUID id) {
		Optional<StampEntity<? extends StampEntityVersion>> optionalStampEntity = ikeRepository.findSTAMPById(id);
		return new StampChronology(
				ikeRepository.getIds(id),
				null,
				optionalStampEntity.map(stampEntity ->
								stampEntity.versions().stream()
										.map(stampEntityVersion ->
												new StampVersion(
														ikeRepository.getIds(stampEntityVersion.state()),
														stampEntityVersion.time(),
														DateTimeUtil.format(stampEntity.time(), DateTimeUtil.SEC_FORMATTER),
														ikeRepository.getIds(stampEntityVersion.author()),
														ikeRepository.getIds(stampEntityVersion.module()),
														ikeRepository.getIds(stampEntityVersion.path())))
										.toList())
						.orElse(Collections.emptyList())
		);
	}

	public StampChronology retrieveStampWithLatestVersion(UUID id) {
		Latest<StampEntityVersion> latestStampEntityVersion = ikeRepository.findLatestSTAMPById(id);
		if (latestStampEntityVersion.isPresent()) {
			StampEntityVersion stampEntityVersion = latestStampEntityVersion.get();
			return new StampChronology(
					ikeRepository.getIds(id),
					new StampVersion(
							ikeRepository.getIds(stampEntityVersion.state()),
							stampEntityVersion.time(),
							DateTimeUtil.format(stampEntityVersion.time(), DateTimeUtil.SEC_FORMATTER),
							ikeRepository.getIds(stampEntityVersion.author()),
							ikeRepository.getIds(stampEntityVersion.module()),
							ikeRepository.getIds(stampEntityVersion.path())),
					null);
		} else {
			return new StampChronology(
					ikeRepository.getIds(id),
					null,
					null);
		}
	}

	public List<List<UUID>> retrieveSemantics(UUID id) {
		return ikeRepository.findAssociatedSemanticIds(id);
	}
}

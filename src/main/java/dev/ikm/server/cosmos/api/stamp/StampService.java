package dev.ikm.server.cosmos.api.stamp;

import dev.ikm.server.cosmos.database.Context;
import dev.ikm.server.cosmos.database.IkeRepository;
import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.util.time.DateTimeUtil;
import dev.ikm.tinkar.coordinate.stamp.calculator.Latest;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.StampEntityVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StampService {

	private final IkeRepository ikeRepository;
	private final Context context;

	@Autowired
	public StampService(IkeRepository ikeRepository, Context context) {
		this.ikeRepository = ikeRepository;
		this.context = context;
	}

	public StampChronologyDTO retrieveStampWithAllVersions(UUID uuid) {
		Optional<StampEntity<? extends StampEntityVersion>> optionalStampEntity = ikeRepository.findSTAMPById(PublicIds.of(uuid));
		return new StampChronologyDTO(
				PublicIds.of(uuid).asUuidList().toList(),
				null,
				optionalStampEntity.map(stampEntity ->
								stampEntity.versions().stream()
										.map(stampEntityVersion ->
												new StampVersionDTO(
														stampEntityVersion.state().publicId().asUuidList().toList(),
														stampEntityVersion.time(),
														DateTimeUtil.format(stampEntity.time(), DateTimeUtil.SEC_FORMATTER),
														stampEntityVersion.author().publicId().asUuidList().toList(),
														stampEntityVersion.module().publicId().asUuidList().toList(),
														stampEntityVersion.path().publicId().asUuidList().toList()))
										.toList())
						.orElse(Collections.emptyList())
		);
	}

	public StampChronologyDTO retrieveStampWithLatestVersion(UUID uuid) {
		Latest<StampEntityVersion> latestStampEntityVersion = ikeRepository.findLatestSTAMPById(PublicIds.of(uuid), context.getStampCalculator());
		if (latestStampEntityVersion.isPresent()) {
			StampEntityVersion stampEntityVersion = latestStampEntityVersion.get();
			return new StampChronologyDTO(
					PublicIds.of(uuid).asUuidList().toList(),
						new StampVersionDTO(
								stampEntityVersion.state().publicId().asUuidList().toList(),
								stampEntityVersion.time(),
								DateTimeUtil.format(stampEntityVersion.time(), DateTimeUtil.SEC_FORMATTER),
								stampEntityVersion.author().publicId().asUuidList().toList(),
								stampEntityVersion.module().publicId().asUuidList().toList(),
								stampEntityVersion.path().publicId().asUuidList().toList()),
					null);
		} else {
			return new StampChronologyDTO(
					PublicIds.of(uuid).asUuidList().toList(),
					null,
					null);
		}
	}

	public List<List<UUID>> retrieveSemantics(UUID uuid) {
		List<List<UUID>> stampIds = new ArrayList<>();
		PublicId conceptPublicId = PublicIds.of(uuid);
		PrimitiveData.get().forEachSemanticNidForComponent(
				Entity.nid(conceptPublicId),
				semanticNid -> {
					stampIds.add(PrimitiveData.publicId(semanticNid).asUuidList().toList());
				});
		return stampIds;
	}
}

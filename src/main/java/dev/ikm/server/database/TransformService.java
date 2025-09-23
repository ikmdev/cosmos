package dev.ikm.server.database;

public class TransformService {
//
//	private final ViewContext viewContext;
//
//	@Autowired
//	public TransformService(ViewContext viewContext) {
//		this.viewContext = viewContext;
//	}
//
//	public ConceptChronologyDTO conceptEntity(ConceptEntity<? extends ConceptEntityVersion> conceptEntity,
//											  boolean includeHistory,
//											  boolean isRecursive) {
//		String description = languageCalculator.getDescriptionTextOrNid(conceptEntity.nid());
//		Latest<ConceptEntityVersion> conceptEntityVersionLatest = stampCalculator.latest(conceptEntity.nid());
//		return new ConceptChronologyDTO(
//				publicId(conceptEntity.publicId()),
//				description,
//				conceptVersion(conceptEntityVersionLatest.get()),
//				includeHistory ?
//						conceptEntity.versions().stream()
//								.map(this::conceptVersion)
//								.toList()
//						:
//						null);
//	}
//
//	private ConceptVersionDTO conceptVersion(ConceptEntityVersion conceptEntityVersion) {
//		return new ConceptVersionDTO(stampChronology(conceptEntityVersion.stamp()));
//	}
//
//	public SemanticChronologyView semanticChronology(SemanticEntity<? extends SemanticEntityVersion> semanticEntity) {
//		Latest<SemanticEntityVersion> semanticEntityVersionLatest = stampCalculator.latest(semanticEntity.nid());
//		if (semanticEntity.referencedComponent() instanceof ConceptEntity<? extends ConceptEntityVersion> conceptEntity) {
//			return new SemanticChronologyView(
//					publicId(semanticEntity.publicId()),
//					patternChronology(semanticEntity.pattern()),
//					conceptEntity(conceptEntity),
//					null,
//					null,
//					null,
//					semanticVersion(semanticEntityVersionLatest.get()),
//					includeHistory ? semanticEntity.versions().stream()
//							.map(semanticEntityVersion -> new SemanticVersionView(
//									stampChronology(semanticEntityVersion.stamp()),
//									semanticEntityVersion.fieldValues().stream()
//											.map(this::field)
//											.toList()))
//							.toList()
//							: null);
//
//		} else if (semanticEntity.referencedComponent() instanceof PatternEntity<? extends PatternEntityVersion> patternEntity) {
//			return new SemanticChronologyView(
//					publicId(semanticEntity.publicId()),
//					patternChronology(semanticEntity.pattern()),
//					null,
//					patternChronology(patternEntity),
//					null,
//					null,
//					semanticVersion(semanticEntityVersionLatest.get()),
//					includeHistory ? semanticEntity.versions().stream()
//							.map(semanticEntityVersion -> new SemanticVersionView(
//									stampChronology(semanticEntityVersion.stamp()),
//									semanticEntityVersion.fieldValues().stream()
//											.map(this::field)
//											.toList()))
//							.toList()
//							: null);
//		} else if (semanticEntity.referencedComponent() instanceof StampEntity<? extends StampEntityVersion> stampEntity) {
//			return new SemanticChronologyView(
//					publicId(semanticEntity.publicId()),
//					patternChronology(semanticEntity.pattern()),
//					null,
//					null,
//					null,
//					stampChronology(stampEntity),
//					semanticVersion(semanticEntityVersionLatest.get()),
//					includeHistory ? semanticEntity.versions().stream()
//							.map(semanticEntityVersion -> new SemanticVersionView(
//									stampChronology(semanticEntityVersion.stamp()),
//									semanticEntityVersion.fieldValues().stream()
//											.map(this::field)
//											.toList()))
//							.toList()
//							: null);
//		} else if (semanticEntity.referencedComponent() instanceof SemanticEntity<? extends SemanticEntityVersion> semanticEntity1) {
//			return new SemanticChronologyView(
//					publicId(semanticEntity.publicId()),
//					patternChronology(semanticEntity.pattern()),
//					null,
//					null,
//					semanticChronology(semanticEntity1),
//					null,
//					semanticVersion(semanticEntityVersionLatest.get()),
//					includeHistory ? semanticEntity.versions().stream()
//							.map(semanticEntityVersion -> new SemanticVersionView(
//									stampChronology(semanticEntityVersion.stamp()),
//									semanticEntityVersion.fieldValues().stream()
//											.map(this::field)
//											.toList()))
//							.toList()
//							: null);
//		} else {
//			throw new IllegalStateException("Unexpected value: " + semanticEntity.referencedComponent());
//		}
//	}
//
//	private SemanticVersionView semanticVersion(SemanticEntityVersion semanticEntityVersion) {
//		return new SemanticVersionView(
//				stampChronology(semanticEntityVersion.stamp()),
//				semanticEntityVersion.fieldValues().stream()
//						.map(this::field)
//						.toList());
//	}
//
//	public Object field(Object value) {
//		return switch (value) {
//			case Integer i -> i;
//			case Long l -> l;
//			case Float f -> f;
//			case Double d -> d;
//			case String s -> s;
//			case Boolean b -> b;
//			case byte[] bytes -> Objects.requireNonNullElse(bytes, new byte[0]);
//			case EntityVertex entityVertex -> {
//				int meaningNid = Entity.nid(entityVertex.meaning());
//				Optional<Entity<EntityVersion>> optionalEntity = Entity.get(meaningNid);
//				if (optionalEntity.isEmpty()) {
//					yield null;
//				}
//				Entity<? extends EntityVersion> entity = optionalEntity.get();
//				yield new VertexFieldView(
//						entityVertex.asUuid(),
//						entityVertex.vertexIndex(),
//						conceptEntity((ConceptEntity<? extends ConceptEntityVersion>) entity),                                //object
//						entityVertex.properties().stream()
//								.map(object -> new PropertyFieldView(field(object)))
//								.toList()
//				);
//			}
//			case VertexId vertexId -> vertexId.asUuid();
//			case PublicId publicId -> {
//				int nid = Entity.nid(publicId);
//				Entity<? extends EntityVersion> entity = Entity.getFast(nid);
//				switch (entity) {
//					case ConceptEntity<? extends ConceptEntityVersion> conceptEntity -> {
//						yield conceptEntity(conceptEntity);
//					}
//					case PatternEntity<? extends PatternEntityVersion> patternEntity -> {
//						yield patternChronology(patternEntity);
//					}
//					case SemanticEntity<? extends SemanticEntityVersion> semanticEntity -> {
//						yield semanticChronology(semanticEntity);
//					}
//					case StampEntity<? extends StampEntityVersion> stampEntity -> {
//						yield stampChronology(stampEntity);
//					}
//					case null, default -> throw new IllegalStateException("Unexpected value: " + entity);
//				}
//			}                                        //object
//			case PublicIdList publicIdList -> publicIdList.stream()                                //object
//					.map(publicIdObj -> {
//						PublicId publicId = (PublicId) publicIdObj;
//						int nid = Entity.nid(publicId);
//						Entity<? extends EntityVersion> entity = Entity.getFast(nid);
//						switch (entity) {
//							case ConceptEntity<? extends ConceptEntityVersion> conceptEntity -> {
//								return conceptEntity(conceptEntity);
//							}
//							case PatternEntity<? extends PatternEntityVersion> patternEntity -> {
//								return patternChronology(patternEntity);
//							}
//							case SemanticEntity<? extends SemanticEntityVersion> semanticEntity -> {
//								return semanticChronology(semanticEntity);
//							}
//							case StampEntity<? extends StampEntityVersion> stampEntity -> {
//								return stampChronology(stampEntity);
//							}
//							case null, default -> throw new IllegalStateException("Unexpected value: " + entity);
//						}
//					})
//					.toList();
//			case PublicIdSet publicIdSet -> publicIdSet.stream()
//					.map(publicIdObj -> {
//						PublicId publicId = (PublicId) publicIdObj;
//						int nid = Entity.nid(publicId);
//						Entity<? extends EntityVersion> entity = Entity.getFast(nid);
//						switch (entity) {
//							case ConceptEntity<? extends ConceptEntityVersion> conceptEntity -> {
//								return conceptEntity(conceptEntity);
//							}
//							case PatternEntity<? extends PatternEntityVersion> patternEntity -> {
//								return patternChronology(patternEntity);
//							}
//							case SemanticEntity<? extends SemanticEntityVersion> semanticEntity -> {
//								return semanticChronology(semanticEntity);
//							}
//							case StampEntity<? extends StampEntityVersion> stampEntity -> {
//								return stampChronology(stampEntity);
//							}
//							case null, default -> throw new IllegalStateException("Unexpected value: " + entity);
//						}
//					})
//					.collect(Collectors.toSet());
//			case IntIdList intIdList -> intIdList
//					.map(intIdValue -> {
//						Entity<? extends EntityVersion> entity = Entity.getFast(intIdValue);
//						switch (entity) {
//							case ConceptEntity<? extends ConceptEntityVersion> conceptEntity -> {
//								return conceptEntity(conceptEntity);
//							}
//							case PatternEntity<? extends PatternEntityVersion> patternEntity -> {
//								return patternChronology(patternEntity);
//							}
//							case SemanticEntity<? extends SemanticEntityVersion> semanticEntity -> {
//								return semanticChronology(semanticEntity);
//							}
//							case StampEntity<? extends StampEntityVersion> stampEntity -> {
//								return stampChronology(stampEntity);
//							}
//							case null, default -> throw new IllegalStateException("Unexpected value: " + entity);
//						}
//					})
//					.toList();
//			case IntIdSet intIdSet -> intIdSet
//					.map(intIdValue -> {
//						Entity<? extends EntityVersion> entity = Entity.getFast(intIdValue);
//						switch (entity) {
//							case ConceptEntity<? extends ConceptEntityVersion> conceptEntity -> {
//								return conceptEntity(conceptEntity);
//							}
//							case PatternEntity<? extends PatternEntityVersion> patternEntity -> {
//								return patternChronology(patternEntity);
//							}
//							case SemanticEntity<? extends SemanticEntityVersion> semanticEntity -> {
//								return semanticChronology(semanticEntity);
//							}
//							case StampEntity<? extends StampEntityVersion> stampEntity -> {
//								return stampChronology(stampEntity);
//							}
//							case null, default -> throw new IllegalStateException("Unexpected value: " + intIdValue);
//						}
//					})
//					.toSet();
//			case Instant instant -> DateTimeUtil.format(instant, DateTimeUtil.SEC_FORMATTER);
//			case BigDecimal bigDecimal -> bigDecimal.toPlainString();
//			case Component component -> switch (component) {
//				case ConceptEntity conceptEntity -> conceptEntity(conceptEntity);
//				case PatternEntity patternEntity -> patternChronology(patternEntity);
//				case SemanticEntity semanticEntity -> semanticChronology(semanticEntity);
//				case StampEntity stampEntity -> stampChronology(stampEntity);
//				default -> throw new IllegalStateException("Unexpected value: " + component);
//			};
//			case DiTreeEntity diTreeEntity -> new DiTreeFieldView(
//					diTreeEntity.vertexMap().stream()
//							.map(entityVertex -> (VertexFieldView) field(entityVertex))
//							.toList(),
//					diTreeEntity.root().vertexIndex(),
//					diTreeEntity.predecessorMap(),
//					diTreeEntity.successorMap()
//			);
//			case DiGraphEntity<? extends EntityVertex> diGraphEntity -> new DiGraphFieldView(
//					diGraphEntity.vertexMap().stream()
//							.map(entityVertex -> (VertexFieldView) field(entityVertex))
//							.toList(),
//					diGraphEntity.roots().stream()
//							.map(entityVertex -> entityVertex.vertexIndex())
//							.toList(),
//					diGraphEntity.successorMap(),
//					diGraphEntity.predecessorMap());
//			case PlanarPoint planarPoint -> new PlanarPointFieldView(planarPoint.x(), planarPoint.y());
//			case SpatialPoint spatialPoint ->
//					new SpatialPointFieldView(spatialPoint.x(), spatialPoint.y(), spatialPoint.z());
//			default -> value;
//		};
//	}
//
//	public PatternChronologyView patternChronology(PatternEntity<? extends PatternEntityVersion> patternEntity) {
//		Latest<PatternEntityVersion> patternEntityVersionLatest = stampCalculator.latest(patternEntity.nid());
//		return new PatternChronologyView(
//				publicId(patternEntity.publicId()),
//				languageCalculator.getDescriptionTextOrNid(patternEntity.nid()),
//				patternVersion(patternEntityVersionLatest.get()),
//				includeHistory ? patternEntity.versions().stream()
//						.map(this::patternVersion)
//						.toList()
//						: null);
//	}
//
//	public PatternVersionView patternVersion(PatternEntityVersion patternEntityVersion) {
//		return new PatternVersionView(
//				stampChronology(patternEntityVersion.stamp()),
//				conceptEntity(patternEntityVersion.semanticMeaning()),
//				conceptEntity(patternEntityVersion.semanticPurpose()),
//				fieldDefinitions(patternEntityVersion.fieldDefinitions()));
//	}
//
//	public List<FieldDefinitionView> fieldDefinitions(ImmutableList<? extends FieldDefinitionForEntity> fieldDefinitions) {
//		return fieldDefinitions.stream()
//				.map(fieldDefinitionForEntity -> new FieldDefinitionView(
//						conceptEntity(fieldDefinitionForEntity.dataType()),
//						conceptEntity(fieldDefinitionForEntity.meaning()),
//						conceptEntity(fieldDefinitionForEntity.purpose()),
//						fieldDefinitionForEntity.indexInPattern()))
//				.toList();
//	}
//
//	public PublicIdView publicId(PublicId publicId) {
//		List<UUID> uuids = publicId.asUuidList().stream().toList();
//		return new PublicIdView(uuids);
//	}
//
//	public StampChronologyView stampChronology(StampEntity<? extends StampEntityVersion> stampEntity) {
//		Latest<StampEntityVersion> stampEntityVersionLatest = stampCalculator.latest(stampEntity.nid());
//		return new StampChronologyView(
//				publicId(stampEntity.publicId()),
//				stampVersion(stampEntityVersionLatest.get()),
//				includeHistory ? stampEntity.versions().stream()
//						.map(this::stampVersion)
//						.toList()
//						: null);
//	}
//
//	public StampVersionView stampVersion(StampEntityVersion stampEntityVersion) {
//		Entity<? extends EntityVersion> statusEntity = Entity.get(stampEntityVersion.state().nid()).orElseThrow();
//		Entity<? extends EntityVersion> authorEntity = Entity.get(stampEntityVersion.author().nid()).orElseThrow();
//		Entity<? extends EntityVersion> moduleEntity = Entity.get(stampEntityVersion.module().nid()).orElseThrow();
//		Entity<? extends EntityVersion> pathEntity = Entity.get(stampEntityVersion.path().nid()).orElseThrow();
//		if (isRecursive) {
//			return new StampVersionView(
//					conceptEntity((ConceptEntity<? extends ConceptEntityVersion>) statusEntity),
//					null,
//					null,
//					stampEntityVersion.time(),
//					DateTimeUtil.format(stampEntityVersion.time(), DateTimeUtil.SEC_FORMATTER),
//					conceptEntity((ConceptEntity<? extends ConceptEntityVersion>) authorEntity),
//					null,
//					null,
//					conceptEntity((ConceptEntity<? extends ConceptEntityVersion>) moduleEntity),
//					null,
//					null,
//					conceptEntity((ConceptEntity<? extends ConceptEntityVersion>) pathEntity),
//					null,
//					null);
//		} else {
//			return new StampVersionView(
//					null,
//					publicId(statusEntity.publicId()),
//					languageCalculator.getDescriptionTextOrNid(statusEntity.nid()),
//					stampEntityVersion.time(),
//					DateTimeUtil.format(stampEntityVersion.time(), DateTimeUtil.SEC_FORMATTER),
//					null,
//					publicId(authorEntity.publicId()),
//					languageCalculator.getDescriptionTextOrNid(authorEntity.nid()),
//					null,
//					publicId(moduleEntity.publicId()),
//					languageCalculator.getDescriptionTextOrNid(moduleEntity.nid()),
//					null,
//					publicId(pathEntity.publicId()),
//					languageCalculator.getDescriptionTextOrNid(pathEntity.nid()));
//		}
//	}
}

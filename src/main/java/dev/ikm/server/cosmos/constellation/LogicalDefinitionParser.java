package dev.ikm.server.cosmos.constellation;

import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.TinkarTermV2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LogicalDefinitionParser {

	public record LogicalDefinition(long sufficientCount, long necessaryCount, List<Definition> definitions) {

	}

	public record Definition(UUID id, Type type, List<RoleGroup> roleGroups, List<Role> roles,
							 List<Reference> references) {
	}

	public record RoleGroup(UUID id, List<Role> roles) {
	}

	public record Role(Concept predicate, Concept object) {
	}

	public record Reference(Concept concept) {
	}

	public enum SubType {
		ROLE_GROUP,
		ROLE,
		Reference
	}

	public enum Type {
		NECESSARY,
		SUFFICIENT
	}

	private final DiTreeEntity diTreeEntity;

	public LogicalDefinitionParser(DiTreeEntity diTreeEntity) {
		this.diTreeEntity = diTreeEntity;
	}

	public LogicalDefinition parse() {
		List<Definition> definitions = processDiTree();
		long sufficientDefinitions = definitions.stream().filter(definition -> definition.type == Type.SUFFICIENT).count();
		long necessaryDefinitions = definitions.stream().filter(definition -> definition.type == Type.NECESSARY).count();
		return new LogicalDefinition(sufficientDefinitions, necessaryDefinitions, definitions);
	}

	private List<Definition> processDiTree() {
		List<Definition> definitions = new ArrayList<>();
		int[] definitionIndices = diTreeEntity.successorMap().get(diTreeEntity.root().vertexIndex()).toArray();
		for (int definitionIdx : definitionIndices) {
			Definition definition = buildDefinition(definitionIdx);
			definitions.add(definition);
		}
		return definitions;
	}

	private Definition buildDefinition(int definitionIdx) {
		Type type = determineDefinitionType(diTreeEntity.vertexMap().get(definitionIdx));
		List<RoleGroup> roleGroups = new ArrayList<>();
		List<Role> roles = new ArrayList<>();
		List<Reference> references = new ArrayList<>();

		int[] successors = parseSuccessors(definitionIdx);
		for (int successorIdx : successors) {
			SubType subType = determineSubType(successorIdx);
			switch (subType) {
				case ROLE_GROUP -> roleGroups.add(buildRoleGroup(successorIdx));
				case ROLE -> roles.add(buildRole(successorIdx));
				case Reference -> references.add(buildReference(successorIdx));
				case null, default -> throw new IllegalStateException("Unknown sub type");
			}
		}

		return new Definition(UUID.randomUUID(), type, roleGroups, roles, references); //TODO - figure out index
	}

	private RoleGroup buildRoleGroup(int roleGroupIdx) {
		List<Role> roles = new ArrayList<>();
		int[] successors = parseSuccessors(roleGroupIdx);
		for (int roleIdx : successors) {
			roles.add(buildRole(roleIdx));
		}
		return new RoleGroup(UUID.randomUUID(), roles);
	}

	private Role buildRole(int roleIdx) {
		Concept role = parseRoleProperty(roleIdx);
		int referenceIdx = parseRoleReferenceIndex(roleIdx);
		Concept filler = parseConceptReference(referenceIdx);
		return new Role(role, filler);
	}

	private Reference buildReference(int referenceIdx) {
		Concept reference = parseConceptReference(referenceIdx);
		return new Reference(reference);
	}


	private SubType determineSubType(int successorIdx) {
		EntityVertex andSuccessorVertex = diTreeEntity.vertexMap().get(successorIdx);
		//Check if the meaning is Concept Reference (aka Is-A)
		if (andSuccessorVertex.getMeaningNid() == TinkarTermV2.CONCEPT_REFERENCE.nid()) {
			return SubType.Reference;
		} else if (andSuccessorVertex.getMeaningNid() == TinkarTermV2.ROLE.nid()) {
			//Need to see if the Role Type property is Role Group or a Role constraint
			Concept role = parseRoleProperty(successorIdx);
			if (role.nid() == TinkarTermV2.ROLE_GROUP.nid()) {
				return SubType.ROLE_GROUP;
			} else {
				return SubType.ROLE;
			}
		}
		return null;
	}

	private int[] parseSuccessors(int index) {
		int andIndex = diTreeEntity.successorMap().get(index).get(0);
		EntityVertex andVertex = diTreeEntity.vertexMap().get(andIndex);
		if (andVertex.getMeaningNid() != TinkarTermV2.AND.nid()) {
			throw new IllegalStateException("Expected AND node in single definition");
		}
		return diTreeEntity.successorMap().get(andIndex).toArray();
	}

	private Concept parseConceptReference(int index) {
		Concept[] roleProperties = diTreeEntity.vertexMap().get(index).properties().values().toArray(new Concept[0]);
		if (roleProperties.length != 1) {
			throw new IllegalStateException("Expected 2 properties for Role");
		}
		return roleProperties[0];
	}

	private Concept parseRoleProperty(int index) {
		Concept[] roleProperties = diTreeEntity.vertexMap().get(index).properties().values().toArray(new Concept[0]);
		if (roleProperties.length != 2) {
			throw new IllegalStateException("Expected 2 properties for Role");
		}
		return roleProperties[0];
	}

	private int parseRoleReferenceIndex(int index) {
		int roleReferenceIndex = diTreeEntity.successorMap().get(index).get(0);
		EntityVertex roleReferenceVertex = diTreeEntity.vertexMap().get(roleReferenceIndex);
		if (roleReferenceVertex.getMeaningNid() != TinkarTermV2.CONCEPT_REFERENCE.nid()) {
			throw new IllegalStateException("Expected Concept Reference in role group in sufficient definition for " + roleReferenceVertex.getMeaningNid());
		}
		return roleReferenceIndex;
	}

	private Type determineDefinitionType(EntityVertex entityVertex) {
		if (entityVertex.getMeaningNid() == TinkarTermV2.NECESSARY_SET.nid()) {
			return Type.NECESSARY;
		} else if (entityVertex.getMeaningNid() == TinkarTermV2.SUFFICIENT_SET.nid()) {
			return Type.SUFFICIENT;
		} else {
			throw new IllegalStateException("Unknown definition type");
		}
	}
}

/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.simplejdbcmapper.relationship;

import org.springframework.util.Assert;

/**
 * The relationship definition. It is thread safe and can be used with different
 * query results which have the same relationship.
 * 
 * <pre>
 * Relationship orderToManyOrderLine = 
 *      Relationshp.type(Order.class).toMany(OrderLine.class).joinOn("id", "orderId".populate("orderLines");
 *                                               
 * {@code List<Order>} orders = relationshipMapper.assemble(orderToManyOrderLine).getList(Order.class);
 * 
 * </pre>
 * <p>
 * For more details see the <a href=
 * "https://github.com/spring-jdbc-crud/simplejdbcmapper#assembling-relationships-from-custom-queries">documentation</a>
 * 
 * 
 * @author Antony Joseph
 */
public class Relationship {
	private Class<?> mainType;
	private Class<?> relatedType;
	private Class<?> throughType;
	private ToOne toOne;
	private ToMany toMany;
	private ToManyThrough toManyThrough;
	private String relationshipType;

	Relationship(Class<?> mainType, Class<?> relatedType, Class<?> throughType, ToOne toOne, ToMany toMany,
			ToManyThrough toManyThrough, String relationshipType) {
		this.mainType = mainType;
		this.relatedType = relatedType;
		this.throughType = throughType;
		this.toOne = toOne;
		this.toMany = toMany;
		this.toManyThrough = toManyThrough;
		this.relationshipType = relationshipType;
	}

	public static RelationshipFluent.RelationshipType type(Class<?> type) {
		Assert.notNull(type, "type must not be null");
		return new RelationshipFluentx(type);
	}

	Class<?> getMainType() {
		return mainType;
	}

	Class<?> getRelatedType() {
		return relatedType;
	}

	String getRelationshipType() {
		return relationshipType;
	}

	Class<?> getThroughType() {
		return throughType;
	}

	ToOne getToOne() {
		return toOne;
	}

	ToMany getToMany() {
		return toMany;
	}

	ToManyThrough getToManyThrough() {
		return toManyThrough;
	}

	@Override
	public String toString() {
		return mainType.getSimpleName() + " " + relationshipType + " " + relatedType.getSimpleName();
	}

	private static class RelationshipFluentx implements RelationshipFluent.RelationshipType, RelationshipFluent.ToOne,
			RelationshipFluent.ToMany, RelationshipFluent.Populate {
		private Class<?> mainType;
		private Class<?> relatedType;
		private Class<?> throughType;
		private ToOne toOne;
		private ToMany toMany;
		private ToManyThrough toManyThrough;
		private String relationshipType;

		public RelationshipFluentx(Class<?> mainType) {
			this.mainType = mainType;
		}

		public RelationshipFluent.ToOne toOne(Class<?> relatedType) {
			Assert.notNull(relatedType, "relatedType must not be null");
			if (mainType == relatedType) {
				throw new IllegalArgumentException("mainType and relatedType cannot be same.");
			}
			this.relatedType = relatedType;
			this.relationshipType = RelationshipMapper.TO_ONE;
			this.toOne = new ToOne(mainType, relatedType);
			return this;
		}

		public RelationshipFluent.ToMany toMany(Class<?> relatedType) {
			Assert.notNull(relatedType, "relatedType must not be null");
			if (mainType == relatedType) {
				throw new IllegalArgumentException("mainType and relatedType cannot be same.");
			}
			this.relatedType = relatedType;
			this.relationshipType = RelationshipMapper.TO_MANY;
			this.toMany = new ToMany(mainType, relatedType);
			return this;
		}

		public RelationshipFluent.Populate joinOn(String mainObjJoinProperty, String relatedObjJoinProperty) {
			if (relationshipType.equals(RelationshipMapper.TO_ONE)) {
				toOne.joinOn(mainObjJoinProperty, relatedObjJoinProperty);
			} else if (relationshipType.equals(RelationshipMapper.TO_MANY)) {
				toMany.joinOn(mainObjJoinProperty, relatedObjJoinProperty);
			}
			return this;
		}

		public RelationshipFluent.Populate through(Class<?> throughType, String fkPropertyToMainObjId,
				String fkPropertyToRelatedObjId) {
			this.throughType = throughType;
			this.relationshipType = RelationshipMapper.TO_MANY_THROUGH;
			this.toManyThrough = new ToManyThrough(mainType, relatedType);
			toManyThrough.through(throughType, fkPropertyToMainObjId, fkPropertyToRelatedObjId);
			return this;
		}

		public Relationship populate(String mainObjPropertyToPopulate) {
			if (relationshipType.equals(RelationshipMapper.TO_ONE)) {
				toOne.populate(mainObjPropertyToPopulate);
			} else if (relationshipType.equals(RelationshipMapper.TO_MANY)) {
				toMany.populate(mainObjPropertyToPopulate);
			} else {
				// toManyThrough
				toManyThrough.populate(mainObjPropertyToPopulate);
			}
			return new Relationship(mainType, relatedType, throughType, toOne, toMany, toManyThrough, relationshipType);
		}

	}

}

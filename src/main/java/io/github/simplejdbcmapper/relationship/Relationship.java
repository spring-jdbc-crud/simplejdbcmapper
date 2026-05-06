package io.github.simplejdbcmapper.relationship;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import io.github.simplejdbcmapper.relationship.RelationshipMapper.ExtractorResult;

public class Relationship<T> implements RelationshipSpec, ToManySpec, ToOneSpec, PopulateSpec, GetListSpec {
	static final String IS_PREFIX = "is";
	static final String GET_PREFIX = "get";
	static final String SET_PREFIX = "set";

	private Class<T> mainType;
	private Class<?> relatedType;
	private List<ExtractorResult> results = new ArrayList<>();

	private List<T> listT = new ArrayList<>();

	public void addToList(T obj) {
		listT.add(obj);
	}

	public List<T> getListT() {
		return listT;
	}

	private ToOne toOne;
	private ToMany toMany;

	private String relationshipType = "toOne";

	private Relationship(Class<T> mainType, List<ExtractorResult> results) {
		this.mainType = mainType;
		this.results = results;

		this.toOne = new ToOne();
		this.toMany = new ToMany();
	}

	public static <T> RelationshipSpec newInstance(Class<T> type, List<ExtractorResult> results) {
		return new Relationship<>(type, results);
	}

	/**
	 * A toOne relationship
	 * 
	 * @param <U>            the type of the related object
	 * 
	 * @param relatedObjList the list of related objects
	 * 
	 */
	public <U> ToOneSpec toOne(Class<U> relatedType) {
		this.relatedType = relatedType;
		return this;
	}

	public <U> ToManySpec toMany(Class<U> relatedType) {
		this.relatedType = relatedType;
		this.relationshipType = "toMany";
		return this;
	}

	public PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty) {
		if (relationshipType.equals("toOne")) {
			toOne.joinOn(mainObjJoinProperty, relatedObjJoinProperty, getList(mainType), getList(relatedType));
		} else if (relationshipType.equals("toMany")) {
			toMany.joinOn(mainObjJoinProperty, relatedObjJoinProperty, getList(mainType), getList(relatedType));
		}

		return this;
	}

	public PopulateSpec through(Class<?> throughType, String fkPropertyToMainObjId, String fkPropertyToRelatedObjId) {
		this.relationshipType = "toManyThrough";

		ExtractorResult mainResult = getExtractorResult(mainType);
		ExtractorResult relatedResult = getExtractorResult(relatedType);

		toMany.through(getList(throughType), fkPropertyToMainObjId, fkPropertyToRelatedObjId, mainResult.list(),
				mainResult.idPropertyName(), relatedResult.list(), relatedResult.idPropertyName());
		return this;
	}

	public GetListSpec populate(String propertyToPopulateOnMainObj) {
		if (relationshipType.equals("toOne")) {
			toOne.populate(propertyToPopulateOnMainObj, getList(mainType));
			toOne.populateToOne(getList(mainType), getList(relatedType));
		} else if (relationshipType.equals("toMany")) {
			toMany.populate(propertyToPopulateOnMainObj, getList(mainType));
			toMany.populateToMany(getList(mainType), getList(relatedType));
		} else {
			// toManyThrough
			toMany.populate(propertyToPopulateOnMainObj, getList(mainType));
			toMany.populateToManyThrough(getList(mainType), getList(relatedType));
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> getList(Class<E> type) {
		for (ExtractorResult result : results) {
			if (result.entityType() == type) {
				return (List<E>) result.list();
			}
		}
		throw new IllegalArgumentException(type + "was not part of the query result set");
	}

	private ExtractorResult getExtractorResult(Class<?> type) {
		for (ExtractorResult result : results) {
			if (result.entityType() == type) {
				return result;
			}
		}
		throw new IllegalArgumentException(type + "was not part of the query result set");
	}

	static Method getReadMethod(List<?> list, String propertyName) {
		if (!CollectionUtils.isEmpty(list)) {
			for (Object obj : list) {
				if (obj != null) {
					Method m = ReflectionUtils.findMethod(obj.getClass(),
							GET_PREFIX + StringUtils.capitalize(propertyName));
					if (m == null) {
						m = ReflectionUtils.findMethod(obj.getClass(),
								IS_PREFIX + StringUtils.capitalize(propertyName));
					}
					if (m == null) {
						throw new IllegalArgumentException("Invalid argument. Could not find getter for "
								+ obj.getClass().getName() + "." + propertyName);
					}
					return m;
				}
			}
		}
		return null;
	}

	static Method getWriteMethod(List<?> list, String propertyName) {
		if (!CollectionUtils.isEmpty(list)) {
			for (Object obj : list) {
				if (obj != null) {
					Field field = ReflectionUtils.findField(obj.getClass(), propertyName);
					if (field == null) {
						throw new IllegalArgumentException("Invalid argument. Property name " + propertyName
								+ " does not exist for " + obj.getClass().getName());
					}
					Method m = ReflectionUtils.findMethod(obj.getClass(),
							SET_PREFIX + StringUtils.capitalize(propertyName), field.getType());
					if (m == null) {
						throw new IllegalArgumentException("Invalid argument. Could not find setter for "
								+ obj.getClass().getName() + "." + propertyName);
					}
					return m;
				}
			}
		}
		return null;
	}

	static Class<?> getPropertyType(List<?> list, String propertyName) {
		if (!CollectionUtils.isEmpty(list)) {
			for (Object obj : list) {
				if (obj != null) {
					Field field = ReflectionUtils.findField(obj.getClass(), propertyName);
					if (field != null) {
						return field.getType();
					}
				}
			}
		}
		return null;
	}

}

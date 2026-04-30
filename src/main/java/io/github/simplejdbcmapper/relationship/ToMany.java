package io.github.simplejdbcmapper.relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class ToMany implements ToManySpec, ThroughSpec, PopulateSpec {

	private String mainObjIdProperty;

	private String relatedObjIdProperty;

	private String relatedObjFkProperty;

	private String mainObjPropertyToPopulate;

	private List<?> mainObjList;

	private List<?> relatedObjList;

	private BeanWrapper bwMainObj; // used for validation of property names

	private BeanWrapper bwRelatedObj; // used for validation of property names;

	private IntermediateJoiner intermediateJoiner;

	private String relationshipType = "hasMany";

	private ToMany(List<?> mainObjList, List<?> relatedObjList) {
		this.mainObjList = mainObjList;
		this.relatedObjList = relatedObjList;
		bwMainObj = getBeanWrapper(mainObjList);
		bwRelatedObj = getBeanWrapper(relatedObjList);
	}

	static ToManySpec toMany(List<?> mainObjList, List<?> relatedObjList) {
		return new ToMany(mainObjList, relatedObjList);
	}

	public PopulateSpec joinOn(String mainObjIdProperty, String relatedObjFkProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjFkProperty, "relatedObjFkProperty must not be null");

		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjIdProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + mainObjIdProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		if (bwRelatedObj != null && !bwRelatedObj.isReadableProperty(relatedObjFkProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + relatedObjFkProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		this.mainObjIdProperty = mainObjIdProperty;
		this.relatedObjFkProperty = relatedObjFkProperty;
		return this;
	}

	public ThroughSpec through(List<?> intermediateList, String fkPropertyToMainObjId,
			String fkPropertyToRelatedObjId) {
		Assert.notNull(fkPropertyToMainObjId, "fkPropertyToMainObjId must not be null");
		Assert.notNull(fkPropertyToRelatedObjId, "fkPropertyToRelatedObjId must not be null");

		BeanWrapper bw = getBeanWrapper(intermediateList);
		if (bw != null && !bw.isReadableProperty(fkPropertyToMainObjId)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + fkPropertyToMainObjId
					+ " does not exist for " + bw.getWrappedClass().getName());
		}
		if (bw != null && !bw.isReadableProperty(fkPropertyToRelatedObjId)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + fkPropertyToRelatedObjId
					+ " does not exist for " + bw.getWrappedClass().getName());
		}

		this.intermediateJoiner = new IntermediateJoiner(intermediateList, fkPropertyToMainObjId,
				fkPropertyToRelatedObjId);

		this.relationshipType = "hasManyThrough";
		return this;
	}

	public PopulateSpec ids(String mainObjIdProperty, String relatedObjIdProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjIdProperty, "relatedObjIdProperty must not be null");
		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjIdProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + mainObjIdProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}
		if (bwRelatedObj != null && !bwRelatedObj.isReadableProperty(relatedObjIdProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + relatedObjIdProperty
					+ " does not exist for " + bwRelatedObj.getWrappedClass().getName());
		}

		this.mainObjIdProperty = mainObjIdProperty;
		this.relatedObjIdProperty = relatedObjIdProperty;
		return this;
	}

	public void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjPropertyToPopulate)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + mainObjPropertyToPopulate
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}
		this.mainObjPropertyToPopulate = mainObjPropertyToPopulate;
		if ("hasMany".equals(relationshipType)) {
			populateHasMany();
		} else {
			populateHasManyThrough();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populateHasMany() {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		Map<Object, List<Object>> foreignKeyToListMap = new HashMap<>();
		for (Object relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object foreignKeyPropertyValue = bwRelatedObj.getPropertyValue(relatedObjFkProperty);
				if (foreignKeyPropertyValue != null) {
					List list = foreignKeyToListMap.get(foreignKeyPropertyValue);
					if (list == null) {
						list = new ArrayList<>();
						list.add(relatedObj);
						foreignKeyToListMap.put(foreignKeyPropertyValue, list);
					} else {
						list.add(relatedObj);
					}
				}
			}
		}
		for (Object mainObj : mainObjList) {
			if (mainObj != null) {
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object idPropertyValue = bwMainObj.getPropertyValue(mainObjIdProperty);
				List populaterList = foreignKeyToListMap.get(idPropertyValue);
				if (populaterList == null) {
					populaterList = new ArrayList();
				}
				bwMainObj.setPropertyValue(mainObjPropertyToPopulate, populaterList);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void populateHasManyThrough() {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		// relatedObjId - relatedObj
		Map<Object, Object> idToRelatedObjMap = new HashMap<>();
		for (Object relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object relatedObjIdValue = bwRelatedObj.getPropertyValue(relatedObjIdProperty);
				if (relatedObjIdValue != null) {
					idToRelatedObjMap.put(relatedObjIdValue, relatedObj);
				}
			}
		}
		for (Object mainObj : mainObjList) {
			if (mainObj != null) {
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object mainObjIdValue = bwMainObj.getPropertyValue(mainObjIdProperty);
				List relatedObjIdListFromJoiner = intermediateJoiner.getRelatedObjIds(mainObjIdValue);
				List populaterList = new ArrayList();
				if (!CollectionUtils.isEmpty(relatedObjIdListFromJoiner)) {
					for (Object relatedObjId : relatedObjIdListFromJoiner) {
						Object relatedObj = idToRelatedObjMap.get(relatedObjId);
						if (relatedObj != null) {
							populaterList.add(relatedObj);
						}
					}
				}
				bwMainObj.setPropertyValue(mainObjPropertyToPopulate, populaterList);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private BeanWrapper getBeanWrapper(List mainObjList) {
		BeanWrapper bw = null;
		if (!CollectionUtils.isEmpty(mainObjList)) {
			for (Object obj : mainObjList) {
				if (obj != null) {
					bw = PropertyAccessorFactory.forBeanPropertyAccess(obj);
					break;
				}
			}
		}
		return bw;
	}

	class IntermediateJoiner {
		@SuppressWarnings("rawtypes")
		// key: mainObjIdValue,
		// value: list of relatedObjIdValues
		private Map<Object, List> mainObjIdMap = new HashMap<>();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public IntermediateJoiner(List<?> intermediateList, String fkPropertyToMainObjId,
				String fkPropertyToRelatedObjId) {
			if (CollectionUtils.isEmpty(intermediateList)) {
				return;
			}
			for (Object intermediateObj : intermediateList) {
				BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(intermediateObj);
				Object mainObjIdValue = bw.getPropertyValue(fkPropertyToMainObjId);
				Object relatedObjIdValue = bw.getPropertyValue(fkPropertyToRelatedObjId);
				if (mainObjIdValue != null && relatedObjIdValue != null) {
					if (mainObjIdMap.containsKey(mainObjIdValue)) {
						mainObjIdMap.get(mainObjIdValue).add(relatedObjIdValue);
					} else {
						List list = new ArrayList();
						list.add(relatedObjIdValue);
						mainObjIdMap.put(mainObjIdValue, list);
					}
				}
			}

		}

		@SuppressWarnings("rawtypes")
		public List getRelatedObjIds(Object mainObjId) {
			return mainObjIdMap.get(mainObjId);
		}
	}
}

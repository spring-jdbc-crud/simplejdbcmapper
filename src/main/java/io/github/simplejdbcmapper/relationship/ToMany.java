package io.github.simplejdbcmapper.relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
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

	private ToMany(List<?> mainObjList, List<?> relatedObjList) {
		this.mainObjList = mainObjList;
		this.relatedObjList = relatedObjList;

		if (!CollectionUtils.isEmpty(mainObjList)) {
			Object obj = mainObjList.get(0);
			bwMainObj = new BeanWrapperImpl(obj);
		}

		if (!CollectionUtils.isEmpty(relatedObjList)) {
			Object obj = relatedObjList.get(0);
			bwRelatedObj = new BeanWrapperImpl(obj);
		}
	}

	public static ToManySpec toMany(List<?> mainObjList, List<?> relatedObjList) {
		return new ToMany(mainObjList, relatedObjList);
	}

	public PopulateSpec joinOn(String mainObjIdProperty, String relatedObjFkProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjFkProperty, "relatedObjFkProperty must not be null");

		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjIdProperty)) {
			throw new IllegalArgumentException("invalid argument. Property name " + mainObjIdProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		if (bwRelatedObj != null && !bwRelatedObj.isReadableProperty(relatedObjFkProperty)) {
			throw new IllegalArgumentException("invalid argument. Property name " + relatedObjFkProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		this.mainObjIdProperty = mainObjIdProperty;
		this.relatedObjFkProperty = relatedObjFkProperty;
		return this;
	}

	public void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjPropertyToPopulate)) {
			throw new IllegalArgumentException("invalid argument. Property name " + mainObjPropertyToPopulate
					+ " does not readable for " + bwMainObj.getWrappedClass().getName());
		}

		this.mainObjPropertyToPopulate = mainObjPropertyToPopulate;
		populateHasMany();
	}

	public ThroughSpec through(List<?> intermediateList, String fkPropertyToMainObjId,
			String fkPropertyToRelatedObjId) {
		Assert.notNull(fkPropertyToMainObjId, "fkPropertyToMainObjId must not be null");
		Assert.notNull(fkPropertyToRelatedObjId, "fkPropertyToRelatedObjId must not be null");

		if (!CollectionUtils.isEmpty(intermediateList)) {
			Object obj = intermediateList.get(0);
			BeanWrapper bw = new BeanWrapperImpl(obj);
			if (!bw.isReadableProperty(fkPropertyToMainObjId)) {
				throw new IllegalArgumentException("invalid argument. Property name " + fkPropertyToMainObjId
						+ " does not exist for " + obj.getClass().getName());
			}
			if (!bw.isReadableProperty(fkPropertyToRelatedObjId)) {
				throw new IllegalArgumentException("invalid argument. Property name " + fkPropertyToRelatedObjId
						+ " does not exist for " + obj.getClass().getName());
			}
		}

		this.intermediateJoiner = new IntermediateJoiner(intermediateList, fkPropertyToMainObjId,
				fkPropertyToRelatedObjId);
		return this;
	}

	public PopulateSpec ids(String mainObjIdProperty, String relatedObjIdProperty) {

		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjIdProperty, "relatedObjIdProperty must not be null");

		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjIdProperty)) {
			throw new IllegalArgumentException("invalid argument. Property name " + mainObjIdProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}
		if (bwRelatedObj != null && !bwRelatedObj.isReadableProperty(relatedObjIdProperty)) {
			throw new IllegalArgumentException("invalid argument. Property name " + relatedObjIdProperty
					+ " does not exist for " + bwRelatedObj.getWrappedClass().getName());
		}

		this.mainObjIdProperty = mainObjIdProperty;
		this.relatedObjIdProperty = relatedObjIdProperty;
		return this;
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
				if (idPropertyValue != null) {
					bwMainObj.setPropertyValue(mainObjPropertyToPopulate, foreignKeyToListMap.get(idPropertyValue));
				}
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
				Object idValue = bwRelatedObj.getPropertyValue(relatedObjIdProperty);
				if (idValue != null) {
					idToRelatedObjMap.put(idValue, relatedObj);
				}
			}
		}
		for (Object mainObj : mainObjList) {
			if (mainObj != null) {
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object mainObjIdValue = bwMainObj.getPropertyValue(mainObjIdProperty);
				List relatedObjIdList = intermediateJoiner.getRelatedObjIds(mainObjIdValue);
				if (!CollectionUtils.isEmpty(relatedObjIdList)) {
					List list = new ArrayList();
					for (Object relatedObjId : relatedObjIdList) {
						Object relatedObj = idToRelatedObjMap.get(relatedObjId);
						if (relatedObj != null) {
							list.add(relatedObj);
						}
					}
					if (!CollectionUtils.isEmpty(relatedObjList)) {
						bwMainObj.setPropertyValue(mainObjPropertyToPopulate, list);
					}
				}
			}
		}
	}

	class IntermediateJoiner {

		@SuppressWarnings("rawtypes")
		// key: mainObjIdValue,
		// value: list of relatedObjIdValues
		private Map<Object, List> mainObjIdMap = new HashMap<>();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public IntermediateJoiner(List<?> intermediateList, String fkPropertyToMainObjId,
				String fkPropertyToRelatedObjId) {
			for (Object assocObj : intermediateList) {
				BeanWrapper bw = new BeanWrapperImpl(assocObj);
				Object mainObjIdValue = bw.getPropertyValue(fkPropertyToMainObjId);
				Object relatedObjIdValue = bw.getPropertyValue(fkPropertyToRelatedObjId);
				if (mainObjIdMap.containsKey(mainObjIdValue)) {
					mainObjIdMap.get(mainObjIdValue).add(relatedObjIdValue);
				} else {
					List list = new ArrayList();
					list.add(relatedObjIdValue);
					mainObjIdMap.put(mainObjIdValue, list);
				}
			}

		}

		@SuppressWarnings("rawtypes")
		public List getRelatedObjIds(Object mainObjId) {
			return mainObjIdMap.get(mainObjId);
		}
	}
}

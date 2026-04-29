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
import org.springframework.util.StringUtils;

public class ToMany implements ToManySpec, ThroughSpec {

	private String matchOnMainObjProperty;
	private String matchOnRelatedObjProperty;

	private String mainObjPropertyToPopulate;

	private String mainObjIdProperty;
	private String relatedObjIdProperty;

	private List<?> mainObjList;
	private List<?> relatedObjList;

	private IntermediateJoiner intermediateJoiner;

	private ToMany(List<?> mainObjList, List<?> relatedObjList) {
		this.mainObjList = mainObjList;
		this.relatedObjList = relatedObjList;
	}

	public static ToManySpec toMany(List<?> mainObjList, List<?> relatedObjList) {
		return new ToMany(mainObjList, relatedObjList);
	}

	public ToManySpec joinOn(String mainObjIdProperty, String relatedObjFkProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjFkProperty, "relatedObjFkProperty must not be null");

		this.matchOnMainObjProperty = mainObjIdProperty;
		this.matchOnRelatedObjProperty = relatedObjFkProperty;
		return this;
	}

	public ToManySpec populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		this.mainObjPropertyToPopulate = mainObjPropertyToPopulate;
		return this;
	}

	public ThroughSpec through(List<?> intermediateList, String fkPropertyToMainObjId,
			String fkPropertyToRelatedObjId) {
		this.intermediateJoiner = new IntermediateJoiner(intermediateList, fkPropertyToMainObjId,
				fkPropertyToRelatedObjId);
		return this;
	}

	public ThroughSpec ids(String mainObjIdProperty, String relatedObjIdProperty) {
		this.mainObjIdProperty = mainObjIdProperty;
		this.relatedObjIdProperty = relatedObjIdProperty;
		return this;
	}

	public <T, U> void build(List<T> mainObjList, List<U> relatedObjList) {
		populateHasMany(mainObjList, relatedObjList);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	<T, U> void populateHasMany(List<T> mainObjList, List<U> relatedObjList) {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		Map<Object, List<U>> foreignKeyToListMap = new HashMap<>();
		for (U relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object foreignKeyPropertyValue = bwRelatedObj.getPropertyValue(matchOnRelatedObjProperty);
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
		for (T mainObj : mainObjList) {
			if (mainObj != null) {
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object idPropertyValue = bwMainObj.getPropertyValue(matchOnMainObjProperty);
				if (idPropertyValue != null) {
					bwMainObj.setPropertyValue(mainObjPropertyToPopulate, foreignKeyToListMap.get(idPropertyValue));
				}
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void populateHasManyThrough(List<?> mainObjList, List<?> relatedObjList, String mainObjIdProperty,
			String relatedObjIdProperty, IntermediateJoiner intermediateJoiner, String mainObjHasManyProperty) {
		Assert.notNull(mainObjIdProperty, "mainObjIdProperty must not be null");
		Assert.notNull(relatedObjIdProperty, "relatedObjIdProperty must not be null");
		Assert.notNull(intermediateJoiner, "intermediateJoiner must not be null");
		Assert.notNull(mainObjHasManyProperty, "mainObjHasManyProperty must not be null");

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
						bwMainObj.setPropertyValue(mainObjHasManyProperty, list);
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
		public IntermediateJoiner(List<?> associativeList, String fkPropertyToMainObjId,
				String fkPropertyToRelatedObjId) {
			Assert.notNull(associativeList, "associativeList cannot be null");
			if (!StringUtils.hasText(fkPropertyToMainObjId)) {
				throw new IllegalArgumentException("mainObjIdProperty has no value");
			}
			if (!StringUtils.hasText(fkPropertyToRelatedObjId)) {
				throw new IllegalArgumentException("relatedObjIdProperty has no value");
			}
			for (Object assocObj : associativeList) {
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

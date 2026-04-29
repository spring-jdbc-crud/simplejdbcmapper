package io.github.simplejdbcmapper.relationship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class ToOne implements ToOneSpec {
	private String matchOnMainObjProperty;
	private String matchOnRelatedObjProperty;

	private String mainObjPropertyToPopulate;

	@SuppressWarnings("unused")
	private List<?> mainObjList;
	@SuppressWarnings("unused")
	private List<?> relatedObjList;

	private ToOne(List<?> mainObjList, List<?> relatedObjList) {
		this.mainObjList = mainObjList;
		this.relatedObjList = relatedObjList;
	}

	public static ToOneSpec toOne(List<?> mainObjList, List<?> relatedObjList) {
		// Assert.notNull(type, "type cannot be null");
		return new ToOne(mainObjList, relatedObjList);
	}

	public ToOneSpec joinOn(String mainObjFkProperty, String relatedObjIdProperty) {
		Assert.notNull(mainObjFkProperty, "mainObjFkProperty must not be null");
		Assert.notNull(relatedObjIdProperty, "relatedObjIdProperty must not be null");

		this.matchOnMainObjProperty = mainObjFkProperty;
		this.matchOnRelatedObjProperty = relatedObjIdProperty;
		return this;
	}

	public ToOneSpec populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		this.mainObjPropertyToPopulate = mainObjPropertyToPopulate;
		return this;
	}

	public <T, U> void build(List<T> mainObjList, List<U> relatedObjList) {
		populateHasOne(mainObjList, relatedObjList);
	}

	public <T, U> void populateHasOne(List<T> mainObjList, List<U> relatedObjList) {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		Map<Object, U> idToRelatedObjMap = new HashMap<>();
		for (U relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object idPropertyValue = bwRelatedObj.getPropertyValue(matchOnRelatedObjProperty);
				if (idPropertyValue != null) {
					idToRelatedObjMap.put(idPropertyValue, relatedObj);
				}
			}
		}
		for (T mainObj : mainObjList) {
			if (mainObj != null) {
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object foreignKeyPropertyValue = bwMainObj.getPropertyValue(matchOnMainObjProperty);
				if (foreignKeyPropertyValue != null) {
					bwMainObj.setPropertyValue(mainObjPropertyToPopulate,
							idToRelatedObjMap.get(foreignKeyPropertyValue));
				}
			}
		}
	}

}

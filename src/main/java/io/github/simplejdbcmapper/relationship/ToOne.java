package io.github.simplejdbcmapper.relationship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class ToOne implements ToOneSpec, PopulateSpec {
	private String matchOnMainObjProperty;
	private String matchOnRelatedObjProperty;

	private String mainObjPropertyToPopulate;

	private List<?> mainObjList;
	private List<?> relatedObjList;

	private BeanWrapper bwMainObj; // used for validation of property names

	private BeanWrapper bwRelatedObj; // used for validation of property names;

	private ToOne(List<?> mainObjList, List<?> relatedObjList) {
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

	public static ToOneSpec toOne(List<?> mainObjList, List<?> relatedObjList) {
		return new ToOne(mainObjList, relatedObjList);
	}

	public PopulateSpec joinOn(String mainObjFkProperty, String relatedObjIdProperty) {
		Assert.notNull(mainObjFkProperty, "mainObjFkProperty must not be null");
		Assert.notNull(relatedObjIdProperty, "relatedObjIdProperty must not be null");

		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjFkProperty)) {
			throw new IllegalArgumentException("invalid argument. Property name " + mainObjFkProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		if (bwRelatedObj != null && !bwRelatedObj.isReadableProperty(relatedObjIdProperty)) {
			throw new IllegalArgumentException("invalid argument. Property name " + relatedObjIdProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		this.matchOnMainObjProperty = mainObjFkProperty;
		this.matchOnRelatedObjProperty = relatedObjIdProperty;
		return this;
	}

	public void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjPropertyToPopulate)) {
			throw new IllegalArgumentException("invalid argument. Property name " + mainObjPropertyToPopulate
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}
		this.mainObjPropertyToPopulate = mainObjPropertyToPopulate;
		populateHasOne();
	}

	private void populateHasOne() {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		Map<Object, Object> idToRelatedObjMap = new HashMap<>();
		for (Object relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object idPropertyValue = bwRelatedObj.getPropertyValue(matchOnRelatedObjProperty);
				if (idPropertyValue != null) {
					idToRelatedObjMap.put(idPropertyValue, relatedObj);
				}
			}
		}
		for (Object mainObj : mainObjList) {
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

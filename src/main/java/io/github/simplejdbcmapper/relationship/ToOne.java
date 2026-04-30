package io.github.simplejdbcmapper.relationship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class ToOne<T, U> implements ToOneSpec<T, U>, PopulateSpec {
	private String mainObjJoinProperty;
	private String relatedObjJoinProperty;

	private String mainObjPropertyToPopulate;

	private List<T> mainObjList;
	private List<U> relatedObjList;

	private BeanWrapper bwMainObj; // used for validation of property names

	private BeanWrapper bwRelatedObj; // used for validation of property names;

	private ToOne(List<T> mainObjList, List<U> relatedObjList) {
		this.mainObjList = mainObjList;
		this.relatedObjList = relatedObjList;
		bwMainObj = getBeanWrapper(mainObjList);
		bwRelatedObj = getBeanWrapper(relatedObjList);
	}

	static <T, U> ToOneSpec<T, U> toOne(List<T> mainObjList, List<U> relatedObjList) {
		return new ToOne<>(mainObjList, relatedObjList);
	}

	public PopulateSpec joinOn(String mainObjJoinProperty, String relatedObjJoinProperty) {
		Assert.notNull(mainObjJoinProperty, "mainObjJoinProperty must not be null");
		Assert.notNull(relatedObjJoinProperty, "relatedObjJoinProperty must not be null");

		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjJoinProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + mainObjJoinProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		if (bwRelatedObj != null && !bwRelatedObj.isReadableProperty(relatedObjJoinProperty)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + relatedObjJoinProperty
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}

		this.mainObjJoinProperty = mainObjJoinProperty;
		this.relatedObjJoinProperty = relatedObjJoinProperty;
		return this;
	}

	public void populate(String mainObjPropertyToPopulate) {
		Assert.notNull(mainObjPropertyToPopulate, "mainObjPropertyToPopulate must not be null");
		if (bwMainObj != null && !bwMainObj.isReadableProperty(mainObjPropertyToPopulate)) {
			throw new IllegalArgumentException("Invalid argument. Property name " + mainObjPropertyToPopulate
					+ " does not exist for " + bwMainObj.getWrappedClass().getName());
		}
		this.mainObjPropertyToPopulate = mainObjPropertyToPopulate;
		populateHasOne();
	}

	private void populateHasOne() {
		if (CollectionUtils.isEmpty(mainObjList) || CollectionUtils.isEmpty(relatedObjList)) {
			return;
		}
		Map<Object, U> idToRelatedObjMap = new HashMap<>();
		for (U relatedObj : relatedObjList) {
			if (relatedObj != null) {
				BeanWrapper bwRelatedObj = PropertyAccessorFactory.forBeanPropertyAccess(relatedObj);
				Object idPropertyValue = bwRelatedObj.getPropertyValue(relatedObjJoinProperty);
				if (idPropertyValue != null) {
					idToRelatedObjMap.put(idPropertyValue, relatedObj);
				}
			}
		}
		for (T mainObj : mainObjList) {
			if (mainObj != null) {
				BeanWrapper bwMainObj = PropertyAccessorFactory.forBeanPropertyAccess(mainObj);
				Object foreignKeyPropertyValue = bwMainObj.getPropertyValue(mainObjJoinProperty);
				if (foreignKeyPropertyValue != null) {
					bwMainObj.setPropertyValue(mainObjPropertyToPopulate,
							idToRelatedObjMap.get(foreignKeyPropertyValue));
				}
			}
		}
	}

	private BeanWrapper getBeanWrapper(List<?> list) {
		BeanWrapper bw = null;
		if (!CollectionUtils.isEmpty(list)) {
			for (Object obj : list) {
				if (obj != null) {
					bw = new BeanWrapperImpl(obj);
					break;
				}
			}
		}
		return bw;
	}

}

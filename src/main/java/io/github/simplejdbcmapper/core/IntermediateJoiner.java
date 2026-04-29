package io.github.simplejdbcmapper.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class IntermediateJoiner {

	@SuppressWarnings("rawtypes")
	private Map<Object, List> mainObjIdMap = new HashMap<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IntermediateJoiner(List<?> associativeList, String mainObjIdProperty, String relatedObjIdProperty) {
		Assert.notNull(associativeList, "associativeList cannot be null");
		if (!StringUtils.hasText(mainObjIdProperty)) {
			throw new IllegalArgumentException("mainObjIdProperty has no value");
		}
		if (!StringUtils.hasText(relatedObjIdProperty)) {
			throw new IllegalArgumentException("relatedObjIdProperty has no value");
		}
		for (Object assocObj : associativeList) {
			BeanWrapper bw = new BeanWrapperImpl(assocObj);
			Object mainObjIdValue = bw.getPropertyValue(mainObjIdProperty);
			Object relatedObjIdValue = bw.getPropertyValue(relatedObjIdProperty);
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

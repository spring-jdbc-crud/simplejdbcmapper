package io.github.simplejdbcmapper.core;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import io.github.simplejdbcmapper.exception.MapperException;
/**
 * 
 * Some utility methods.
 * 
 */
public class SimpleJdbcMapperUtils {
	private SimpleJdbcMapperUtils() {
	}

	/**
	 * Merges the corresponding child object to the 'hasOne' property of the parent
	 * 
	 * @param parentList                     The parent list whose 'hasOne' property
	 *                                       that needs to be populated
	 * @param childList                      the child list
	 * @param parentJoinPropertyName         The property name on parent used to
	 *                                       merge the results
	 * @param childJoinPropertyName          The property name on child used to
	 *                                       merge the results
	 * @param parentHasOnePropertyToPopulate The parent property to populate
	 */
	public static <T, U> void mergeResultsToPopulateHasOne(List<T> parentList, List<U> childList,
			String parentJoinPropertyName, String childJoinPropertyName, String parentHasOnePropertyToPopulate) {
		if (CollectionUtils.isEmpty(parentList) || CollectionUtils.isEmpty(childList)) {
			return;
		}
		Set<T> parentSet = new HashSet<>(parentList);
		Set<U> childSet = new HashSet<>(childList);
		parentSet.remove(null);
		childSet.remove(null);
		if (CollectionUtils.isEmpty(parentSet) || CollectionUtils.isEmpty(childSet)) {
			return;
		}
		validateHasOne(parentSet.iterator().next(), childSet.iterator().next(), parentJoinPropertyName,
				childJoinPropertyName, parentHasOnePropertyToPopulate);
		Map<Object, U> idToObjMap = new HashMap<>();
		for (U child : childSet) {
			BeanWrapper bwChild = PropertyAccessorFactory.forBeanPropertyAccess(child);
			Object idPropertyValue = bwChild.getPropertyValue(childJoinPropertyName);
			idToObjMap.put(idPropertyValue, child);
		}
		for (T parent : parentSet) {
			BeanWrapper bwParent = PropertyAccessorFactory.forBeanPropertyAccess(parent);
			Object joinPropertyValue = bwParent.getPropertyValue(parentJoinPropertyName);
			bwParent.setPropertyValue(parentHasOnePropertyToPopulate, idToObjMap.get(joinPropertyValue));
		}
	}

	/**
	 * Merges the corresponding list of child objects to the 'hasMany' property of
	 * the parent
	 * 
	 * @param parentList                      The parent list whose 'hasOne'
	 *                                        property that needs to be populated
	 * @param childList                       the child list
	 * @param parentJoinPropertyName          The property name on parent used to
	 *                                        merge the results
	 * @param childJoinPropertyName           The property name on child used to
	 *                                        merge the results
	 * @param parentHasManyPropertyToPopulate The parent property to populate
	 */
	public static <T, U> void mergeResultsToPopulateHasMany(List<T> parentList, List<U> childList,
			String parentJoinPropertyName, String childJoinPropertyName, String parentHasManyPropertyToPopulate) {
		if (CollectionUtils.isEmpty(parentList) || CollectionUtils.isEmpty(childList)) {
			return;
		}
		Set<T> parentSet = new HashSet<>(parentList);
		Set<U> childSet = new LinkedHashSet<>(childList);
		parentSet.remove(null);
		childSet.remove(null);
		if (CollectionUtils.isEmpty(parentSet) || CollectionUtils.isEmpty(childSet)) {
			return;
		}
		validateHasMany(parentSet.iterator().next(), childSet.iterator().next(), parentJoinPropertyName,
				childJoinPropertyName, parentHasManyPropertyToPopulate);
		Map<Object, List<U>> propertyToListMap = new HashMap<>();
		for (U child : childSet) {
			BeanWrapper bwChild = PropertyAccessorFactory.forBeanPropertyAccess(child);
			Object joinPropertyValue = bwChild.getPropertyValue(childJoinPropertyName);
			if (propertyToListMap.containsKey(joinPropertyValue)) {
				List<U> list = propertyToListMap.get(joinPropertyValue);
				list.add(child);
			} else {
				List<U> list = new ArrayList<>();
				list.add(child);
				propertyToListMap.put(joinPropertyValue, list);
			}
		}
		for (T parent : parentSet) {
			BeanWrapper bwParent = PropertyAccessorFactory.forBeanPropertyAccess(parent);
			Object idPropertyValue = bwParent.getPropertyValue(parentJoinPropertyName);
			bwParent.setPropertyValue(parentHasManyPropertyToPopulate, propertyToListMap.get(idPropertyValue));
		}
	}

	/**
	 * Splits the list into multiple lists by chunk size. Can be used to split the
	 * sql IN clauses since some databases have a limitation on 'IN' clause entries
	 * and size
	 *
	 * @param list the list to chunk
	 * @param chunkSize  The size of each chunk
	 * @return Collection of lists broken down by chunkSize
	 */
	@SuppressWarnings("rawtypes")
	public static List<List> chunkTheList(List list, Integer chunkSize) {
		List<List> chunks = new ArrayList<>();
		if (list != null) {
			for (int i = 0; i < list.size(); i += chunkSize) {
				chunks.add(list.subList(i, Math.min(i + chunkSize, list.size())));
			}
		}
		return chunks;
	}

	private static void validateHasOne(Object parentObj, Object childObj, String parentJoinPropertyName,
			String childJoinPropertyName, String parentHasOnePropertyName) {
		Assert.notNull(parentJoinPropertyName, "parentJoinPropertyName must not be null");
		Assert.notNull(childJoinPropertyName, "childJoinPropertyName must not be null");
		Assert.notNull(parentHasOnePropertyName, "parentHasOnePropertyName must not be null");
		BeanWrapper bwParent = PropertyAccessorFactory.forBeanPropertyAccess(parentObj);
		BeanWrapper bwChild = PropertyAccessorFactory.forBeanPropertyAccess(childObj);
		if (!bwParent.isReadableProperty(parentJoinPropertyName)) {
			throw new MapperException(parentJoinPropertyName + " not found in " + bwParent.getWrappedClass());
		}
		if (!bwParent.isReadableProperty(parentHasOnePropertyName)) {
			throw new MapperException(parentHasOnePropertyName + " not found in " + bwParent.getWrappedClass());
		}
		if (!bwChild.isReadableProperty(childJoinPropertyName)) {
			throw new MapperException(childJoinPropertyName + " not found in " + bwChild.getWrappedClass());
		}
		PropertyDescriptor pd = bwParent.getPropertyDescriptor(parentHasOnePropertyName);
		if (!pd.getPropertyType().isAssignableFrom(childObj.getClass())) {
			throw new MapperException("property type conflict. property " + parentObj.getClass().getSimpleName() + "."
					+ parentHasOnePropertyName + " is of type " + pd.getPropertyType().getSimpleName()
					+ " while type for hasOne relationship is " + childObj.getClass().getSimpleName());
		}
	}

	private static void validateHasMany(Object parentObj, Object childObj, String parentJoinPropertyName,
			String childJoinPropertyName, String parentHasManyPropertyName) {
		Assert.notNull(parentJoinPropertyName, "parentJoinPropertyName must not be null");
		Assert.notNull(childJoinPropertyName, "childJoinPropertyName must not be null");
		Assert.notNull(parentHasManyPropertyName, "parentHasManyPropertyName must not be null");
		BeanWrapper bwParent = PropertyAccessorFactory.forBeanPropertyAccess(parentObj);
		BeanWrapper bwChild = PropertyAccessorFactory.forBeanPropertyAccess(childObj);
		if (!bwParent.isReadableProperty(parentJoinPropertyName)) {
			throw new MapperException(parentJoinPropertyName + " not found in " + bwParent.getWrappedClass());
		}
		if (!bwChild.isReadableProperty(childJoinPropertyName)) {
			throw new MapperException(childJoinPropertyName + " not found in " + bwChild.getWrappedClass());
		}
		if (!bwParent.isReadableProperty(parentHasManyPropertyName)) {
			throw new MapperException(parentHasManyPropertyName + " not found in " + bwParent.getWrappedClass());
		}
		PropertyDescriptor pd = bwParent.getPropertyDescriptor(parentHasManyPropertyName);
		if (!Collection.class.isAssignableFrom(pd.getPropertyType())) {
			throw new MapperException(
					"property " + parentObj.getClass().getSimpleName() + "." + parentHasManyPropertyName
							+ " is not a collection. Merge for hasMany requires it to be a collection");
		}
		Class<?> collectionGenericType = getGenericTypeOfCollection(parentObj, parentHasManyPropertyName);
		if (collectionGenericType == null) {
			throw new MapperException("Collections without generic types are not supported. Collection "
					+ parentObj.getClass().getSimpleName() + "." + parentHasManyPropertyName
					+ " does not have a generic type.");
		}
		if (!collectionGenericType.isAssignableFrom(childObj.getClass())) {
			throw new MapperException("Collection generic type and child class type mismatch. "
					+ parentObj.getClass().getSimpleName() + "." + parentHasManyPropertyName + " has generic type "
					+ collectionGenericType.getSimpleName() + " while the collection is of type "
					+ childObj.getClass().getSimpleName());
		}
	}

	private static Class<?> getGenericTypeOfCollection(Object obj, String propertyName) {
		try {
			Field field = obj.getClass().getDeclaredField(propertyName);
			ParameterizedType pt = (ParameterizedType) field.getGenericType();
			Type[] genericType = pt.getActualTypeArguments();
			if (genericType != null && genericType.length > 0) {
				return Class.forName(genericType[0].getTypeName());
			}
		} catch (Exception e) {
			// do nothing
		}
		return null;
	}
}

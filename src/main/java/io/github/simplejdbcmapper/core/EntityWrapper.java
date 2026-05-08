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
package io.github.simplejdbcmapper.core;

import org.springframework.core.convert.ConversionService;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * Entity wrapper for getters and setters.
 * 
 * @author Antony Joseph
 */
class EntityWrapper {
	private Object object;

	public EntityWrapper(Object object) {
		this.object = object;
	}

	public Object getPropertyValue(PropertyMapping propMapping) {
		try {
			return propMapping.getReadMethod().invoke(object);
		} catch (Exception e) {
			throw new MapperException(e.getMessage() + " error while trying to get value of "
					+ object.getClass().getSimpleName() + "." + propMapping.getPropertyName());
		}
	}

	public void setPropertyValue(PropertyMapping propMapping, Object val) {
		setPropertyValue(propMapping, val, null);
	}

	public void setPropertyValue(PropertyMapping propMapping, Object val, ConversionService conversionService) {
		try {
			if (conversionService == null) {
				propMapping.getWriteMethod().invoke(object, val);
			} else {
				propMapping.getWriteMethod().invoke(object,
						conversionService.convert(val, propMapping.getPropertyType()));
			}
		} catch (Exception e) {
			throw new MapperException(
					e.getMessage() + ". Invoking " + propMapping.getWriteMethod() + " with value " + val, e);
		}
	}

	public Class<?> getWrappedClass() {
		return object.getClass();
	}

	public Object getWrappedInstance() {
		return object;
	}

}

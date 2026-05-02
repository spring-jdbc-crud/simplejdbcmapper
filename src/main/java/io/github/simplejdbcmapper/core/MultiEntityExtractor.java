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

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import io.github.simplejdbcmapper.exception.MapperException;

/**
 * The extractor of query results for multiple entities
 * 
 * @author Antony Joseph
 */
class MultiEntityExtractor {
	private static final Logger logger = LoggerFactory.getLogger(MultiEntityExtractor.class);

	private final SimpleJdbcMapperSupport sjmSupport;

	public MultiEntityExtractor(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public String getMultiEntitySqlColumns(MultiEntity multiEntity) {
		StringBuilder sb = new StringBuilder(64);
		StringJoiner sj = new StringJoiner(", ", " ", " ");
		for (Map.Entry<Class<?>, String> entry : multiEntity.getEntries()) {
			String tableAlias = entry.getValue();
			String tablePrefix = tableAlias + ".";
			String colPrefix = tableAlias + "_";
			TableMapping tableMapping = sjmSupport.getTableMapping(entry.getKey());
			for (PropertyMapping propMapping : tableMapping.getPropertyMappings()) {
				sb.append(tablePrefix).append(propMapping.getColumnName()).append(" AS ").append(colPrefix)
						.append(propMapping.getColumnName());
				sj.add(sb);
				sb.setLength(0);
			}
		}
		return sj.toString();
	}

	public ResultSetExtractor<ResultListMap> resultSetExtractor(MultiEntity multiEntity) {
		List<EntityExtractor> entityExtractors = getEntityExtractors(multiEntity);

		return new ResultSetExtractor<ResultListMap>() {
			@SuppressWarnings("unchecked")
			@Override
			public ResultListMap extractData(ResultSet rs) throws SQLException, DataAccessException {
				int rowCnt = 1;
				while (rs.next()) {
					for (EntityExtractor entityExtractor : entityExtractors) {
						EntityRowMapper<?> rowMapper = entityExtractor.rowMapper();
						// rowMapper will always return an object
						Object obj = rowMapper.mapRow(rs, rowCnt);
						try {
							Object id = entityExtractor.idReadMethod().invoke(obj);
							if (id != null && entityExtractor.idSet().add(id)) {
								// not a duplicate
								entityExtractor.result().add(obj);
							}
						} catch (Exception e) {
							throw new MapperException(e.getMessage(), e);
						}
					}
					rowCnt++;
				}

				ResultListMap result = new ResultListMap();
				for (EntityExtractor entityExtractor : entityExtractors) {
					result.putList(entityExtractor.entityType, entityExtractor.result());
				}
				return result;
			}
		};
	}

	@SuppressWarnings("rawtypes")
	List<EntityExtractor> getEntityExtractors(MultiEntity multiEntity) {
		int offset = 1;
		List<EntityExtractor> entityExtractors = new ArrayList<>();
		for (Map.Entry<Class<?>, String> entry : multiEntity.getEntries()) {
			Class<?> entityType = entry.getKey();
			EntityRowMapper<?> rowMapper = newEntityRowMapper(entityType, offset);
			TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
			Method idReadMethod = tableMapping.getIdPropertyMapping().getReadMethod();
			entityExtractors
					.add(new EntityExtractor(entityType, rowMapper, new ArrayList(), idReadMethod, new HashSet()));
			offset += tableMapping.getPropertyMappings().length;
		}
		return entityExtractors;
	}

	public <T> EntityRowMapper<T> newEntityRowMapper(Class<T> entityType, int offset) {
		TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
		EntityRowMapper<T> rowMapper = new EntityRowMapper<>(tableMapping, sjmSupport.getConversionService(), offset);
		if (logger.isDebugEnabled()) {
			logger.debug("EntityRowMapper: " + rowMapper);
		}
		return rowMapper;
	}

	@SuppressWarnings("rawtypes")
	record EntityExtractor(Class entityType, EntityRowMapper<?> rowMapper, List result, Method idReadMethod,
			Set idSet) {
	}

}

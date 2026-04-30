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
import java.util.HashMap;
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
 * @author Antony Joseph
 */
class MultiEntitySupport {
	private static final Logger logger = LoggerFactory.getLogger(MultiEntitySupport.class);

	private final SimpleJdbcMapperSupport sjmSupport;

	public MultiEntitySupport(SimpleJdbcMapperSupport sjmSupport) {
		this.sjmSupport = sjmSupport;
	}

	public String getMultiEntitySqlColumns(MultiEntity multiEntity) {
		StringBuilder sb = new StringBuilder(126);
		StringJoiner sj = new StringJoiner(", ", " ", " ");
		for (Map.Entry<Class<?>, String> entry : multiEntity.getEntities().entrySet()) {
			String tablePrefix = entry.getValue() + ".";
			String colPrefix = entry.getValue() + "_";
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

	@SuppressWarnings("rawtypes")
	public ResultSetExtractor<Map<Class, List>> resultSetExtractor(MultiEntity multiEntity) {
		int offset = 1;
		List<EntityExtractor> entityExtractorList = new ArrayList<>();
		for (Class<?> entityType : multiEntity.getEntities().keySet()) {
			TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
			EntityRowMapper rowMapper = new EntityRowMapper(tableMapping, sjmSupport.getConversionService(), offset);
			if (logger.isDebugEnabled()) {
				logger.debug("EntityRowMapper: " + rowMapper);
			}
			Method idReadMethod = tableMapping.getIdPropertyMapping().getReadMethod();
			entityExtractorList
					.add(new EntityExtractor(entityType, rowMapper, new ArrayList(), idReadMethod, new HashSet()));
			offset += tableMapping.getPropertyMappings().length;
		}

		return new ResultSetExtractor<Map<Class, List>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Map<Class, List> extractData(ResultSet rs) throws SQLException, DataAccessException {
				int rowCnt = 1;
				while (rs.next()) {
					for (EntityExtractor entityExtractor : entityExtractorList) {
						EntityRowMapper rowMapper = entityExtractor.rowMapper();
						Object obj = rowMapper.mapRow(rs, rowCnt);
						try {
							Object id = entityExtractor.idReadMethod().invoke(obj);
							if (entityExtractor.idSet().add(id)) {
								// not a duplicate
								entityExtractor.result().add(obj);
							}
						} catch (Exception e) {
							throw new MapperException(e);
						}
					}
					rowCnt++;
				}
				Map<Class, List> resultMap = new HashMap<>();
				for (EntityExtractor eExtractor : entityExtractorList) {
					resultMap.put(eExtractor.entityType, eExtractor.result());
				}
				return resultMap;
			}
		};
	}

	@SuppressWarnings("rawtypes")
	record EntityExtractor(Class<?> entityType, EntityRowMapper<?> rowMapper, List result, Method idReadMethod,
			Set idSet) {
	}

}

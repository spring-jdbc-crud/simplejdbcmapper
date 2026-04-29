package io.github.simplejdbcmapper.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.CollectionUtils;

import io.github.simplejdbcmapper.exception.MapperException;

public class MultiEntitySupport {

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
		Map<Class, List> tempResultMap = new LinkedHashMap<>();
		Map<Class, EntityRowMapper> mapRowMappers = new LinkedHashMap<>();
		for (Class<?> entityType : multiEntity.getEntities().keySet()) {
			tempResultMap.put(entityType, new ArrayList());
			TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
			mapRowMappers.put(entityType, new EntityRowMapper(tableMapping, sjmSupport.getConversionService(), offset));
			offset += tableMapping.getPropertyMappings().length;
		}

		return new ResultSetExtractor<Map<Class, List>>() {
			@SuppressWarnings("unchecked")
			@Override
			public Map<Class, List> extractData(ResultSet rs) throws SQLException, DataAccessException {
				int rowCnt = 1;
				while (rs.next()) {
					for (Map.Entry<Class, EntityRowMapper> entry : mapRowMappers.entrySet()) {
						EntityRowMapper rowMapper = entry.getValue();
						Object obj = rowMapper.mapRow(rs, rowCnt);
						tempResultMap.get(entry.getKey()).add(obj);
					}
					rowCnt++;
				}
				Map<Class, List> resultMap = new HashMap<>();
				for (Map.Entry<Class, List> entry : tempResultMap.entrySet()) {
					resultMap.put(entry.getKey(), distinctById(entry.getKey(), entry.getValue()));
				}
				return resultMap;
			}
		};
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List distinctById(Class entityType, List list) {
		List resultList = new ArrayList();
		if (!CollectionUtils.isEmpty(list)) {
			Set set = new HashSet();
			TableMapping tableMapping = sjmSupport.getTableMapping(entityType);
			PropertyMapping idPropMapping = tableMapping.getIdPropertyMapping();
			for (Object entityObj : list) {
				try {
					if (entityObj != null) {
						Object id = idPropMapping.getReadMethod().invoke(entityObj);
						if (id != null && set.add(id)) {
							resultList.add(entityObj);
						}
					}
				} catch (Exception e) {
					throw new MapperException(e);
				}
			}
		}
		return resultList;
	}

}

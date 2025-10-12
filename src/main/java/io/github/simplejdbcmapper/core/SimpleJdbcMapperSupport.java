/*
 * Copyright 2025 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jdbc.core.metadata.TableMetaDataProvider;
import org.springframework.jdbc.core.metadata.TableParameterMetaData;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import io.github.simplejdbcmapper.annotation.Column;
import io.github.simplejdbcmapper.annotation.CreatedBy;
import io.github.simplejdbcmapper.annotation.CreatedOn;
import io.github.simplejdbcmapper.annotation.Id;
import io.github.simplejdbcmapper.annotation.IdType;
import io.github.simplejdbcmapper.annotation.Table;
import io.github.simplejdbcmapper.annotation.UpdatedBy;
import io.github.simplejdbcmapper.annotation.UpdatedOn;
import io.github.simplejdbcmapper.annotation.Version;
import io.github.simplejdbcmapper.exception.AnnotationException;
import io.github.simplejdbcmapper.exception.MapperException;

/**
 * Support class for SimpleJdbcMapper
 *
 * @author Antony Joseph
 */
class SimpleJdbcMapperSupport {
	// Map key - class name
	// value - the table mapping
	private SimpleCache<String, TableMapping> tableMappingCache = new SimpleCache<>();

	private final DataSource dataSource;

	private final String schemaName;

	private final String catalogName;

	private String databaseProductName;

	private Map<String, Integer> databaseMetaDataOverrideMap;

	/**
	 * Constructor.
	 *
	 * @param jdbcClient  The jdbcClient
	 * @param schemaName  database schema name.
	 * @param catalogName database catalog name.
	 */
	public SimpleJdbcMapperSupport(DataSource dataSource, String schemaName, String catalogName) {
		Assert.notNull(dataSource, "dataSource must not be null");
		this.dataSource = dataSource;
		this.schemaName = schemaName;
		this.catalogName = catalogName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	/**
	 * Gets the table mapping for the Object. The table mapping has the table name
	 * and and object property to database column mapping.
	 *
	 * <p>
	 * Table name is either from the @Tabel annotation or the underscore case
	 * conversion of the Object name.
	 *
	 * @param clazz The object class
	 * @return The table mapping.
	 */
	public TableMapping getTableMapping(Class<?> clazz) {
		Assert.notNull(clazz, "clazz must not be null");
		TableMapping tableMapping = tableMappingCache.get(clazz.getName());
		if (tableMapping == null) {
			TableColumnInfo tableColumnInfo = getTableColumnInfo(clazz);
			String tableName = tableColumnInfo.getTableName();
			List<Field> fields = getAllFields(clazz);
			IdPropertyInfo idPropertyInfo = getIdPropertyInfo(clazz, fields);
			// key:column name, value: ColumnInfo
			Map<String, ColumnInfo> columnNameToColumnInfo = tableColumnInfo.getColumnInfos().stream()
					.collect(Collectors.toMap(o -> o.getColumnName(), o -> o));
			// key:propertyName, value:PropertyMapping. LinkedHashMap to maintain order of
			// properties
			Map<String, PropertyMapping> propNameToPropertyMapping = new LinkedHashMap<>();
			for (Field field : fields) {
				String propertyName = field.getName();
				Column colAnnotation = AnnotationUtils.findAnnotation(field, Column.class);
				if (colAnnotation != null) {
					String colName = colAnnotation.name();
					if ("[DEFAULT]".equals(colName)) {
						colName = SjmInternalUtils.toUnderscoreName(propertyName);
					}
					colName = SjmInternalUtils.toLowerCase(colName);
					if (!columnNameToColumnInfo.containsKey(colName)) {
						throw new AnnotationException(colName + " column not found in table " + tableName
								+ " for model property " + clazz.getSimpleName() + "." + propertyName);
					}
					propNameToPropertyMapping.put(propertyName,
							new PropertyMapping(propertyName, field.getType().getName(), colName,
									columnNameToColumnInfo.get(colName).getColumnSqlType(),
									getDatabaseMetaDataOverrideSqlType(field.getType())));

				}
				processAnnotation(Id.class, field, tableName, propNameToPropertyMapping, columnNameToColumnInfo);
				processAnnotation(Version.class, field, tableName, propNameToPropertyMapping, columnNameToColumnInfo);
				processAnnotation(CreatedOn.class, field, tableName, propNameToPropertyMapping, columnNameToColumnInfo);
				processAnnotation(UpdatedOn.class, field, tableName, propNameToPropertyMapping, columnNameToColumnInfo);
				processAnnotation(CreatedBy.class, field, tableName, propNameToPropertyMapping, columnNameToColumnInfo);
				processAnnotation(UpdatedBy.class, field, tableName, propNameToPropertyMapping, columnNameToColumnInfo);
			}
			List<PropertyMapping> propertyMappings = new ArrayList<>(propNameToPropertyMapping.values());
			validateAnnotations(propertyMappings, clazz);
			tableMapping = new TableMapping(clazz, tableName, tableColumnInfo.getSchemaName(),
					tableColumnInfo.getCatalogName(), idPropertyInfo, propertyMappings);
			tableMappingCache.put(clazz.getName(), tableMapping);
		}
		return tableMapping;
	}

	// gets all unique fields including from super classes.
	public List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = getAllFieldsInternal(clazz);
		// there could be duplicate fields due to super classes. Get unique fields list
		// by name
		Set<String> set = new HashSet<>();
		return fields.stream().filter(p -> set.add(p.getName())).toList();
	}

	public String getCommonDatabaseName() {
		return JdbcUtils.commonDatabaseName(getDatabaseProductName());
	}

	public void setDatabaseMetaDataOverride(Map<Class<?>, Integer> databaseMetaDataOverrideMap) {
		if (this.databaseMetaDataOverrideMap == null) {
			Map<String, Integer> map = new HashMap<>();
			for (Map.Entry<Class<?>, Integer> entry : databaseMetaDataOverrideMap.entrySet()) {
				map.put(entry.getKey().getName(), entry.getValue());
			}
			this.databaseMetaDataOverrideMap = map;
		} else {
			throw new IllegalStateException("databaseMetaDataOverrideMap was already set and cannot be changed.");
		}
	}

	SimpleCache<String, TableMapping> getTableMappingCache() {
		return tableMappingCache;
	}

	private IdPropertyInfo getIdPropertyInfo(Class<?> clazz, List<Field> fields) {
		Id idAnnotation = null;
		String idPropertyName = null;
		boolean isIdAutoGenerated = false;
		for (Field field : fields) {
			idAnnotation = AnnotationUtils.findAnnotation(field, Id.class);
			if (idAnnotation != null) {
				idPropertyName = field.getName();
				if (idAnnotation.type() == IdType.AUTO_GENERATED) {
					if (field.getType().isPrimitive()) {
						throw new AnnotationException(clazz.getSimpleName() + "." + idPropertyName
								+ " is auto generated and it cannot be a primitive type.");
					}
					isIdAutoGenerated = true;
				}
				break;
			}
		}
		if (idAnnotation == null) {
			throw new AnnotationException(
					"@Id annotation not found in class " + clazz.getSimpleName() + " . It is required");
		}
		return new IdPropertyInfo(clazz, idPropertyName, isIdAutoGenerated);
	}

	private TableColumnInfo getTableColumnInfo(Class<?> clazz) {
		Table tableAnnotation = AnnotationUtils.findAnnotation(clazz, Table.class);
		validateTableAnnotation(tableAnnotation, clazz);
		String catalog = getCatalogForTable(tableAnnotation);
		String schema = getSchemaForTable(tableAnnotation);
		validateMetaDataConfig(catalog, schema);
		String tableName = tableAnnotation.name();
		List<ColumnInfo> columnInfoList = getColumnInfoFromTableMetadata(tableName, schema, catalog);
		if (SjmInternalUtils.isEmpty(columnInfoList)) {
			throw new AnnotationException(getTableMetaDataNotFoundErrMsg(clazz, tableName, schema, catalog));
		}
		return new TableColumnInfo(tableName, schema, catalog, columnInfoList);
	}

	private List<ColumnInfo> getColumnInfoFromTableMetadata(String tableName, String schema, String catalog) {
		Assert.hasLength(tableName, "tableName must not be empty");
		TableMetaDataProvider provider = SjmTableMetaDataProviderFactory.createMetaDataProvider(dataSource, catalog,
				schema, tableName);
		List<ColumnInfo> columnInfoList = new ArrayList<>();
		List<TableParameterMetaData> list = provider.getTableParameterMetaData();
		for (TableParameterMetaData metaData : list) {
			ColumnInfo columnInfo = new ColumnInfo(metaData.getParameterName(), metaData.getSqlType());
			columnInfoList.add(columnInfo);
		}
		return columnInfoList;
	}

	private <T extends Annotation> void processAnnotation(Class<T> annotationClazz, Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping, Map<String, ColumnInfo> columnNameToColumnInfo) {
		Annotation annotation = AnnotationUtils.findAnnotation(field, annotationClazz);
		if (annotation != null) {
			String propertyName = field.getName();
			PropertyMapping propMapping = propNameToPropertyMapping.get(propertyName);
			if (propMapping == null) { // it means there is no @Column annotation for the property
				String colName = SjmInternalUtils.toUnderscoreName(propertyName); // the default column name
				if (!columnNameToColumnInfo.containsKey(colName)) {
					throw new AnnotationException(
							colName + " column not found in table " + tableName + " for model property "
									+ field.getDeclaringClass().getSimpleName() + "." + field.getName());
				}
				propMapping = new PropertyMapping(propertyName, field.getType().getName(), colName,
						columnNameToColumnInfo.get(colName).getColumnSqlType());
				propNameToPropertyMapping.put(propertyName, propMapping);
			}
			BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(propMapping);
			// set idAnnotation, versionAnnotation, createdOnAnnotation etc on
			// PropertyMapping object
			bw.setPropertyValue(StringUtils.uncapitalize(annotationClazz.getSimpleName()) + "Annotation", true);
		}
	}

	private void validateTableAnnotation(Table tableAnnotation, Class<?> clazz) {
		if (tableAnnotation == null) {
			throw new AnnotationException(
					clazz.getSimpleName() + " does not have the @Table annotation. It is required");
		}
		if (SjmInternalUtils.isEmpty(tableAnnotation.name().trim())) {
			throw new AnnotationException("For " + clazz.getSimpleName() + " the @Table annotation has a blank name");
		}
	}

	private void validateAnnotations(List<PropertyMapping> propertyMappings, Class<?> clazz) {
		int idCnt = 0;
		int versionCnt = 0;
		int createdByCnt = 0;
		int createdOnCnt = 0;
		int updatedOnCnt = 0;
		int updatedByCnt = 0;
		for (PropertyMapping propMapping : propertyMappings) {
			int conflictCnt = 0;
			if (propMapping.isIdAnnotation()) {
				idCnt++;
				conflictCnt++;
			}
			if (propMapping.isVersionAnnotation()) {
				versionCnt++;
				conflictCnt++;
			}
			if (propMapping.isCreatedOnAnnotation()) {
				createdOnCnt++;
				conflictCnt++;
			}
			if (propMapping.isCreatedByAnnotation()) {
				createdByCnt++;
				conflictCnt++;
			}
			if (propMapping.isUpdatedOnAnnotation()) {
				updatedOnCnt++;
				conflictCnt++;
			}
			if (propMapping.isUpdatedByAnnotation()) {
				updatedByCnt++;
				conflictCnt++;
			}
			if (propMapping.isVersionAnnotation() && !isIntegerClass(propMapping.getPropertyClassName())) {
				throw new AnnotationException("@Version requires the type of property " + clazz.getSimpleName() + "."
						+ propMapping.getPropertyName() + " to be Integer");
			}
			/*
			 * if (propMapping.isCreatedOnAnnotation() &&
			 * !isLocalDateTimeClass(propMapping.getPropertyClassName())) { throw new
			 * AnnotationException("@CreatedOn requires the type of property " +
			 * clazz.getSimpleName() + "." + propMapping.getPropertyName() +
			 * " to be LocalDateTime"); } if (propMapping.isUpdatedOnAnnotation() &&
			 * !isLocalDateTimeClass(propMapping.getPropertyClassName())) { throw new
			 * AnnotationException("@UpdatedOn requires the type of property " +
			 * clazz.getSimpleName() + "." + propMapping.getPropertyName() +
			 * " to be LocalDateTime"); }
			 */
			if (conflictCnt > 1) {
				throw new AnnotationException(clazz.getSimpleName() + "." + propMapping.getPropertyName()
						+ " has multiple annotations that conflict");
			}
		}
		if (idCnt > 1) {
			throw new AnnotationException(" model " + clazz.getSimpleName() + " has multiple @Id annotations");
		}
		if (versionCnt > 1) {
			throw new AnnotationException(" model " + clazz.getSimpleName() + " has multiple @Version annotations");
		}
		if (createdOnCnt > 1) {
			throw new AnnotationException(" model " + clazz.getSimpleName() + " has multiple @CreatedOn annotations");
		}
		if (createdByCnt > 1) {
			throw new AnnotationException(" model " + clazz.getSimpleName() + " has multiple @CreatedBy annotations");
		}
		if (updatedOnCnt > 1) {
			throw new AnnotationException(" model " + clazz.getSimpleName() + " has multiple @UpdatedOn annotations");
		}
		if (updatedByCnt > 1) {
			throw new AnnotationException(" model " + clazz.getSimpleName() + " has multiple @UpdatedBy annotations");
		}
	}

	private String getCatalogForTable(Table tableAnnotation) {
		return SjmInternalUtils.isEmpty(tableAnnotation.catalog()) ? this.catalogName : tableAnnotation.catalog();
	}

	private String getSchemaForTable(Table tableAnnotation) {
		return SjmInternalUtils.isEmpty(tableAnnotation.schema()) ? this.schemaName : tableAnnotation.schema();
	}

	private String getTableMetaDataNotFoundErrMsg(Class<?> clazz, String tableName, String schema, String catalog) {
		String errMsg = "Unable to locate meta-data for table '" + tableName + "'";
		if (schema != null && catalog != null) {
			errMsg += " in schema " + schema + " and catalog " + catalog;
		} else {
			if (schema != null) {
				errMsg += " in schema " + schema;
			}
			if (catalog != null) {
				errMsg += " in catalog/database " + catalog;
			}
		}
		errMsg += " for class " + clazz.getSimpleName();
		return errMsg;
	}

	// get all fields including fields in super classes.
	private List<Field> getAllFieldsInternal(Class<?> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}
		List<Field> result = new ArrayList<>(getAllFields(clazz.getSuperclass()));
		List<Field> fields = Arrays.stream(clazz.getDeclaredFields()).toList();
		result.addAll(0, fields);
		return result;
	}

	private void validateMetaDataConfig(String catalogName, String schemaName) {
		String commonDatabaseName = JdbcUtils.commonDatabaseName(getDatabaseProductName());
		if ("mysql".equalsIgnoreCase(commonDatabaseName) && SjmInternalUtils.isNotEmpty(schemaName)) {
			throw new MapperException(commonDatabaseName
					+ ": When creating SimpleJdbcMapper() if you are using 'schema' (argument 2) use 'catalog' (argument 3) instead."
					+ " If you are using the @Table annotation use the 'catalog' attribue instead of 'schema' attribute");
		}
		if ("oracle".equalsIgnoreCase(commonDatabaseName) && SjmInternalUtils.isNotEmpty(catalogName)) {
			throw new MapperException(commonDatabaseName
					+ ": When creating SimpleJdbcMapper() if you are using the 'catalog' (argument 3) use 'schema' (argument 2) instead."
					+ " If you are using the @Table annotation use the 'schema' attribue instead of 'catalog' attribute");
		}
	}

	private String getDatabaseProductName() {
		// databaseProductName is not a volatile variable. No side effects even if
		// contention
		if (this.databaseProductName != null) {
			return this.databaseProductName;
		} else {
			// this synchronized block only runs once
			synchronized (this) {
				if (this.databaseProductName == null) {
					try {
						this.databaseProductName = JdbcUtils.extractDatabaseMetaData(dataSource,
								new DatabaseMetaDataCallback<String>() {
									public String processMetaData(DatabaseMetaData dbMetaData)
											throws SQLException, MetaDataAccessException {
										return dbMetaData.getDatabaseProductName() == null ? ""
												: dbMetaData.getDatabaseProductName();
									}
								});
					} catch (Exception e) {
						throw new MapperException(e);
					}
				}
				return this.databaseProductName;
			}
		}
	}

	private Integer getDatabaseMetaDataOverrideSqlType(Class<?> clazz) {
		return databaseMetaDataOverrideMap == null ? null : databaseMetaDataOverrideMap.get(clazz.getName());
	}

	private boolean isIntegerClass(String className) {
		return "java.lang.Integer".equals(className);
	}

}

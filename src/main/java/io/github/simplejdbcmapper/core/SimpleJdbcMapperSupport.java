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
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.metadata.TableMetaDataContext;
import org.springframework.jdbc.core.metadata.TableMetaDataProvider;
import org.springframework.jdbc.core.metadata.TableMetaDataProviderFactory;
import org.springframework.jdbc.core.metadata.TableParameterMetaData;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
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

	private final JdbcClient jdbcClient;

	private final JdbcTemplate jdbcTemplate;

	private final NamedParameterJdbcTemplate npJdbcTemplate;

	private String databaseProductName;

	private boolean enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone = false;

	// Using Spring's DefaultConversionService as default conversionService for
	// SimpleJdbcMapper
	private ConversionService conversionService = DefaultConversionService.getSharedInstance();

	private Supplier<?> recordAuditedOnSupplier;
	private Supplier<?> recordAuditedBySupplier;

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

		this.npJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		this.jdbcTemplate = npJdbcTemplate.getJdbcTemplate();
		this.jdbcClient = JdbcClient.create(jdbcTemplate);
	}

	public DataSource getDataSource() {
		return this.dataSource;
	}

	public JdbcClient getJdbcClient() {
		return this.jdbcClient;
	}

	public JdbcTemplate getJdbcTemplate() {
		return this.jdbcTemplate;
	}

	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return this.npJdbcTemplate;
	}

	public <T> void setRecordAuditedBySupplier(Supplier<T> supplier) {
		if (recordAuditedBySupplier == null) {
			recordAuditedBySupplier = supplier;
		} else {
			throw new IllegalStateException("recordAuditedBySupplier was already set and cannot be changed.");
		}
	}

	public <T> void setRecordAuditedOnSupplier(Supplier<T> supplier) {
		if (recordAuditedOnSupplier == null) {
			recordAuditedOnSupplier = supplier;
		} else {
			throw new IllegalStateException("recordAuditedOnSupplier was already set and cannot be changed.");
		}
	}

	@SuppressWarnings("rawtypes")
	public Supplier getRecordAuditedBySupplier() {
		return recordAuditedBySupplier;
	}

	public @SuppressWarnings("rawtypes") Supplier getRecordAuditedOnSupplier() {
		return recordAuditedOnSupplier;
	}

	public ConversionService getConversionService() {
		return conversionService;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public BeanWrapper getBeanWrapper(Object obj) {
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(obj);
		bw.setConversionService(conversionService);
		return bw;
	}

	public <T> BeanPropertyRowMapper<T> getBeanPropertyRowMapper(Class<T> clazz) {
		BeanPropertyRowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(clazz);
		rowMapper.setConversionService(this.conversionService);
		return rowMapper;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public TableMapping getTableMapping(Class<?> clazz) {
		Assert.notNull(clazz, "clazz must not be null");
		TableMapping tableMapping = tableMappingCache.get(clazz.getName());
		if (tableMapping == null) {
			Table tableAnnotation = getTableAnnotation(clazz);
			String tableName = tableAnnotation.name();
			String catalog = getCatalogForTable(tableAnnotation);
			String schema = getSchemaForTable(tableAnnotation);
			validateMetaDataConfig(catalog, schema);
			List<Field> fields = getAllFields(clazz);
			IdPropertyInfo idPropertyInfo = getIdPropertyInfo(clazz, fields);
			// key:column name, value: TableParameterMetaData
			Map<String, TableParameterMetaData> columnNameToTpmd = getTableParameterMetadataList(tableName, schema,
					catalog, clazz).stream().collect(Collectors.toMap(o -> o.getParameterName(), o -> o));
			// key:propertyName, value:PropertyMapping. LinkedHashMap to maintain order of
			// properties
			Map<String, PropertyMapping> propNameToPropertyMapping = new LinkedHashMap<>();
			for (Field field : fields) {
				// process column annotation always first
				processColumnAnnotation(field, tableName, propNameToPropertyMapping, columnNameToTpmd);
				processIdAnnotation(field, tableName, propNameToPropertyMapping, columnNameToTpmd);
				processVersionAnnotation(field, tableName, propNameToPropertyMapping, columnNameToTpmd);
				processCreatedOnAnnotation(field, tableName, propNameToPropertyMapping, columnNameToTpmd);
				processUpdatedOnAnnotation(field, tableName, propNameToPropertyMapping, columnNameToTpmd);
				processCreatedByAnnotation(field, tableName, propNameToPropertyMapping, columnNameToTpmd);
				processUpdatedByAnnotation(field, tableName, propNameToPropertyMapping, columnNameToTpmd);
			}
			List<PropertyMapping> propertyMappings = new ArrayList<>(propNameToPropertyMapping.values());
			validateAnnotations(propertyMappings, clazz);
			tableMapping = new TableMapping(clazz, tableName, schema, catalog, idPropertyInfo, propertyMappings);
			tableMappingCache.put(clazz.getName(), tableMapping);
		}
		return tableMapping;
	}

	// gets all unique fields including from super classes.
	public List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = getAllFieldsInternal(clazz);
		// there could be duplicate fields due to super classes. Get unique fields list
		// by name
		Set<String> set = new LinkedHashSet<>();
		return fields.stream().filter(p -> set.add(p.getName())).toList();
	}

	public String getCommonDatabaseName() {
		return JdbcUtils.commonDatabaseName(getDatabaseProductName());
	}

	public void enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone() {
		enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone = true;
	}

	public SimpleCache<String, TableMapping> getTableMappingCache() {
		return tableMappingCache;
	}

	private IdPropertyInfo getIdPropertyInfo(Class<?> clazz, List<Field> fields) {
		Id idAnnotation = null;
		String idPropertyName = null;
		boolean isIdAutoGenerated = false;
		for (Field field : fields) {
			idAnnotation = AnnotationUtils.findAnnotation(field, Id.class);
			if (idAnnotation != null) {
				if (field.getType().isPrimitive()) {
					throw new AnnotationException(
							clazz.getSimpleName() + "." + idPropertyName + " is an id and cannot be a primitive type.");
				}
				idPropertyName = field.getName();
				if (idAnnotation.type() == IdType.AUTO_GENERATED) {
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

	private Table getTableAnnotation(Class<?> clazz) {
		Table tableAnnotation = AnnotationUtils.findAnnotation(clazz, Table.class);
		validateTableAnnotation(tableAnnotation, clazz);
		return tableAnnotation;
	}

	private void processColumnAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		Column colAnnotation = AnnotationUtils.findAnnotation(field, Column.class);
		if (colAnnotation != null) {
			String propertyName = field.getName();
			String colName = colAnnotation.name();
			if ("[DEFAULT]".equals(colName)) {
				colName = InternalUtils.toUnderscoreName(propertyName);
			}
			colName = InternalUtils.toLowerCase(colName);
			if (!columnNameToTpmd.containsKey(colName)) {
				throw new AnnotationException(colName + " column not found in table " + tableName
						+ " for model property " + field.getDeclaringClass().getSimpleName() + "." + propertyName);
			}
			propNameToPropertyMapping.put(propertyName,
					new PropertyMapping(propertyName, field.getType().getName(), colName,
							columnNameToTpmd.get(colName).getSqlType(),
							getDatabaseMetaDataOverrideSqlType(field.getType())));

		}
	}

	private void processIdAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(Id.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	private void processVersionAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(Version.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	private void processCreatedOnAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(CreatedOn.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	private void processUpdatedOnAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(UpdatedOn.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	private void processCreatedByAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(CreatedBy.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	private void processUpdatedByAnnotation(Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		processAnnotation(UpdatedBy.class, field, tableName, propNameToPropertyMapping, columnNameToTpmd);
	}

	private <T extends Annotation> void processAnnotation(Class<T> annotationClazz, Field field, String tableName,
			Map<String, PropertyMapping> propNameToPropertyMapping,
			Map<String, TableParameterMetaData> columnNameToTpmd) {
		Annotation annotation = AnnotationUtils.findAnnotation(field, annotationClazz);
		if (annotation != null) {
			String propertyName = field.getName();
			PropertyMapping propMapping = propNameToPropertyMapping.get(propertyName);
			if (propMapping == null) { // it means there is no @Column annotation for the property
				String colName = InternalUtils.toUnderscoreName(propertyName); // the default column name
				if (!columnNameToTpmd.containsKey(colName)) {
					throw new AnnotationException(
							colName + " column not found in table " + tableName + " for model property "
									+ field.getDeclaringClass().getSimpleName() + "." + field.getName());
				}
				propMapping = new PropertyMapping(propertyName, field.getType().getName(), colName,
						columnNameToTpmd.get(colName).getSqlType());
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
		if (InternalUtils.isBlank(tableAnnotation.name())) {
			throw new AnnotationException("For " + clazz.getSimpleName() + " the @Table annotation has a blank name");
		}
	}

	private void validateAnnotations(List<PropertyMapping> propertyMappings, Class<?> clazz) {
		annotationDuplicateCheck(propertyMappings, clazz);
		annotationConflictCheck(propertyMappings, clazz);
		annotationVersionTypeCheck(propertyMappings, clazz);
	}

	private void annotationDuplicateCheck(List<PropertyMapping> propertyMappings, Class<?> clazz) {
		int idCnt = 0;
		int versionCnt = 0;
		int createdByCnt = 0;
		int createdOnCnt = 0;
		int updatedOnCnt = 0;
		int updatedByCnt = 0;
		for (PropertyMapping propMapping : propertyMappings) {
			if (propMapping.isIdAnnotation()) {
				idCnt++;
			}
			if (propMapping.isVersionAnnotation()) {
				versionCnt++;
			}
			if (propMapping.isCreatedOnAnnotation()) {
				createdOnCnt++;
			}
			if (propMapping.isCreatedByAnnotation()) {
				createdByCnt++;
			}
			if (propMapping.isUpdatedOnAnnotation()) {
				updatedOnCnt++;
			}
			if (propMapping.isUpdatedByAnnotation()) {
				updatedByCnt++;
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

	private void annotationConflictCheck(List<PropertyMapping> propertyMappings, Class<?> clazz) {
		for (PropertyMapping propMapping : propertyMappings) {
			int conflictCnt = 0;
			if (propMapping.isIdAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isVersionAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isCreatedOnAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isCreatedByAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isUpdatedOnAnnotation()) {
				conflictCnt++;
			}
			if (propMapping.isUpdatedByAnnotation()) {
				conflictCnt++;
			}
			if (conflictCnt > 1) {
				throw new AnnotationException(clazz.getSimpleName() + "." + propMapping.getPropertyName()
						+ " has multiple annotations that conflict");
			}
		}
	}

	private void annotationVersionTypeCheck(List<PropertyMapping> propertyMappings, Class<?> clazz) {
		for (PropertyMapping propMapping : propertyMappings) {
			if (propMapping.isVersionAnnotation() && !isIntegerClass(propMapping.getPropertyClassName())) {
				throw new AnnotationException("@Version requires the type of property " + clazz.getSimpleName() + "."
						+ propMapping.getPropertyName() + " to be Integer");
			}
		}
	}

	private List<TableParameterMetaData> getTableParameterMetadataList(String tableName, String schema, String catalog,
			Class<?> clazz) {
		if (InternalUtils.isBlank(tableName)) {
			throw new IllegalArgumentException("tableName must not be blank");
		}
		TableMetaDataContext tableMetaDataContext = createNewTableMetaDataContext(tableName, schema, catalog);
		TableMetaDataProvider provider = TableMetaDataProviderFactory.createMetaDataProvider(dataSource,
				tableMetaDataContext);
		List<TableParameterMetaData> tpmdList = provider.getTableParameterMetaData();
		if (InternalUtils.isEmpty(tpmdList)) {
			throw new AnnotationException(getTableMetaDataNotFoundErrMsg(clazz, tableName, schema, catalog));
		}
		return tpmdList;
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

	private void validateMetaDataConfig(String catalog, String schema) {
		String commonDatabaseName = JdbcUtils.commonDatabaseName(getDatabaseProductName());
		if ("mysql".equalsIgnoreCase(commonDatabaseName) && InternalUtils.isNotEmpty(schema)) {
			throw new MapperException(commonDatabaseName
					+ ": When creating SimpleJdbcMapper() if you are using 'schema' (argument 2) use 'catalog' (argument 3) instead."
					+ " If you are using the @Table annotation use the 'catalog' attribue instead of 'schema' attribute");
		}
		if ("oracle".equalsIgnoreCase(commonDatabaseName) && InternalUtils.isNotEmpty(catalog)) {
			throw new MapperException(commonDatabaseName
					+ ": When creating SimpleJdbcMapper() if you are using the 'catalog' (argument 3) use 'schema' (argument 2) instead."
					+ " If you are using the @Table annotation use the 'schema' attribue instead of 'catalog' attribute");
		}
	}

	private String getCatalogForTable(Table tableAnnotation) {
		return InternalUtils.isBlank(tableAnnotation.catalog()) ? this.catalogName : tableAnnotation.catalog();
	}

	private String getSchemaForTable(Table tableAnnotation) {
		return InternalUtils.isBlank(tableAnnotation.schema()) ? this.schemaName : tableAnnotation.schema();
	}

	// get all fields including fields in super classes.
	private List<Field> getAllFieldsInternal(Class<?> clazz) {
		if (clazz == null) {
			return Collections.emptyList();
		}
		List<Field> result = new ArrayList<>(getAllFieldsInternal(clazz.getSuperclass()));
		List<Field> fields = Arrays.stream(clazz.getDeclaredFields()).toList();
		result.addAll(0, fields);
		return result;
	}

	private String getDatabaseProductName() {
		// No side effects even if there is thread contention and it gets set more than
		// once
		if (databaseProductName != null) {
			return databaseProductName;
		} else {
			synchronized (this) {
				if (databaseProductName == null) {
					try {
						databaseProductName = JdbcUtils.extractDatabaseMetaData(dataSource,
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
				return databaseProductName;
			}
		}
	}

	private Integer getDatabaseMetaDataOverrideSqlType(Class<?> clazz) {
		if (enableOffsetDateTimeSqlTypeAsTimestampWithTimeZone && clazz != null
				&& OffsetDateTime.class.isAssignableFrom(clazz)) {
			return Types.TIMESTAMP_WITH_TIMEZONE;
		}
		return null;
	}

	private TableMetaDataContext createNewTableMetaDataContext(String table, String schema, String catalog) {
		TableMetaDataContext tableMetaDataContext = new TableMetaDataContext();
		tableMetaDataContext.setTableName(table);
		tableMetaDataContext.setSchemaName(schema);
		tableMetaDataContext.setCatalogName(catalog);
		tableMetaDataContext.setAccessTableColumnMetaData(true);
		tableMetaDataContext.setOverrideIncludeSynonymsDefault(true);
		return tableMetaDataContext;
	}

	private boolean isIntegerClass(String className) {
		return "java.lang.Integer".equals(className);
	}
}

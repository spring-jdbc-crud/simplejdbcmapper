package io.github.simplejdbcmapper.core;

import java.lang.reflect.Field;

import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class TestUtils {
	public static SimpleJdbcMapperSupport getSimpleJdbcMapperSupport(SimpleJdbcMapper sjm) {
		try {
			Field field = sjm.getClass().getDeclaredField("simpleJdbcMapperSupport");
			field.setAccessible(true);
			return (SimpleJdbcMapperSupport) field.get(sjm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static FindOperation getFindOperation(SimpleJdbcMapper sjm) {
		try {
			Field field = sjm.getClass().getDeclaredField("findOperation");
			field.setAccessible(true);

			return (FindOperation) field.get(sjm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static InsertOperation getInsertOperation(SimpleJdbcMapper sjm) {
		try {
			Field field = sjm.getClass().getDeclaredField("insertOperation");
			field.setAccessible(true);

			return (InsertOperation) field.get(sjm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static UpdateOperation getUpdateOperation(SimpleJdbcMapper sjm) {
		try {
			Field field = sjm.getClass().getDeclaredField("updateOperation");
			field.setAccessible(true);

			return (UpdateOperation) field.get(sjm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static DeleteOperation getDeleteOperation(SimpleJdbcMapper sjm) {
		try {
			Field field = sjm.getClass().getDeclaredField("deleteOperation");
			field.setAccessible(true);

			return (DeleteOperation) field.get(sjm);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static TableMappingProvider getTableMappingProvider(SimpleJdbcMapperSupport sjms) {
		try {
			Field field = sjms.getClass().getDeclaredField("tableMappingProvider");
			field.setAccessible(true);
			return (TableMappingProvider) field.get(sjms);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static SqlColumnsProvider getSqlColumnsProvider(SimpleJdbcMapperSupport sjms) {
		try {
			Field field = sjms.getClass().getDeclaredField("sqlColumnsProvider");
			field.setAccessible(true);
			return (SqlColumnsProvider) field.get(sjms);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static SimpleCache<Class<?>, String> getSqlColumnsCache(SqlColumnsProvider scp) {
		try {
			Field field = scp.getClass().getDeclaredField("sqlColumnsCache");
			field.setAccessible(true);
			return (SimpleCache<Class<?>, String>) field.get(scp);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static SimpleCache<String, String> getSqlColumnsAliasCache(SqlColumnsProvider scp) {
		try {
			Field field = scp.getClass().getDeclaredField("sqlColumnsAliasCache");
			field.setAccessible(true);
			return (SimpleCache<String, String>) field.get(scp);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static SimpleCache<Class<?>, TableMapping> getTableMappingCache(TableMappingProvider tmpr) {
		try {
			Field field = tmpr.getClass().getDeclaredField("tableMappingCache");
			field.setAccessible(true);
			return (SimpleCache<Class<?>, TableMapping>) field.get(tmpr);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static SimpleCache<Class<?>, String> getFindByIdSqlCache(FindOperation fo) {
		try {
			Field field = fo.getClass().getDeclaredField("findByIdSqlCache");
			field.setAccessible(true);
			return (SimpleCache<Class<?>, String>) field.get(fo);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static SimpleCache<Class<?>, SimpleJdbcInsert> getInsertSqlCache(InsertOperation io) {
		try {
			Field field = io.getClass().getDeclaredField("insertSqlCache");
			field.setAccessible(true);
			return (SimpleCache<Class<?>, SimpleJdbcInsert>) field.get(io);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static SimpleCache<Class<?>, SqlAndParams> getUpdateSqlCache(UpdateOperation uo) {
		try {
			Field field = uo.getClass().getDeclaredField("updateSqlCache");
			field.setAccessible(true);
			return (SimpleCache<Class<?>, SqlAndParams>) field.get(uo);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static SimpleCache<String, SqlAndParams> getUpdateSpecificPropertiesSqlCache(UpdateOperation uo) {
		try {
			Field field = uo.getClass().getDeclaredField("updateSpecificPropertiesSqlCache");
			field.setAccessible(true);
			return (SimpleCache<String, SqlAndParams>) field.get(uo);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static SimpleCache<Class<?>, String> getDeleteSqlCache(DeleteOperation dop) {
		try {
			Field field = dop.getClass().getDeclaredField("deleteSqlCache");
			field.setAccessible(true);
			return (SimpleCache<Class<?>, String>) field.get(dop);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

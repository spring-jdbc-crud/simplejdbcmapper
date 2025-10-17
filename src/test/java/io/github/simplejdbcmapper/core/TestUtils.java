package io.github.simplejdbcmapper.core;

import java.lang.reflect.Field;

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

	public static TableMappingHelper getTableMappingHelper(SimpleJdbcMapper sjm) {
		try {
			Field field = sjm.getClass().getDeclaredField("tableMappingHelper");
			field.setAccessible(true);

			return (TableMappingHelper) field.get(sjm);
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
}

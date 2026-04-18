package io.github.simplejdbcmapper.type;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;

public class HandlerFactory {

	private static TypeHandler bigDecimalTypeHandler = new BigDecimalTypeHandler();
	private static TypeHandler blobTypeHandler = new BlobTypeHandler();

	private static TypeHandler booleanTypeHandler = new BooleanTypeHandler();
	private static TypeHandler byteArrayTypeHandler = new ByteArrayTypeHandler();
	private static TypeHandler byteTypeHandler = new ByteTypeHandler();

	private static TypeHandler clobTypeHandler = new ClobTypeHandler();

	private static TypeHandler dateTypeHandler = new DateTypeHandler();

	private static TypeHandler doubleTypeHandler = new DoubleTypeHandler();

	private static TypeHandler enumTypeHandler = new EnumTypeHandler();

	private static TypeHandler floatTypeHandler = new FloatTypeHandler();

	private static TypeHandler integerTypeHandler = new IntegerTypeHandler();
	private static TypeHandler longTypeHandler = new LongTypeHandler();

	private static TypeHandler shortTypeHandler = new ShortTypeHandler();

	private static TypeHandler timestampTypeHandler = new TimestampTypeHandler();

	private static TypeHandler timeTypeHandler = new TimeTypeHandler();

	private static TypeHandler utilDateTypeHandler = new UtilDateTypeHandler();

	private static TypeHandler defaultTypeHandler = new DefaultTypeHandler();

	public static TypeHandler getTypeHandler(Class<?> type) {
		if (BigDecimal.class == type) {
			return bigDecimalTypeHandler;
		} else if (Blob.class == type) {
			return blobTypeHandler;
		} else if (Boolean.class == type) {
			return booleanTypeHandler;
		} else if (byte[].class == type) {
			return byteArrayTypeHandler;
		} else if (Byte.class == type) {
			return byteTypeHandler;
		} else if (Clob.class == type) {
			return clobTypeHandler;
		} else if (java.sql.Date.class == type) {
			return dateTypeHandler;
		} else if (Double.class == type) {
			return doubleTypeHandler;
		} else if (Enum.class == type) {
			return enumTypeHandler;
		} else if (Float.class == type) {
			return floatTypeHandler;
		} else if (Integer.class == type) {
			return integerTypeHandler;
		} else if (Long.class == type) {
			return longTypeHandler;
		} else if (Short.class == type) {
			return shortTypeHandler;
		} else if (java.sql.Timestamp.class == type) {
			return timestampTypeHandler;
		} else if (java.sql.Time.class == type) {
			return timeTypeHandler;
		} else if (java.util.Date.class == type) {
			return utilDateTypeHandler;
		}

		return defaultTypeHandler;

	}

}

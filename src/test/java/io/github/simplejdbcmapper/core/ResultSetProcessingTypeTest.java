package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.StatusEnum;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ResultSetProcessingTypeTest {
	@Test
	void resultSetType_fromClass_Test() {

		assertEquals(ResultSetProcessingType.STRING, ResultSetProcessingType.getResultSetProcessingType(String.class, false));

		assertEquals(ResultSetProcessingType.BOOLEAN, ResultSetProcessingType.getResultSetProcessingType(Boolean.class, false));

		assertEquals(ResultSetProcessingType.BYTE, ResultSetProcessingType.getResultSetProcessingType(Byte.class, false));

		assertEquals(ResultSetProcessingType.SHORT, ResultSetProcessingType.getResultSetProcessingType(Short.class, false));

		assertEquals(ResultSetProcessingType.INTEGER, ResultSetProcessingType.getResultSetProcessingType(Integer.class, false));

		assertEquals(ResultSetProcessingType.LONG, ResultSetProcessingType.getResultSetProcessingType(Long.class, false));

		assertEquals(ResultSetProcessingType.FLOAT, ResultSetProcessingType.getResultSetProcessingType(Float.class, false));

		assertEquals(ResultSetProcessingType.DOUBLE, ResultSetProcessingType.getResultSetProcessingType(Double.class, false));

		assertEquals(ResultSetProcessingType.NUMBER, ResultSetProcessingType.getResultSetProcessingType(Number.class, false));

		assertEquals(ResultSetProcessingType.BIGDECIMAL, ResultSetProcessingType.getResultSetProcessingType(BigDecimal.class, false));

		assertEquals(ResultSetProcessingType.DATE, ResultSetProcessingType.getResultSetProcessingType(java.sql.Date.class, false));

		assertEquals(ResultSetProcessingType.TIME, ResultSetProcessingType.getResultSetProcessingType(java.sql.Time.class, false));
		assertEquals(ResultSetProcessingType.TIMESTAMP, ResultSetProcessingType.getResultSetProcessingType(java.sql.Timestamp.class, false));
		assertEquals(ResultSetProcessingType.UTILDATE, ResultSetProcessingType.getResultSetProcessingType(java.util.Date.class, false));

		assertEquals(ResultSetProcessingType.BYTEARRAY, ResultSetProcessingType.getResultSetProcessingType(byte[].class, false));
		assertEquals(ResultSetProcessingType.BLOB, ResultSetProcessingType.getResultSetProcessingType(Blob.class, false));
		assertEquals(ResultSetProcessingType.CLOB, ResultSetProcessingType.getResultSetProcessingType(Clob.class, false));

		assertEquals(ResultSetProcessingType.ENUM, ResultSetProcessingType.getResultSetProcessingType(StatusEnum.class, false));

		assertEquals(ResultSetProcessingType.UNKNOWN, ResultSetProcessingType.getResultSetProcessingType(char[].class, false));

		assertEquals(ResultSetProcessingType.CUSTOMCONVERTER, ResultSetProcessingType.getResultSetProcessingType(Integer.class, true));

	}
}

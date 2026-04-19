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
public class ResultSetTypeTest {
	@Test
	void resultSetType_fromClass_Test() {

		assertEquals(ResultSetType.STRING, ResultSetType.getResultSetType(String.class));

		assertEquals(ResultSetType.BOOLEAN, ResultSetType.getResultSetType(Boolean.class));

		assertEquals(ResultSetType.BYTE, ResultSetType.getResultSetType(Byte.class));

		assertEquals(ResultSetType.SHORT, ResultSetType.getResultSetType(Short.class));

		assertEquals(ResultSetType.STRING, ResultSetType.getResultSetType(String.class));

		assertEquals(ResultSetType.INTEGER, ResultSetType.getResultSetType(Integer.class));

		assertEquals(ResultSetType.LONG, ResultSetType.getResultSetType(Long.class));

		assertEquals(ResultSetType.FLOAT, ResultSetType.getResultSetType(Float.class));

		assertEquals(ResultSetType.DOUBLE, ResultSetType.getResultSetType(Double.class));

		assertEquals(ResultSetType.NUMBER, ResultSetType.getResultSetType(Number.class));

		assertEquals(ResultSetType.BIGDECIMAL, ResultSetType.getResultSetType(BigDecimal.class));

		assertEquals(ResultSetType.DATE, ResultSetType.getResultSetType(java.sql.Date.class));

		assertEquals(ResultSetType.TIME, ResultSetType.getResultSetType(java.sql.Time.class));
		assertEquals(ResultSetType.TIMESTAMP, ResultSetType.getResultSetType(java.sql.Timestamp.class));
		assertEquals(ResultSetType.UTILDATE, ResultSetType.getResultSetType(java.util.Date.class));

		assertEquals(ResultSetType.BYTEARRAY, ResultSetType.getResultSetType(byte[].class));
		assertEquals(ResultSetType.BLOB, ResultSetType.getResultSetType(Blob.class));
		assertEquals(ResultSetType.CLOB, ResultSetType.getResultSetType(Clob.class));

		assertEquals(ResultSetType.ENUM, ResultSetType.getResultSetType(StatusEnum.class));

		assertEquals(ResultSetType.UNKNOWN, ResultSetType.getResultSetType(char[].class));

	}
}

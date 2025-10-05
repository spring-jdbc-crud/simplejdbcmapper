package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.StatusEnum;
import io.github.simplejdbcmapper.model.TypeCheckSqlServer;
import microsoft.sql.DateTimeOffset;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TypeCheckSqlServerTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private SimpleJdbcMapper sjm;

	@BeforeEach
	void beforeMethod() {
		// tests will only run if sqlserver
		if (!jdbcDriver.contains("sqlserver")) {
			Assumptions.assumeTrue(false);
		}
	}

	@Test
	void insert_TypeCheckSqlServerTest() {
		TypeCheckSqlServer iObj = new TypeCheckSqlServer();

		var localDateVal = LocalDate.now();
		iObj.setLocalDateData(localDateVal);

		var dateVal = new Date();
		iObj.setJavaUtilDateData(dateVal);

		var localDateTimeVal = LocalDateTime.now();
		iObj.setLocalDateTimeData(localDateTimeVal);

		var bigDecimalVal = new BigDecimal("10.23");
		iObj.setBigDecimalData(bigDecimalVal);

		iObj.setBooleanVal(true);

		iObj.setImage(new byte[] { 10, 20, 30 });

		iObj.setStatus(StatusEnum.OPEN);

		var offsetVal = DateTimeOffset.valueOf(OffsetDateTime.now());
		iObj.setOffsetDateTimeData(offsetVal);

		iObj.setClobData("123456789".toCharArray());

		sjm.insert(iObj);

		TypeCheckSqlServer tc = sjm.findById(TypeCheckSqlServer.class, iObj.getId());
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		assertEquals(localDateVal, tc.getLocalDateData());

		assertEquals(fmt.format(dateVal), fmt.format(tc.getJavaUtilDateData()));

		DateTimeFormatter ldtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		assertEquals(ldtFmt.format(localDateTimeVal), ldtFmt.format(tc.getLocalDateTimeData()));

		assertEquals(0, tc.getBigDecimalData().compareTo(iObj.getBigDecimalData()));

		assertArrayEquals(iObj.getImage(), tc.getImage());

		assertTrue(tc.getBooleanVal());

		assertEquals(StatusEnum.OPEN, tc.getStatus());

		assertEquals(offsetVal, tc.getOffsetDateTimeData());

		assertNotNull(tc.getImage());
		assertNotNull(tc.getClobData());

	}

	@Test
	void update_TypeCheckSqlServerTest() {
		TypeCheckSqlServer iObj = new TypeCheckSqlServer();
		sjm.insert(iObj);

		TypeCheckSqlServer uObj = sjm.findById(TypeCheckSqlServer.class, iObj.getId());
		var localDateVal = LocalDate.now();
		uObj.setLocalDateData(localDateVal);

		var dateVal = new Date();
		uObj.setJavaUtilDateData(dateVal);

		var localDateTimeVal = LocalDateTime.now();
		uObj.setLocalDateTimeData(localDateTimeVal);

		var bigDecimalVal = new BigDecimal("10.23");
		uObj.setBigDecimalData(bigDecimalVal);

		uObj.setBooleanVal(true);

		uObj.setImage(new byte[] { 10, 20, 30 });

		uObj.setStatus(StatusEnum.OPEN);

		var offsetVal = DateTimeOffset.valueOf(OffsetDateTime.now());
		uObj.setOffsetDateTimeData(offsetVal);

		uObj.setClobData("123456789".toCharArray());

		sjm.update(uObj);

		TypeCheckSqlServer tc = sjm.findById(TypeCheckSqlServer.class, uObj.getId());
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		assertEquals(localDateVal, tc.getLocalDateData());

		assertEquals(fmt.format(dateVal), fmt.format(tc.getJavaUtilDateData()));

		DateTimeFormatter ldtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		assertEquals(ldtFmt.format(localDateTimeVal), ldtFmt.format(tc.getLocalDateTimeData()));

		assertEquals(0, tc.getBigDecimalData().compareTo(uObj.getBigDecimalData()));

		assertArrayEquals(uObj.getImage(), tc.getImage());

		assertTrue(tc.getBooleanVal());

		assertEquals(StatusEnum.OPEN, tc.getStatus());

		assertEquals(offsetVal, tc.getOffsetDateTimeData());

		assertNotNull(tc.getImage());
		assertNotNull(tc.getClobData());
	}

}

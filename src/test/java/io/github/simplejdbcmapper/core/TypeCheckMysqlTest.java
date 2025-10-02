package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
import io.github.simplejdbcmapper.model.TypeCheckMysql;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TypeCheckMysqlTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private SimpleJdbcMapper sjm;

	@BeforeEach
	void beforeMethod() {
		// tests will run only if mysql
		if (!jdbcDriver.contains("mysql")) {
			Assumptions.assumeTrue(false);
		}
	}

	@Test
	void insert_TypeCheckMysqlTest() {
		TypeCheckMysql obj = new TypeCheckMysql();

		obj.setLocalDateData(LocalDate.now());
		obj.setJavaUtilDateData(new Date());
		obj.setLocalDateTimeData(LocalDateTime.now());
		obj.setBigDecimalData(new BigDecimal("10.23"));
		obj.setBooleanVal(true);
		obj.setImage(new byte[] { 10, 20, 30 });
		obj.setOffsetDateTimeData(OffsetDateTime.now());
		obj.setStatus(StatusEnum.OPEN);

		obj.setJavaUtilDateTsData(new Date());

		sjm.insert(obj);

		TypeCheckMysql tc = sjm.findById(TypeCheckMysql.class, obj.getId());
		assertNotNull(tc.getLocalDateData());
		assertNotNull(tc.getJavaUtilDateData());
		assertNotNull(tc.getLocalDateTimeData());

		assertEquals(0, tc.getBigDecimalData().compareTo(obj.getBigDecimalData()));

		assertNotNull(tc.getOffsetDateTimeData());

		assertArrayEquals(obj.getImage(), tc.getImage());

		assertTrue(tc.getBooleanVal());

		assertNotNull(tc.getJavaUtilDateTsData());
		assertEquals(StatusEnum.OPEN, tc.getStatus());
	}

	@Test
	void update_TypeCheckMysqlTest() {
		TypeCheckMysql obj = new TypeCheckMysql();

		obj.setLocalDateData(LocalDate.now());
		obj.setJavaUtilDateData(new Date());
		obj.setLocalDateTimeData(LocalDateTime.now());
		obj.setBigDecimalData(new BigDecimal("10.23"));
		obj.setBooleanVal(true);
		obj.setImage(new byte[] { 10, 20, 30 });
		obj.setOffsetDateTimeData(OffsetDateTime.now());

		obj.setJavaUtilDateTsData(new Date());

		sjm.insert(obj);

		TypeCheckMysql tc = sjm.findById(TypeCheckMysql.class, obj.getId());
		TypeCheckMysql tc1 = sjm.findById(TypeCheckMysql.class, obj.getId());

		Instant instant = LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
		java.util.Date nextDay = Date.from(instant);

		Instant instant1 = LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant();
		java.util.Date nextDayDateTime = Date.from(instant1);

		tc1.setLocalDateData(LocalDate.now().plusDays(1));
		tc1.setJavaUtilDateData(nextDay);
		tc1.setLocalDateTimeData(LocalDateTime.now().plusDays(1));

		tc1.setOffsetDateTimeData(OffsetDateTime.now().plusDays(1));

		tc1.setBigDecimalData(new BigDecimal("11.34"));
		tc1.setBooleanVal(false);

		byte[] newImageVal = new byte[] { 5 };
		tc1.setImage(newImageVal);

		tc1.setJavaUtilDateTsData(nextDayDateTime);
		tc1.setStatus(StatusEnum.CLOSED);

		sjm.update(tc1);

		TypeCheckMysql tc2 = sjm.findById(TypeCheckMysql.class, obj.getId());

		assertTrue(tc2.getLocalDateData().isAfter(tc.getLocalDateData()));
		assertTrue(tc2.getJavaUtilDateData().getTime() > tc.getJavaUtilDateData().getTime());
		assertTrue(tc2.getLocalDateTimeData().isAfter(tc.getLocalDateTimeData()));

		assertTrue(tc2.getOffsetDateTimeData().isAfter(tc.getOffsetDateTimeData()));

		assertEquals(0, tc2.getBigDecimalData().compareTo(new BigDecimal("11.34")));

		assertArrayEquals(newImageVal, tc2.getImage());

		assertTrue(!tc2.getBooleanVal());

		assertTrue(tc2.getJavaUtilDateTsData().getTime() > tc.getJavaUtilDateTsData().getTime());
		assertEquals(StatusEnum.CLOSED, tc2.getStatus());
	}

}

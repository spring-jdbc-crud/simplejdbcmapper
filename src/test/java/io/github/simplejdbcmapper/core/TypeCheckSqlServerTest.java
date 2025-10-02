package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import io.github.simplejdbcmapper.model.TypeCheckSqlServer;

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
		TypeCheckSqlServer obj = new TypeCheckSqlServer();

		obj.setLocalDateData(LocalDate.now());
		obj.setJavaUtilDateData(new Date());
		obj.setLocalDateTimeData(LocalDateTime.now());
		obj.setBigDecimalData(new BigDecimal("10.23"));

		obj.setJavaUtilDateDtData(new Date());
		obj.setStatus(StatusEnum.OPEN);
		
		obj.setImage(new byte[] { 10, 20, 30 });
		
		obj.setClobData("123456789".toCharArray());

		sjm.insert(obj);

		TypeCheckSqlServer tc = sjm.findById(TypeCheckSqlServer.class, obj.getId());
		assertNotNull(tc.getLocalDateData());
		assertNotNull(tc.getJavaUtilDateData());
		assertNotNull(tc.getLocalDateTimeData());

		assertEquals(0, tc.getBigDecimalData().compareTo(obj.getBigDecimalData()));

		assertNotNull(tc.getJavaUtilDateDtData());
		assertEquals(StatusEnum.OPEN, tc.getStatus());
		assertNotNull(tc.getImage());
		assertNotNull(tc.getClobData());
	}

	@Test
	void update_TypeCheckSqlServerTest() {
		TypeCheckSqlServer obj = new TypeCheckSqlServer();

		obj.setLocalDateData(LocalDate.now());
		obj.setJavaUtilDateData(new Date());
		obj.setLocalDateTimeData(LocalDateTime.now());
		obj.setBigDecimalData(new BigDecimal("10.23"));

		obj.setJavaUtilDateDtData(new Date());

		sjm.insert(obj);

		TypeCheckSqlServer tc = sjm.findById(TypeCheckSqlServer.class, obj.getId());
		TypeCheckSqlServer tc1 = sjm.findById(TypeCheckSqlServer.class, obj.getId());

		Instant instant = LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
		java.util.Date nextDay = Date.from(instant);

		Instant instant1 = LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant();
		java.util.Date nextDayDateTime = Date.from(instant1);

		tc1.setLocalDateData(LocalDate.now().plusDays(1));
		tc1.setJavaUtilDateData(nextDay);
		tc1.setLocalDateTimeData(LocalDateTime.now().plusDays(1));

		tc1.setJavaUtilDateDtData(nextDayDateTime);

		tc1.setBigDecimalData(new BigDecimal("11.34"));
		tc1.setStatus(StatusEnum.CLOSED);
		tc1.setImage(new byte[] { 10, 20, 30 });
		
		tc1.setClobData("123456789".toCharArray());

		sjm.update(tc1);

		TypeCheckSqlServer tc2 = sjm.findById(TypeCheckSqlServer.class, obj.getId());

		assertTrue(tc2.getLocalDateData().isAfter(tc.getLocalDateData()));
		assertTrue(tc2.getJavaUtilDateData().getTime() > tc.getJavaUtilDateData().getTime());
		assertTrue(tc2.getLocalDateTimeData().isAfter(tc.getLocalDateTimeData()));

		assertEquals(0, tc2.getBigDecimalData().compareTo(new BigDecimal("11.34")));
		assertTrue(tc2.getJavaUtilDateDtData().getTime() > tc.getJavaUtilDateDtData().getTime());
		assertSame(StatusEnum.CLOSED, tc2.getStatus());
		
		assertNotNull(tc2.getImage());
		assertNotNull(tc2.getClobData());
	}

}

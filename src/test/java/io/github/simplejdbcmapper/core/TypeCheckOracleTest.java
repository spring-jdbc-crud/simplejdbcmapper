package io.github.simplejdbcmapper.core;

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

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.exception.MapperException;
import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.StatusEnum;
import io.github.simplejdbcmapper.model.TypeCheckOracle;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TypeCheckOracleTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private DataSource ds;

	@Autowired
	private SimpleJdbcMapper sjm;

	@BeforeEach
	void beforeMethod() {
		// tests will only run if oracle
		if (!jdbcDriver.contains("oracle")) {
			Assumptions.assumeTrue(false);
		}
	}

	@Test
	void insert_TypeCheckTest() {
		TypeCheckOracle obj = new TypeCheckOracle();

		obj.setLocalDateData(LocalDate.now());
		obj.setJavaUtilDateData(new Date());
		obj.setLocalDateTimeData(LocalDateTime.now());
		obj.setBigDecimalData(new BigDecimal("10.23"));
		obj.setOffsetDateTimeData(OffsetDateTime.now());

		obj.setJavaUtilDateTsData(new Date());
		obj.setStatus(StatusEnum.OPEN);

		obj.setImage(new byte[] { 10, 20, 30 });

		obj.setClobData("123456789".toCharArray());

		sjm.insert(obj);

		TypeCheckOracle tc = sjm.findById(TypeCheckOracle.class, obj.getId());
		assertNotNull(tc.getLocalDateData());
		assertNotNull(tc.getJavaUtilDateData());
		assertNotNull(tc.getLocalDateTimeData());

		assertEquals(0, tc.getBigDecimalData().compareTo(obj.getBigDecimalData()));

		assertNotNull(tc.getOffsetDateTimeData());

		assertNotNull(tc.getJavaUtilDateTsData());

		assertEquals(StatusEnum.OPEN, tc.getStatus());
		assertNotNull(tc.getImage());
		assertEquals(9, tc.getClobData().length);
	}

	@Test
	void insert_TypeCheckWithBlobAndClob_AsNull_Test() {
		TypeCheckOracle obj = new TypeCheckOracle();

		obj.setLocalDateData(LocalDate.now());
		obj.setJavaUtilDateData(new Date());
		obj.setLocalDateTimeData(LocalDateTime.now());
		obj.setBigDecimalData(new BigDecimal("10.23"));
		obj.setOffsetDateTimeData(OffsetDateTime.now());

		obj.setJavaUtilDateTsData(new Date());
		obj.setStatus(StatusEnum.OPEN);
		obj.setImage(null);
		obj.setClobData(null);
		Assertions.assertDoesNotThrow(() -> {
			sjm.insert(obj);
		});
	}

	@Test
	void update_TypeCheckTest() {
		TypeCheckOracle obj = new TypeCheckOracle();

		obj.setLocalDateData(LocalDate.now());
		obj.setJavaUtilDateData(new Date());
		obj.setLocalDateTimeData(LocalDateTime.now());
		obj.setBigDecimalData(new BigDecimal("10.23"));
		obj.setOffsetDateTimeData(OffsetDateTime.now());
		obj.setJavaUtilDateTsData(new Date());
		sjm.insert(obj);

		TypeCheckOracle tc = sjm.findById(TypeCheckOracle.class, obj.getId());
		TypeCheckOracle tc1 = sjm.findById(TypeCheckOracle.class, obj.getId());

		Instant instant = LocalDate.now().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
		java.util.Date nextDay = Date.from(instant);

		Instant instant1 = LocalDateTime.now().plusDays(1).atZone(ZoneId.systemDefault()).toInstant();
		java.util.Date nextDayDateTime = Date.from(instant1);

		tc1.setLocalDateData(LocalDate.now().plusDays(1));
		tc1.setJavaUtilDateData(nextDay);
		tc1.setLocalDateTimeData(LocalDateTime.now().plusDays(1));

		tc1.setOffsetDateTimeData(OffsetDateTime.now().plusDays(1));

		tc1.setBigDecimalData(new BigDecimal("11.34"));

		tc1.setJavaUtilDateTsData(nextDayDateTime);

		tc1.setStatus(StatusEnum.CLOSED);
		tc1.setImage(new byte[] { 10, 20, 30 });

		tc1.setClobData("123456789".toCharArray());

		sjm.update(tc1);

		TypeCheckOracle tc2 = sjm.findById(TypeCheckOracle.class, obj.getId());

		assertTrue(tc2.getLocalDateData().isAfter(tc.getLocalDateData()));
		assertTrue(tc2.getJavaUtilDateData().getTime() > tc.getJavaUtilDateData().getTime());
		assertTrue(tc2.getLocalDateTimeData().isAfter(tc.getLocalDateTimeData()));

		assertTrue(tc2.getOffsetDateTimeData().isAfter(tc.getOffsetDateTimeData()));

		assertEquals(0, tc2.getBigDecimalData().compareTo(new BigDecimal("11.34")));

		assertTrue(tc2.getJavaUtilDateTsData().getTime() > tc.getJavaUtilDateTsData().getTime());

		assertEquals(StatusEnum.CLOSED, tc2.getStatus());

		assertNotNull(tc2.getImage());
		assertEquals(9, tc2.getClobData().length);
	}

	@Test
	void update_WithBlogClob_AsNull_test() {
		TypeCheckOracle obj = new TypeCheckOracle();

		obj.setLocalDateData(LocalDate.now());
		obj.setJavaUtilDateData(new Date());
		obj.setLocalDateTimeData(LocalDateTime.now());
		obj.setBigDecimalData(new BigDecimal("10.23"));
		obj.setOffsetDateTimeData(OffsetDateTime.now());
		obj.setJavaUtilDateTsData(new Date());
		sjm.insert(obj);

		TypeCheckOracle tc = sjm.findById(TypeCheckOracle.class, obj.getId());

		tc.setImage(null);

		tc.setClobData(null);

		Assertions.assertDoesNotThrow(() -> {
			sjm.update(tc);
		});

	}

	@Test
	void oracleConfig_failureOnUsingSchemaInsteadOfCatalog() {
		SimpleJdbcMapper mapper = new SimpleJdbcMapper(ds, null, "schema1");
		Integer id = Integer.valueOf(1);

		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			mapper.findById(Order.class, id);
		});

		assertTrue(exception.getMessage().contains(
				"When creating SimpleJdbcMapper() if you are using the 'catalog' (argument 3) use 'schema' (argument 2) instead"));
	}

}

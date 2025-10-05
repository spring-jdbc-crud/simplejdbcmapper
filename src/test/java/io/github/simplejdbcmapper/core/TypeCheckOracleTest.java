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
		TypeCheckOracle iObj = new TypeCheckOracle();
		var localDateVal = LocalDate.now();
		iObj.setLocalDateData(localDateVal);

		var dateVal = new Date();
		iObj.setJavaUtilDateData(dateVal);

		var localDateTimeVal = LocalDateTime.now();
		iObj.setLocalDateTimeData(localDateTimeVal);

		var bigDecimalVal = new BigDecimal("10.23");
		iObj.setBigDecimalData(bigDecimalVal);

		iObj.setJavaUtilDateTsData(new Date());

		iObj.setImage(new byte[] { 10, 20, 30 });

		iObj.setStatus(StatusEnum.OPEN);

		var offsetVal = OffsetDateTime.now();
		iObj.setOffsetDateTimeData(offsetVal);

		iObj.setClobData("123456789".toCharArray());

		sjm.insert(iObj);

		TypeCheckOracle tc = sjm.findById(TypeCheckOracle.class, iObj.getId());

		assertNotNull(tc.getJavaUtilDateTsData());

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		assertEquals(localDateVal, tc.getLocalDateData());

		assertEquals(fmt.format(dateVal), fmt.format(tc.getJavaUtilDateData()));

		DateTimeFormatter ldtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		assertEquals(ldtFmt.format(localDateTimeVal), ldtFmt.format(tc.getLocalDateTimeData()));

		assertEquals(0, tc.getBigDecimalData().compareTo(iObj.getBigDecimalData()));

		assertArrayEquals(iObj.getImage(), tc.getImage());

		assertEquals(StatusEnum.OPEN, tc.getStatus());

		assertEquals(offsetVal, tc.getOffsetDateTimeData());

		assertNotNull(tc.getImage());
		assertNotNull(tc.getClobData());

	}

	@Test
	void insert_TypeCheckWithBlobAndClob_AsNull_Test() {
		TypeCheckOracle obj = new TypeCheckOracle();
		obj.setImage(null);
		obj.setClobData(null);
		Assertions.assertDoesNotThrow(() -> {
			sjm.insert(obj);
		});
	}

	@Test
	void update_TypeCheckTest() {
		TypeCheckOracle iObj = new TypeCheckOracle();
		sjm.insert(iObj);

		TypeCheckOracle uObj = sjm.findById(TypeCheckOracle.class, iObj.getId());

		var localDateVal = LocalDate.now();
		uObj.setLocalDateData(localDateVal);

		var dateVal = new Date();
		uObj.setJavaUtilDateData(dateVal);

		var localDateTimeVal = LocalDateTime.now();
		uObj.setLocalDateTimeData(localDateTimeVal);

		var bigDecimalVal = new BigDecimal("10.23");
		uObj.setBigDecimalData(bigDecimalVal);

		uObj.setJavaUtilDateTsData(new Date());

		uObj.setImage(new byte[] { 10, 20, 30 });

		uObj.setStatus(StatusEnum.OPEN);

		var offsetVal = OffsetDateTime.now();
		uObj.setOffsetDateTimeData(offsetVal);

		uObj.setClobData("123456789".toCharArray());

		sjm.update(uObj);

		TypeCheckOracle tc = sjm.findById(TypeCheckOracle.class, iObj.getId());

		assertNotNull(tc.getJavaUtilDateTsData());

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		assertEquals(localDateVal, tc.getLocalDateData());

		assertEquals(fmt.format(dateVal), fmt.format(tc.getJavaUtilDateData()));

		DateTimeFormatter ldtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		assertEquals(ldtFmt.format(localDateTimeVal), ldtFmt.format(tc.getLocalDateTimeData()));

		assertEquals(0, tc.getBigDecimalData().compareTo(uObj.getBigDecimalData()));

		assertArrayEquals(uObj.getImage(), tc.getImage());

		assertEquals(StatusEnum.OPEN, tc.getStatus());

		assertEquals(offsetVal, tc.getOffsetDateTimeData());

		assertNotNull(tc.getImage());
		assertNotNull(tc.getClobData());

	}

	@Test
	void update_WithBlogClob_AsNull_test() {
		TypeCheckOracle obj = new TypeCheckOracle();
		sjm.insert(obj);

		TypeCheckOracle tc = sjm.findById(TypeCheckOracle.class, obj.getId());

		tc.setImage(null);

		tc.setClobData(null);

		Assertions.assertDoesNotThrow(() -> {
			sjm.update(tc);
		});

	}

	@Test
	void oracleConfig_failureOnUsingCatalogInsteadOfSchema() {
		SimpleJdbcMapper mapper = new SimpleJdbcMapper(ds, null, "schema1");
		Integer id = Integer.valueOf(1);

		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			mapper.findById(Order.class, id);
		});

		assertTrue(exception.getMessage().contains(
				"When creating SimpleJdbcMapper() if you are using the 'catalog' (argument 3) use 'schema' (argument 2) instead"));
	}

}

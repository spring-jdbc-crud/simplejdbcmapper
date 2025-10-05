package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
import io.github.simplejdbcmapper.model.TypeCheckMysql;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TypeCheckMysqlTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

	@Autowired
	private DataSource ds;

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
		TypeCheckMysql iObj = new TypeCheckMysql();

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

		var timestampVal = new Date();
		iObj.setJavaUtilDateTsData(timestampVal);

		iObj.setStatus(StatusEnum.OPEN);

		var offsetVal = OffsetDateTime.now();
		iObj.setOffsetDateTimeData(offsetVal);

		sjm.insert(iObj);

		TypeCheckMysql tc = sjm.findById(TypeCheckMysql.class, iObj.getId());
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		assertEquals(localDateVal, tc.getLocalDateData());

		assertEquals(fmt.format(dateVal), fmt.format(tc.getJavaUtilDateData()));

		DateTimeFormatter ldtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		assertEquals(ldtFmt.format(localDateTimeVal), ldtFmt.format(tc.getLocalDateTimeData()));

		assertEquals(0, tc.getBigDecimalData().compareTo(iObj.getBigDecimalData()));

		assertArrayEquals(iObj.getImage(), tc.getImage());

		assertTrue(tc.getBooleanVal());

		SimpleDateFormat fmtTs = new SimpleDateFormat("yyyyMMdd HHmmss");
		assertEquals(fmtTs.format(timestampVal), fmtTs.format(tc.getJavaUtilDateTsData()));

		assertEquals(StatusEnum.OPEN, tc.getStatus());
		DateTimeFormatter oFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		assertEquals(oFmt.format(offsetVal), oFmt.format(tc.getOffsetDateTimeData()));

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

	@Test
	void mysqlConfig_failureOnUsingSchemaInsteadOfCatalog() {
		SimpleJdbcMapper mapper = new SimpleJdbcMapper(ds, "schema1");
		Integer id = Integer.valueOf(1);

		Exception exception = Assertions.assertThrows(MapperException.class, () -> {
			mapper.findById(Order.class, id);
		});

		assertTrue(exception.getMessage().contains(
				"When creating SimpleJdbcMapper() if you are using 'schema' (argument 2) use 'catalog' (argument 3) instead"));
	}

}

package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

		SimpleDateFormat fmtTs = new SimpleDateFormat("yyyyMMdd HHmm");
		assertEquals(fmtTs.format(timestampVal), fmtTs.format(tc.getJavaUtilDateTsData()));

		assertEquals(StatusEnum.OPEN, tc.getStatus());
		DateTimeFormatter oFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		assertEquals(oFmt.format(offsetVal), oFmt.format(tc.getOffsetDateTimeData()));

	}

	@Test
	void update_TypeCheckMysqlTest() {
		TypeCheckMysql iObj = new TypeCheckMysql();
		sjm.insert(iObj);
		TypeCheckMysql uObj = sjm.findById(TypeCheckMysql.class, iObj.getId());

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

		var timestampVal = new Date();
		uObj.setJavaUtilDateTsData(timestampVal);

		uObj.setStatus(StatusEnum.OPEN);

		var offsetVal = OffsetDateTime.now();
		uObj.setOffsetDateTimeData(offsetVal);

		sjm.update(uObj);

		TypeCheckMysql tc = sjm.findById(TypeCheckMysql.class, uObj.getId());
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
		assertEquals(localDateVal, tc.getLocalDateData());

		assertEquals(fmt.format(dateVal), fmt.format(tc.getJavaUtilDateData()));

		DateTimeFormatter ldtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		assertEquals(ldtFmt.format(localDateTimeVal), ldtFmt.format(tc.getLocalDateTimeData()));

		assertEquals(0, tc.getBigDecimalData().compareTo(uObj.getBigDecimalData()));

		assertArrayEquals(uObj.getImage(), tc.getImage());

		assertTrue(tc.getBooleanVal());

		SimpleDateFormat fmtTs = new SimpleDateFormat("yyyyMMdd HHmm");
		assertEquals(fmtTs.format(timestampVal), fmtTs.format(tc.getJavaUtilDateTsData()));

		assertEquals(StatusEnum.OPEN, tc.getStatus());
		DateTimeFormatter oFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		assertEquals(oFmt.format(offsetVal), oFmt.format(tc.getOffsetDateTimeData()));

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

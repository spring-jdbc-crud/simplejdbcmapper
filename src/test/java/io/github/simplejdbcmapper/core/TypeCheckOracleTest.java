package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.sql.rowset.serial.SerialBlob;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.BlobErr;
import io.github.simplejdbcmapper.model.ClobErr;
import io.github.simplejdbcmapper.model.StatusEnum;
import io.github.simplejdbcmapper.model.TypeCheckOracle;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TypeCheckOracleTest {

	@Value("${spring.datasource.driver-class-name}")
	private String jdbcDriver;

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

		iObj.setClobData("123456789");

		iObj.setClobDataStr("123456789");

		iObj.setNclobData("nclob123456789");

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

		assertEquals(offsetVal.toInstant().truncatedTo(ChronoUnit.MILLIS),
				tc.getOffsetDateTimeData().toInstant().truncatedTo(ChronoUnit.MILLIS));

		assertNotNull(tc.getImage());
		assertNotNull(tc.getClobData());
		assertNotNull(tc.getClobDataStr());
		assertNotNull(tc.getNclobData());

	}

	@Test
	void insert_TypeCheckWithNulls_Test() {
		TypeCheckOracle iObj = new TypeCheckOracle();
		sjm.insert(iObj);
		assertNotNull(iObj.getId());
	}

	@Test
	void insert_TypeCheckWithBlobAndClob_AsNull_Test() {
		TypeCheckOracle obj = new TypeCheckOracle();
		obj.setImage(null);
		obj.setClobData(null);
		obj.setClobDataStr(null);
		obj.setNclobData(null);
		Assertions.assertDoesNotThrow(() -> {
			sjm.insert(obj);
		});
	}

	@Test
	void insert_BlobErrTest() throws Exception {
		BlobErr obj = new BlobErr();
		byte[] byteArray = new byte[] { 10, 20, 30 };
		SerialBlob blob = new SerialBlob(byteArray);
		obj.setImage(blob);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			sjm.insert(obj);
		});

		assertTrue(exception.getMessage().contains("java type has to be byte[]"));
	}

	@Test
	void insert_ClobErrTest() {
		ClobErr obj = new ClobErr();
		obj.setClobData(new String[] { "a", "b", "c" });
		Assertions.assertThrows(Exception.class, () -> {
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

		uObj.setClobData("123456789");
		uObj.setClobDataStr("123456789");
		uObj.setNclobData("nclob123456789");

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

		assertEquals(offsetVal.toInstant().truncatedTo(ChronoUnit.MILLIS),
				tc.getOffsetDateTimeData().toInstant().truncatedTo(ChronoUnit.MILLIS));

		assertNotNull(tc.getImage());
		assertNotNull(tc.getClobData());
		assertNotNull(tc.getClobDataStr());
		assertNotNull(tc.getNclobData());

	}

	@Test
	void update_WithBlogClob_AsNull_test() {
		TypeCheckOracle obj = new TypeCheckOracle();
		sjm.insert(obj);

		TypeCheckOracle tc = sjm.findById(TypeCheckOracle.class, obj.getId());

		tc.setImage(null);

		tc.setClobData(null);

		tc.setNclobData(null);

		Assertions.assertDoesNotThrow(() -> {
			sjm.update(tc);
		});

	}

	@Test
	void update_BlobErrTest() throws SQLException {
		BlobErr obj = new BlobErr();
		obj.setId(1);
		byte[] byteArray = new byte[] { 10, 20, 30 };
		SerialBlob blob = new SerialBlob(byteArray);
		obj.setImage(blob);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			sjm.update(obj);
		});

		assertTrue(exception.getMessage().contains("java type has to be byte[]"));
	}

	@Test
	void update_ClobErrTest() {
		ClobErr obj = new ClobErr();
		obj.setId(1);
		obj.setClobData(new String[] { "a", "b", "c" });
		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			sjm.update(obj);
		});
		assertTrue(exception.getMessage().contains("java type has to be String or other"));
	}

}

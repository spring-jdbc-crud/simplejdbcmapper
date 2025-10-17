package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import io.github.simplejdbcmapper.model.Customer;
import io.github.simplejdbcmapper.model.Employee;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TransactionTest {

	@Autowired
	private SimpleJdbcMapper sjm;

	@BeforeEach
	void beforeMethod() {
		// clear caches to force a table meta data lookup from database.
		SimpleJdbcMapperSupport sjmSupport = TestUtils.getSimpleJdbcMapperSupport(sjm);
		FindOperation fo = TestUtils.getFindOperation(sjm);
		InsertOperation io = TestUtils.getInsertOperation(sjm);
		UpdateOperation uo = TestUtils.getUpdateOperation(sjm);
		sjmSupport.getTableMappingCache().clear();
		io.getInsertSqlCache().clear();
		fo.getFindByIdSqlCache().clear();
		uo.getUpdateSqlCache().clear();
	}

	@Test
	@Transactional
	void database_metadata_lookup_within_a_transaction_test() {
		Customer cust = sjm.findById(Customer.class, 1);
		assertNotNull(cust);
		sjm.update(cust);

		Employee emp = new Employee();
		emp.setFirstName("xyz");
		emp.setLastName("abc");
		sjm.insert(emp);

		assertNotNull(emp.getId());

	}

}

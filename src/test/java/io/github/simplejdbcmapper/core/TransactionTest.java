package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.simplejdbcmapper.model.Customer;
import io.github.simplejdbcmapper.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class TransactionTest {

  @Autowired private SimpleJdbcMapper sjm;

  @BeforeEach
  void beforeMethod() {
    SimpleJdbcMapperSupport sjmSupport = TestUtils.getSimpleJdbcMapperSupport(sjm);
    TableMappingProvider tmpr = TestUtils.getTableMappingProvider(sjmSupport);
    SimpleCache<Class<?>, TableMapping> tableMappingCache = TestUtils.getTableMappingCache(tmpr);

    FindOperation fo = TestUtils.getFindOperation(sjm);
    SimpleCache<Class<?>, String> findByIdSqlCache = TestUtils.getFindByIdSqlCache(fo);

    InsertOperation io = TestUtils.getInsertOperation(sjm);
    SimpleCache<Class<?>, SimpleJdbcInsert> insertSqlCache = TestUtils.getInsertSqlCache(io);

    UpdateOperation uo = TestUtils.getUpdateOperation(sjm);
    SimpleCache<Class<?>, SqlAndParams> updateSqlCache = TestUtils.getUpdateSqlCache(uo);

    tableMappingCache.clear();
    findByIdSqlCache.clear();
    insertSqlCache.clear();
    updateSqlCache.clear();
  }

  @Test
  @Transactional
  void mapping_creation_within_transaction_test() {
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

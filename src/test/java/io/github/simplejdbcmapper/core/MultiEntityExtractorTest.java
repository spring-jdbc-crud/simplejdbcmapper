package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.Order;
import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.Product;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class MultiEntityExtractorTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void resultSetExtractor_plus_multientity_processing_test() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol").add(Product.class,
				"p");

		String sql = """
				SELECT %s
				FROM orders o
				LEFT JOIN order_line ol ON  o.id = ol.order_id
				LEFT JOIN product p ON ol.product_id = p.id
				WHERE o.id <= 4
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = resultListMap.getList(Order.class);
		List<OrderLine> orderLines = resultListMap.getList(OrderLine.class);
		List<Product> products = resultListMap.getList(Product.class);

		// test the last columns to make sure EntityRowMapper offsets work.
		assertEquals("order last col", orders.get(0).getLastMappedCol());
		assertEquals("orderline last col", orderLines.get(0).getLastMappedCol());
		assertEquals("product last col", products.get(0).getLastMappedCol());

		// check extractor only returns unique ids.
		Set<Long> seen = new HashSet<>();
		Set<Long> duplicates = orders.stream().map(Order::getId).filter(id -> !seen.add(id))
				.collect(Collectors.toSet());
		assertEquals(0, duplicates.size());

		Set<Integer> seen2 = new HashSet<>();
		Set<Integer> duplicates2 = orderLines.stream().map(OrderLine::getOrderLineId).filter(id -> !seen2.add(id))
				.collect(Collectors.toSet());
		assertEquals(0, duplicates2.size());

		Set<Integer> seen3 = new HashSet<>();
		Set<Integer> duplicates3 = products.stream().map(Product::getId).filter(id -> !seen3.add(id))
				.collect(Collectors.toSet());
		assertEquals(0, duplicates3.size());

	}

}

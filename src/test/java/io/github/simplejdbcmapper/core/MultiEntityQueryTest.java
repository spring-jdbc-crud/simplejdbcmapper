package io.github.simplejdbcmapper.core;

import java.util.List;
import java.util.Map;

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
class MultiEntityQueryTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@SuppressWarnings("unchecked")
	@Test
	void OrderHasManyOrderLinesWhichHasOneProduct_success() {

		MultiEntity multiEntity = new MultiEntity().add(Order.class, "o").add(OrderLine.class, "ol").add(Product.class,
				"p");

		String sql = """
				SELECT %s
				FROM orders o
				JOIN order_line ol ON ol.order_id = o.order_id
				JOIN product p o ON p.product_id = ol.product_id
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		@SuppressWarnings("rawtypes")
		Map<Class, List> resultMap = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		List<Order> orders = resultMap.get(Order.class);
		List<OrderLine> orderLines = resultMap.get(OrderLine.class);
		List<Product> products = resultMap.get(Product.class);

		SimpleJdbcMapperUtils.populateHasMany(orders, orderLines, "orderId", "orderId", "orderLines");
		SimpleJdbcMapperUtils.populateHasOne(orderLines, products, "productId", "productId", "product");

	}

}

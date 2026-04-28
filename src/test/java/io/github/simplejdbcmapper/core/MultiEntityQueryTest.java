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

		MultiEntity multiEntity = new MultiEntity(new EntityEntry(Order.class, "o"),
				new EntityEntry(OrderLine.class, "ol"), new EntityEntry(Product.class, "p"));

		String sql = """
				SELECT %s
				from orders o
				join order_line ol on ol.order_id = o.order_id
				join product p on p.product_id = ol.product_id
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

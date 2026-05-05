package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.simplejdbcmapper.model.OrderLine;
import io.github.simplejdbcmapper.model.Product;
import io.github.simplejdbcmapper.model.Profile;
import io.github.simplejdbcmapper.model.ProfileUserIdLong;
import io.github.simplejdbcmapper.model.User;
import io.github.simplejdbcmapper.relationship.RelationshipExtractor;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ToOneTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void toOne_joinOn_validation_test() {
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);
		List<Product> products = sjm.findAll(Product.class);

		RelationshipExtractor rExtractor = new RelationshipExtractor();
		rExtractor.putList(OrderLine.class, orderLines);
		rExtractor.putList(Product.class, products);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			rExtractor.type(OrderLine.class).toOne(Product.class).joinOn(null, "id");
		});
		assertTrue(exception.getMessage().contains("mainObjJoinProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			rExtractor.type(OrderLine.class).toOne(Product.class).joinOn("orderLineId", null);
		});
		assertTrue(exception.getMessage().contains("relatedObjJoinProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			rExtractor.type(OrderLine.class).toOne(Product.class).joinOn("x", "id");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Could not find getter for "));

		exception = Assertions.assertThrows(Exception.class, () -> {
			rExtractor.type(OrderLine.class).toOne(Product.class).joinOn("orderLineId", "x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Could not find getter for "));

		// assertDoesNotThrow(() -> {
		// rExtractor.type(null).toOneList(products).joinOn("x", "id");
		// });

		// assertDoesNotThrow(() -> {
		// rExtractor.type(orderLines).toOneList(null).joinOn("orderLineId", "x");
		// });

	}

	@Test
	void toOne_joinOn_propertyTypeCheck_test() {
		User user1 = new User();
		user1.setId(1);
		user1.setName("user1");

		List<User> users = new ArrayList<>();
		users.add(user1);

		ProfileUserIdLong profile1 = new ProfileUserIdLong();
		profile1.setId(1);
		profile1.setTheme("theme for user1");
		profile1.setUserId(1L);

		List<ProfileUserIdLong> profiles = new ArrayList<>();
		profiles.add(profile1);

		RelationshipExtractor rExtractor = new RelationshipExtractor();
		rExtractor.putList(User.class, users);
		rExtractor.putList(ProfileUserIdLong.class, profiles);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			rExtractor.type(User.class).toOne(ProfileUserIdLong.class).joinOn("id", "userId").populate("profile");
		});
		assertTrue(exception.getMessage().contains("are not the same"));

	}

	@Test
	void toOne_populate_validation_test() {
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);
		List<Product> products = sjm.findAll(Product.class);

		RelationshipExtractor rExtractor = new RelationshipExtractor();
		rExtractor.putList(OrderLine.class, orderLines);
		rExtractor.putList(Product.class, products);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			rExtractor.type(OrderLine.class).toOne(Product.class).joinOn("productId", "id").populate(null);
		});
		assertTrue(exception.getMessage().contains("mainObjPropertyToPopulate must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			rExtractor.type(OrderLine.class).toOne(Product.class).joinOn("productId", "id").populate("x");
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		// assertDoesNotThrow(() -> {
		// rExtractor.type(null).toOneList(products).joinOn("productId",
		// "id").populate("x");
		// });

	}

	@Test
	void toOne_UserProfile_both_way_test() {
		User user1 = new User();
		user1.setId(1);
		user1.setName("user1");

		User user2 = new User();
		user2.setId(2);
		user2.setName("user2");

		User user3 = new User();
		user3.setId(3);
		user3.setName("user3");

		Profile profile1 = new Profile();
		profile1.setId(1);
		profile1.setTheme("theme for user1");
		profile1.setUserId(1);

		Profile profile2 = new Profile();
		profile2.setId(2);
		profile2.setTheme("theme for user2");
		profile2.setUserId(2);

		List<User> users = new ArrayList<>();
		users.add(user1);
		users.add(user2);
		users.add(user3);

		List<Profile> profiles = new ArrayList<>();
		profiles.add(profile1);
		profiles.add(profile2);

		RelationshipExtractor rExtractor = new RelationshipExtractor();
		rExtractor.putList(User.class, users);
		rExtractor.putList(Profile.class, profiles);

		rExtractor.type(User.class).toOne(Profile.class).joinOn("id", "userId").populate("profile");

		assertEquals("theme for user1", users.get(0).getProfile().getTheme());
		assertEquals("theme for user2", users.get(1).getProfile().getTheme());
		assertNull(users.get(2).getProfile());

		// reverse test ie populate profile with user
		rExtractor.type(Profile.class).toOne(User.class).joinOn("userId", "id").populate("user");

		assertEquals("user1", profiles.get(0).getUser().getName());
		assertEquals("user2", profiles.get(1).getUser().getName());

	}

	@Test
	void toOne_NullEntry_test() {
		User user1 = new User();
		user1.setId(1);
		user1.setName("user1");

		User user2 = new User();
		user2.setId(2);
		user2.setName("user2");

		User user3 = new User();
		user3.setId(3);
		user3.setName("user3");

		Profile profile1 = new Profile();
		profile1.setId(1);
		profile1.setTheme("theme for user1");
		profile1.setUserId(1);

		Profile profile2 = new Profile();
		profile2.setId(2);
		profile2.setTheme("theme for user2");
		profile2.setUserId(2);

		List<User> users = new ArrayList<>();
		users.add(user1);
		users.add(null);
		users.add(user2);
		users.add(user3);

		List<Profile> profiles = new ArrayList<>();
		profiles.add(null);
		profiles.add(profile1);
		profiles.add(profile2);

		rExtractor.type(users).toOneList(profiles).joinOn("id", "userId").populate("profile");
		assertEquals("theme for user2", users.get(2).getProfile().getTheme());

		// reverse test ie populate profile with user
		rExtractor.type(profiles).toOneList(users).joinOn("userId", "id").populate("user");
		assertEquals("user1", profiles.get(1).getUser().getName());

	}

	@Test
	void toOne_PopulatePropertyExistsButWrongType_test() {
		User user1 = new User();
		user1.setId(1);
		user1.setName("user1");

		Profile profile1 = new Profile();
		profile1.setId(1);
		profile1.setTheme("theme for user1");
		profile1.setUserId(1);

		List<User> users = new ArrayList<>();
		users.add(user1);

		List<Profile> profiles = new ArrayList<>();
		profiles.add(profile1);

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			rExtractor.type(users).toOneList(profiles).joinOn("id", "userId").populate("name");
		});
		assertTrue(exception.getMessage().contains("argument type mismatch"));
	}

	@Test
	void toOne_PropertyNull_test() {
		User user1 = new User();
		user1.setId(1);
		user1.setName("user1");

		User user2 = new User();
		user2.setId(2);
		user2.setName("user2");

		User user3 = new User();
		user3.setId(3);
		user3.setName("user3");

		Profile profile1 = new Profile();
		profile1.setId(1);
		profile1.setTheme("theme for user1");
		profile1.setUserId(1);

		Profile profile2 = new Profile();
		profile2.setId(2);
		profile2.setTheme("theme for user2");
		profile2.setUserId(2);

		List<User> users = new ArrayList<>();
		users.add(user1);
		users.add(new User());
		users.add(user2);
		users.add(user3);

		List<Profile> profiles = new ArrayList<>();
		profiles.add(new Profile());
		profiles.add(profile1);
		profiles.add(profile2);

		rExtractor.type(users).toOneList(profiles).joinOn("id", "userId").populate("profile");
		assertEquals("theme for user2", users.get(2).getProfile().getTheme());

		// reverse test ie populate profile with user
		rExtractor.type(profiles).toOneList(users).joinOn("userId", "id").populate("user");
		assertEquals("user1", profiles.get(1).getUser().getName());

	}

	@Test void toOne_integration_test() { MultiEntity multiEntity = new
	  MultiEntity().add(OrderLine.class, "ol").add(Product.class, "p");
	  
	  String sql = """ SELECT %s FROM order_line ol LEFT JOIN product p ON
	  ol.product_id = p.id WHERE ol.order_line_id <= 4 ORDER BY ol.order_line_id;
	  """.formatted(sjm.getMultiEntitySqlColumns(multiEntity));
	  
	  ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql,
	  sjm.resultSetExtractor(multiEntity));
	  
	  List<OrderLine> orderLines = resultListMap.getList(OrderLine.class);
	  List<Product> products = resultListMap.getList(Product.class);
	  rExtractor.type(orderLines).toOneList(products).joinOn("productId",
	  "id").populate("product");
	  
	  assertEquals(4, orderLines.size()); assertEquals("laces",
	  orderLines.get(2).getProduct().getName(), "line3 product name failed");
	  assertNull(orderLines.get(3).getProduct(),
	  "line3 product expected to be null failed");
	  
	  }

	@Test void toOne_withMultiEntityPositionSwitched_test() { // Product first
	  then Orderline MultiEntity multiEntity = new MultiEntity().add(Product.class,
	  "p").add(OrderLine.class, "ol");
	  
	  String sql = """ SELECT %s FROM order_line ol LEFT JOIN product p ON
	  ol.product_id = p.id WHERE ol.order_line_id <= 4 ORDER BY ol.order_line_id;
	  """.formatted(sjm.getMultiEntitySqlColumns(multiEntity));
	  
	  ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql,
	  sjm.resultSetExtractor(multiEntity));
	  
	  List<OrderLine> orderLines = resultListMap.getList(OrderLine.class);
	  List<Product> products = resultListMap.getList(Product.class);
	  rExtractor.type(orderLines).toOneList(products).joinOn("productId",
	  "id").populate("product");
	  
	  assertEquals(4, orderLines.size()); assertEquals("laces",
	  orderLines.get(2).getProduct().getName(), "line3 product name failed");
	  assertNull(orderLines.get(3).getProduct(),
	  "line3 product expected to be null failed");
	  
	  }

	@Test void toOne_NoResults_test() { MultiEntity multiEntity = new
	  MultiEntity().add(OrderLine.class, "ol").add(Product.class, "p");
	  
	  String sql = """ SELECT %s FROM order_line ol LEFT JOIN product p ON
	  ol.product_id = p.id WHERE ol.order_line_id < 0 ORDER BY ol.order_line_id;
	  """.formatted(sjm.getMultiEntitySqlColumns(multiEntity));
	  
	  ResultListMap resultListMap = sjm.getJdbcTemplate().query(sql,
	  sjm.resultSetExtractor(multiEntity));
	  
	  List<OrderLine> orderLines = resultListMap.getList(OrderLine.class);
	  List<Product> products = resultListMap.getList(Product.class);
	  
	  assertEquals(0, orderLines.size()); assertEquals(0, products.size());
	  
	  assertDoesNotThrow(() -> {
	  rExtractor.type(orderLines).toOneList(products).joinOn("productId",
	  "id").populate("product"); });
	  
	  }

}

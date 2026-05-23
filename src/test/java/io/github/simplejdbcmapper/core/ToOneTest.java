package io.github.simplejdbcmapper.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import io.github.simplejdbcmapper.relationship.Relationship;
import io.github.simplejdbcmapper.relationship.RelationshipMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ToOneTest {
	@Autowired
	private SimpleJdbcMapper sjm;

	@Test
	void toOne_joinOn_validation_test() {
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);
		List<Product> products = sjm.findAll(Product.class);

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(OrderLine.class, orderLines, "orderLineId");
		relMapper.addEntityResult(Product.class, products, "id");

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.type(OrderLine.class).toOne(Product.class).joinOn(null, "id");
		});
		assertTrue(exception.getMessage().contains("mainObjJoinProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.type(OrderLine.class).toOne(Product.class).joinOn("orderLineId", null);
		});
		assertTrue(exception.getMessage().contains("relatedObjJoinProperty must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.type(OrderLine.class).toOne(Product.class).joinOn("x", "id");
		});
		assertTrue(exception.getMessage().contains("does not exist for"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.type(OrderLine.class).toOne(Product.class).joinOn("orderLineId", "x");
		});
		assertTrue(exception.getMessage().contains("does not exist for"));

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

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(User.class, users, "id");
		relMapper.addEntityResult(ProfileUserIdLong.class, profiles, "id");

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.type(User.class).toOne(ProfileUserIdLong.class).joinOn("id", "userId").populate("profile");
		});
		assertTrue(exception.getMessage().contains("Conflicting property types."));

	}

	@Test
	void toOne_setProperty_validation_test() {
		List<OrderLine> orderLines = sjm.findAll(OrderLine.class);
		List<Product> products = sjm.findAll(Product.class);

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(OrderLine.class, orderLines, "orderLineId");
		relMapper.addEntityResult(Product.class, products, "id");

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			Relationship.type(OrderLine.class).toOne(Product.class).joinOn("productId", "id").populate(null);
		});
		assertTrue(exception.getMessage().contains("mainObjPropertyToPopulate must not be null"));

		exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.assemble(
					Relationship.type(OrderLine.class).toOne(Product.class).joinOn("productId", "id").populate("x"));
		});
		assertTrue(exception.getMessage().contains("Invalid argument. Property name"));

		Relationship.type(OrderLine.class).toOne(Product.class).joinOn("productId", "id").populate("product");

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

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(User.class, users, "id");
		relMapper.addEntityResult(Profile.class, profiles, "id");

		Relationship userToOneProfile = Relationship.type(User.class).toOne(Profile.class).joinOn("id", "userId")
				.populate("profile");

		relMapper.assemble(userToOneProfile);
		assertEquals("theme for user1", users.get(0).getProfile().getTheme());
		assertEquals("theme for user2", users.get(1).getProfile().getTheme());
		assertNull(users.get(2).getProfile());

		// reverse test ie populate profile with user
		Relationship profileToOneUser = Relationship.type(Profile.class).toOne(User.class).joinOn("userId", "id")
				.populate("user");

		relMapper.assemble(profileToOneUser);
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

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(User.class, users, "id");
		relMapper.addEntityResult(Profile.class, profiles, "id");

		Relationship userToOneProfile = Relationship.type(User.class).toOne(Profile.class).joinOn("id", "userId")
				.populate("profile");
		relMapper.assemble(userToOneProfile);

		assertEquals("theme for user2", users.get(2).getProfile().getTheme());

		// reverse test ie populate profile with user
		Relationship profileToOneUser = Relationship.type(Profile.class).toOne(User.class).joinOn("userId", "id")
				.populate("user");
		relMapper.assemble(profileToOneUser);
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

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(User.class, users, "id");
		relMapper.addEntityResult(Profile.class, profiles, "id");

		Exception exception = Assertions.assertThrows(Exception.class, () -> {
			relMapper.assemble(
					Relationship.type(User.class).toOne(Profile.class).joinOn("id", "userId").populate("name"));
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

		RelationshipMapper relMapper = new RelationshipMapper();
		relMapper.addEntityResult(User.class, users, "id");
		relMapper.addEntityResult(Profile.class, profiles, "id");

		relMapper.assemble(
				Relationship.type(User.class).toOne(Profile.class).joinOn("id", "userId").populate("profile"));
		assertEquals("theme for user2", users.get(2).getProfile().getTheme());

		// reverse test ie populate profile with user
		relMapper.assemble(Relationship.type(Profile.class).toOne(User.class).joinOn("userId", "id").populate("user"));
		assertEquals("user1", profiles.get(1).getUser().getName());

	}

	@Test
	void toOne_integration_test() {
		MultiEntity multiEntity = new MultiEntity().add(OrderLine.class, "ol").add(Product.class, "p");

		String sql = """
					SELECT %s
					FROM order_line ol
					LEFT JOIN product p ON ol.product_id = p.id
				    WHERE ol.order_line_id <= 4 ORDER BY ol.order_line_id;
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		Relationship orderLineToOneProduct = Relationship.type(OrderLine.class).toOne(Product.class)
				.joinOn("productId", "id").populate("product");

		List<OrderLine> orderLines = relMapper.assemble(orderLineToOneProduct).getList(OrderLine.class);

		assertEquals(4, orderLines.size());
		assertEquals("laces", orderLines.get(2).getProduct().getName(), "line3 product name failed");
		assertNull(orderLines.get(3).getProduct(), "line3 product expected to be null failed");

	}

	@Test
	void toOne_withMultiEntityPositionSwitched_test() {
		// Product first then Orderline
		MultiEntity multiEntity = new MultiEntity().add(Product.class, "p").add(OrderLine.class, "ol");

		String sql = """
					SELECT %s
					FROM order_line ol
					LEFT JOIN product p ON ol.product_id = p.id
					WHERE ol.order_line_id <= 4
					ORDER BY ol.order_line_id;
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper rMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		Relationship orderLineToOneProduct = Relationship.type(OrderLine.class).toOne(Product.class)
				.joinOn("productId", "id").populate("product");
		List<OrderLine> orderLines = rMapper.assemble(orderLineToOneProduct).getList(OrderLine.class);

		assertEquals(4, orderLines.size());
		assertEquals("laces", orderLines.get(2).getProduct().getName(), "line3 product name failed");
		assertNull(orderLines.get(3).getProduct(), "line3 product expected to be null failed");

	}

	@Test
	void toOne_NoResults_test() {
		MultiEntity multiEntity = new MultiEntity().add(OrderLine.class, "ol").add(Product.class, "p");

		String sql = """
				SELECT %s
				FROM order_line ol
				LEFT JOIN product p ON ol.product_id = p.id
				WHERE ol.order_line_id < 0
				ORDER BY ol.order_line_id;
				""".formatted(sjm.getMultiEntitySqlColumns(multiEntity));

		RelationshipMapper relMapper = sjm.getJdbcTemplate().query(sql, sjm.resultSetExtractor(multiEntity));

		Relationship orderLineToOneProduct = Relationship.type(OrderLine.class).toOne(Product.class)
				.joinOn("productId", "id").populate("product");
		assertDoesNotThrow(() -> {
			relMapper.assemble(orderLineToOneProduct).getList(OrderLine.class);
		});

		List<OrderLine> orderLines = relMapper.assemble(orderLineToOneProduct).getList(OrderLine.class);

		assertEquals(0, orderLines.size());

	}

}

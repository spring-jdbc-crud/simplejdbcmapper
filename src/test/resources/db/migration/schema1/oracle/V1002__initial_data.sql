
INSERT INTO SCHEMA1.orders
(order_date, customer_id, status, created_on, created_by, updated_on, updated_by, version, last_mapped_col)
VALUES(to_timestamp('2026-01-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),  1, 'IN PROCESS', to_timestamp('2026-01-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2026-01-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'order last col');

INSERT INTO SCHEMA1.orders
(order_date, customer_id, status, created_on, created_by, updated_on, updated_by,  version, last_mapped_col)
VALUES(to_timestamp('2026-02-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),  2, 'IN PROCESS',to_timestamp('2026-02-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2026-02-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'order last col');

INSERT INTO SCHEMA1.orders
(order_date, created_on, created_by, updated_on, updated_by,  version, last_mapped_col)
VALUES(to_timestamp('2026-03-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), to_timestamp('2026-03-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2026-03-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'order last col');

INSERT INTO SCHEMA1.orders
(order_date, created_on, created_by, updated_on, updated_by,  version, last_mapped_col)
VALUES(to_timestamp('2026-04-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), to_timestamp('2026-04-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2026-04-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'order last col');


INSERT INTO SCHEMA1.order_line
(order_id, product_id, num_of_units, last_mapped_col)
VALUES(1, 1, 10, 'orderline last col');

INSERT INTO SCHEMA1.order_line
(order_id, product_id, num_of_units, last_mapped_col)
VALUES(1, 2, 5, 'orderline last col');

INSERT INTO SCHEMA1.order_line
(order_id, product_id, num_of_units, last_mapped_col)
VALUES(2, 3, 1, 'orderline last col');

INSERT INTO SCHEMA1.order_line
(order_id, last_mapped_col)
VALUES(4, 'orderline last col');


INSERT INTO SCHEMA1.customer
(first_name, last_name)
VALUES( 'tony', 'joe');
INSERT INTO SCHEMA1.customer
(first_name, last_name)
VALUES('jane', 'doe');

INSERT INTO SCHEMA1.customer
(first_name, last_name)
VALUES('customer 3 test for property update', 'customer 3 last name');

INSERT INTO SCHEMA1.customer
(first_name, last_name)
VALUES('customer 4 test for no updateInfo and no version test', 'customer 4 last name');

INSERT INTO SCHEMA1.customer
(first_name, last_name)
VALUES(null, 'customer 5 last name whose first name is null');

INSERT INTO SCHEMA1.product
(id, name, cost, description, created_on, created_by, updated_on, updated_by, version, last_mapped_col)
VALUES(1, 'shoes', 95.00,'some description', to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'product last col');

INSERT INTO SCHEMA1.product
(id, name, cost, created_on, created_by, updated_on, updated_by, version, last_mapped_col)
VALUES(2, 'socks', 4.55,to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'product last col');

INSERT INTO SCHEMA1.product
(id, name, cost, created_on, created_by, updated_on, updated_by, version, last_mapped_col)
VALUES(3, 'laces', 1.25,to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'product last col');

INSERT INTO SCHEMA1.product
(id, name, cost, created_on, created_by, updated_on, updated_by, version, last_mapped_col)
VALUES(4, 'product4 for delete test', 1.25,to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'product last col');

INSERT INTO SCHEMA1.product
(id, name, cost, created_on, created_by, updated_on, updated_by, version, last_mapped_col)
VALUES(5, 'product5 for delete test', 1.25,to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'product last col');


INSERT INTO SCHEMA1.product
(id, name, cost, created_on, created_by, updated_on, updated_by, version, last_mapped_col)
VALUES(6, 'product 6 update test', 2.45,to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', to_timestamp('2020-06-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'system', 1, 'product last col');


INSERT INTO SCHEMA1.person
(person_id, first_name, last_name)
VALUES( 'person101', 'mike', 'smith');


INSERT INTO schema1.employee
(last_name, first_name)
VALUES ('emp1 last', 'emp1 first');

INSERT INTO schema1.employee
(last_name, first_name)
VALUES ('emp2 last', 'emp2 first');

INSERT INTO schema1.employee
(last_name, first_name)
VALUES ('emp3 last', 'emp3 first');

INSERT INTO schema1.employee
(last_name, first_name)
VALUES ('emp4 last', 'emp4 first');

INSERT INTO schema1.skill
(name)
VALUES ('java');

INSERT INTO schema1.skill
(name)
VALUES ('spring');

INSERT INTO schema1.skill
(name)
VALUES ('typescript');

INSERT INTO schema1.skill
(name)
VALUES ('ruby');


INSERT INTO schema1.employee_skill
(employee_id, skill_id)
VALUES (1, 1);

INSERT INTO schema1.employee_skill
(employee_id, skill_id)
VALUES (1, 2);

INSERT INTO schema1.employee_skill
(employee_id, skill_id)
VALUES (3, 2);

INSERT INTO schema1.employee_skill
(employee_id, skill_id)
VALUES (3, 3);

INSERT INTO schema1.employee_skill
(employee_id, skill_id)
VALUES (3, 4);



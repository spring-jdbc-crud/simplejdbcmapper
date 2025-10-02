create schema schema1 AUTHORIZATION dbo;
create schema schema2 AUTHORIZATION dbo;

create user user_schema1 for login user_schema1;
create user user_schema2 for login user_schema2;

alter role db_owner add member user_schema1;
alter role db_owner add member user_schema2;

alter user user_schema1 with default_schema = schema1;
alter user user_schema2 with default_schema = schema2;
GO
create schema schema1 AUTHORIZATION dbo;
GO
create schema schema2 AUTHORIZATION dbo;
GO
create user user_schema1 for login user_schema1;
GO
create user user_schema2 for login user_schema2;
GO
alter role db_owner add member user_schema1;
GO
alter role db_owner add member user_schema2;
GO
alter user user_schema1 with default_schema = schema1;
GO
alter user user_schema2 with default_schema = schema2;
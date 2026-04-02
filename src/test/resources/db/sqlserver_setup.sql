GO
create schema schema1 AUTHORIZATION dbo;
GO
create schema schema2 AUTHORIZATION dbo;
GO
CREATE LOGIN userSchema1 WITH PASSWORD = 'Mssql123';
GO
CREATE LOGIN userSchema2 WITH PASSWORD = 'Mssql123';
GO
create user userSchema1 for login userSchema1;
GO
create user userSchema2 for login userSchema2;
GO
alter role db_owner add member userSchema1;
GO
alter role db_owner add member user_Schema2;
GO
alter user userSchema1 with default_schema = schema1;
GO
alter user userSchema2 with default_schema = schema2;
-- Used in CI to setup oracle for tests

create user SCHEMA1 identified by app; 
grant all privileges to SCHEMA1;
grant SELECT ANY DICTIONARY to SCHEMA1;

create user SCHEMA2 identified by app;
grant all privileges to SCHEMA2;
grant SELECT ANY DICTIONARY to SCHEMA2;

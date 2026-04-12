-- Used in CI to setup oracle for tests
create user SCHEMA1 identified by app; 
grant all privileges to SCHEMA1;
grant SELECT ANY DICTIONARY to SCHEMA1;


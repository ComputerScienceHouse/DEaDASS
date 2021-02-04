SELECT * from pg_catalog.pg_tables;
select * from pg_catalog.pg_user;
select * from pg_catalog.pg_authid;
select * from pg_catalog.pg_auth_members;
select * from pg_catalog.pg_database;
select * from pg_catalog.pg_db_role_setting;
select * from information_schema.role_table_grants;


SELECT datname FROM pg_database WHERE datistemplate = false;

SELECT * from information_schema.schemata;

SELECT * FROM information_schema.tables ORDER BY table_schema,table_name;

SELECT datname,schema_name,schema_owner,table_name FROM pg_database LEFT JOIN information_schema.schemata ON pg_database.datname = information_schema.schemata.catalog_name LEFT JOIN information_schema.tables ON information_schema.schemata.schema_name = information_schema.tables.table_schema WHERE datistemplate = false;


SELECT grantee,
privilege_type, table_name
FROM information_schema.role_table_grants;


SELECT r.rolname, r.rolsuper, r.rolinherit,
  r.rolcreaterole, r.rolcreatedb, r.rolcanlogin,
  r.rolconnlimit, r.rolvaliduntil,
  ARRAY(SELECT b.rolname
        FROM pg_catalog.pg_auth_members m
        JOIN pg_catalog.pg_roles b ON (m.roleid = b.oid)
        WHERE m.member = r.oid) as memberof
, r.rolreplication
, r.rolbypassrls
FROM pg_catalog.pg_roles r
WHERE r.rolname !~ '^pg_'
ORDER BY 1;

select * from pg_namespace;
select * from pg_authid;
select * from pg_class;

GRANT ALL PRIVILEGES ON DATABASE foo TO foo;

select * from pg_tablespace;

  SELECT 
      r.rolname, 
      r.rolsuper, 
      r.rolinherit,
      r.rolcreaterole,
      r.rolcreatedb,
      r.rolcanlogin,
      r.rolconnlimit, r.rolvaliduntil,
  ARRAY(SELECT b.rolname
        FROM pg_catalog.pg_auth_members m
        JOIN pg_catalog.pg_roles b ON (m.roleid = b.oid)
        WHERE m.member = r.oid) as memberof
, r.rolreplication
, r.rolbypassrls
FROM pg_catalog.pg_roles r
ORDER BY 1;

ALTER USER foo WITH PASSWORD 'mysecretpassword'
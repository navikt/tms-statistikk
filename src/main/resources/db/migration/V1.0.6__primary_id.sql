alter table innlogging_per_dag add column id serial primary key;
alter table varsler_per_dag add column id serial primary key;
alter table utkast add primary key(utkast_id);

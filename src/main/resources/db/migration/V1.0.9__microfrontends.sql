create table if not exists microfrontends(
    id serial primary key,
    initiated_time timestamp not null,
    ident varchar(11) not null,
    action varchar(15) not null,
    microfrontend_id varchar(50) not null,
    initiated_by varchar(50)
);




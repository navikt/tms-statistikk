create table if not exists microfrontends(
    id serial primary key,
    time timestamp with time zone not null,
    ident varchar(11) not null,
    action varchar(15) not null,
    microfrontend_id varchar(50) not null
)


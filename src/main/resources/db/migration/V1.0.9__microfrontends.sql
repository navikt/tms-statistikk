create table if not exists microfrontends(
    id serial primary key,
    initiated_time timestamp not null,
    ident text not null,
    action text not null,
    microfrontend_id text not null,
    initiated_by text
);

CREATE index  microfrontend_ident_idx on microfrontends(ident);




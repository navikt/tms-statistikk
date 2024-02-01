drop table if exists beredskapsvarsel;

create table beredskapsvarsel
(
    id             serial primary key,
    beredskap_tittel varchar not null,
    beredskap_ref varchar not null unique
);

alter table varsel
add column beredskap_ref varchar default null;




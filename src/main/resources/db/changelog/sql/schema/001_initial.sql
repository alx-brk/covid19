set search_path = public;

create table if not exists statistics (
    stat_date date not null,
    country_code text not null ,
    country_slug text not null ,
    country text not null,
    cases bigint not null,
    primary key (stat_date, country_code)
);
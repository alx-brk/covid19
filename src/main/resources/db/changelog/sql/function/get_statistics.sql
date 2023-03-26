set search_path = public;

create or replace function get_statistics(i_country_slugs in text[],
                                          i_start_date in date,
                                          i_end_date in date)
    returns table
            (
                stat_date    date,
                country_slug text,
                country      text,
                country_code text,
                cases        bigint
            )
    language plpgsql
as
$get_statistics$
begin
    return query with series as (select date_trunc('day', dd)::date as s_date, country_slug.*
                                 from generate_series
                                          (i_start_date
                                          , coalesce(i_end_date, i_start_date)
                                          , '1 day'::interval) dd
                                          cross join unnest(i_country_slugs) country_slug)
                 select sr.s_date as stat_date, sr.country_slug, st.country, st.country_code, st.cases
                 from series sr
                          left join statistics st
                                    on sr.s_date = st.stat_date and sr.country_slug = st.country_slug;
end;
$get_statistics$;
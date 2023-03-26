# covid19

## How to build and run
1. It uses jooq and pojo generation based on database. So you have to run postgres first.
```
docker run --name covid-postgres -e POSTGRES_DB=covid -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres
```
2. Run
```
mvn clean install
```

## How to use
You can import openapi.yaml to postman and receive prepared collection.

Otherwise you can use any other tool.
Here is what you can use:
POST /statistics and /statistics/period
with body:
```
{
  "countries":[], // country slugs from original api
  "startDate": "2020-01-22",
  "endDate": "2020-01-25",
}
```
First one will return day from the period when there was maximum confirmed cases, and same for minimum.
Second one will aggregate number of cases for the whole period and return countries with max and min cases

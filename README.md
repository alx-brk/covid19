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
3. Run again, because first goes jooq generation and then liquibase scripts, and database schema is required for jooq generation.
```
mvn clean install
```

## How to use
You can import openapi.yaml to postman and receive prepared collection.

Otherwise you can use any other tool.
Here is what you can use:
POST /statistics and /statistics/period (see src/main/resources/openapi.yaml)
with body:
```
{
  "countries":[], // country slugs from original api
  "startDate": "2020-01-22",
  "endDate": "2020-01-25",
}
```
First one will return day from the period when there was maximum confirmed cases, and same for minimum.
Second one will aggregate number of cases for the whole period and return countries with max and min cases.

## Comments
* At the beginning I implemented POST /statistics, and then at the weekend I realized that maybe understood it wrong and implemented POST /statistics/period.
* Jooq generation requires to know exact port, so I had to avoid using testcontainers because in that case port would be random. So you have to use local database in order to build and run app.
* I made database function which forms kind of dashboard with such structure:
```
_________________________________________________________________________
|   statDate   |    countrySlug |   country | countryCode   |   cases   |
_________________________________________________________________________
|   2020-01-01 |    china       |   China   | CN            |   12345   |
_________________________________________________________________________
|   2020-01-02 |    china       |   null    | null          |   null    |
_________________________________________________________________________

```
It has statDate and countrySlug for each country and each day of period.
When there is some data in database, then cases will appear in dashboard.
Otherwise, there will be null and these cases will be fetched from remote api.
After fetching stats they will be inserted into database asynchronously.
* I didn't write to many tests. Actually, I made only one to save time. In the real life project tests coverage should be at least 80%.
* Also, it would be better for user experience to store data under https://api.covid19api.com/countries in database and provide searching request to find countrySlug for any country. This improvement would be one of the next to implement.

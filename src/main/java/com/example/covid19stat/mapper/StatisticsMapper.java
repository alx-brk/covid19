package com.example.covid19stat.mapper;

import com.example.openapi.samples.gen.springbootserver.model.CountryDayStat;
import com.example.openapi.samples.gen.springbootserver.model.CountryPeriodStat;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsForPeriodResponse;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsRequest;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsResponse;
import com.tej.JooQDemo.jooq.sample.model.Tables;
import com.tej.JooQDemo.jooq.sample.model.tables.records.GetStatisticsRecord;
import com.tej.JooQDemo.jooq.sample.model.tables.records.StatisticsRecord;
import org.jooq.DSLContext;
import org.jooq.impl.TableRecordImpl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class StatisticsMapper {

    public static final String STATISTICS = StatisticsRecord.class.getSimpleName();
    @Autowired
    private DSLContext dslContext;


    public abstract List<CountryDayStat> map(List<StatisticsRecord> records);

    @Mapping(target = "confirmedCases", source = "cases")
    @Mapping(target = "date", source = "statDate")
    public abstract CountryDayStat mapStat(StatisticsRecord record);

    public abstract StatisticsRecord map(GetStatisticsRecord record);

    @Mapping(target = "confirmedCases", source = "cases")
    @Mapping(target = "date", source = "statDate")
    public abstract CountryDayStat mapStat(GetStatisticsRecord record);

    public GetStatisticsResponse mapStatistics(List<GetStatisticsRecord> statisticsDashboard) {
        final CountryDayStat maxCases = statisticsDashboard.stream()
                .max(Comparator.comparing(GetStatisticsRecord::getCases))
                .map(this::mapStat)
                .orElseThrow();
        final CountryDayStat minCases = statisticsDashboard.stream()
                .min(Comparator.comparing(GetStatisticsRecord::getCases))
                .map(this::mapStat)
                .orElseThrow();
        return new GetStatisticsResponse()
                .maxConfirmed(maxCases)
                .minConfirmed(minCases);
    }

    public GetStatisticsForPeriodResponse mapStatisticsForPeriod(List<GetStatisticsRecord> statisticsDashboard, GetStatisticsRequest request) {
        Map<String, CountryDayStat> aggregated = statisticsDashboard.stream()
                .map(this::mapStat)
                .collect(Collectors.toMap(
                                CountryDayStat::getCountrySlug,
                                Function.identity(),
                                (p1, p2) -> p2.confirmedCases(sumLongs(p2.getConfirmedCases(), p1.getConfirmedCases()))
                        )
                );
        CountryPeriodStat max = aggregated.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .max(Comparator.comparing(CountryDayStat::getConfirmedCases))
                .map(s -> mapPeriodStat(s, request))
                .get();
        CountryPeriodStat min = aggregated.entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .min(Comparator.comparing(CountryDayStat::getConfirmedCases))
                .map(s -> mapPeriodStat(s, request))
                .get();

        return new GetStatisticsForPeriodResponse()
                .maxConfirmed(max)
                .minConfirmed(min);
    }


    @Mapping(target = "confirmedCases", source = "record.confirmedCases")
    @Mapping(target = "country", source = "record.country")
    @Mapping(target = "countryCode", source = "record.countryCode")
    @Mapping(target = "countrySlug", source = "record.countrySlug")
    @Mapping(target = "startDate", source = "request.startDate")
    @Mapping(target = "endDate", source = "request.endDate")
    public abstract CountryPeriodStat mapPeriodStat(CountryDayStat record, GetStatisticsRequest request);

    private Long sumLongs(Long val1, Long val2) {
        return BigInteger.valueOf(val1)
                .add(BigInteger.valueOf(val2))
                .longValue();
    }


    @ObjectFactory
    @SuppressWarnings("unchecked cast")
    protected <T extends TableRecordImpl<T>> T init(@TargetType Class<T> targetClass) {
        if (targetClass.getSimpleName().equals(STATISTICS)) {
            return (T) dslContext.newRecord(Tables.STATISTICS);
        }
        throw new IllegalArgumentException();
    }
}

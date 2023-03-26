package com.example.covid19stat.service;

import com.example.covid19stat.exception.InvalidRequestException;
import com.example.covid19stat.mapper.StatisticsMapper;
import com.example.covid19stat.model.ByCountryStatPartition;
import com.example.covid19stat.model.ByCountryStatResponse;
import com.example.covid19stat.persistence.StatisticsRepository;
import com.example.covid19stat.service.util.Covid19StatsAdapter;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsForPeriodResponse;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsRequest;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsResponse;
import com.tej.JooQDemo.jooq.sample.model.tables.records.GetStatisticsRecord;
import com.tej.JooQDemo.jooq.sample.model.tables.records.StatisticsRecord;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StatisticsService {

    private final Covid19StatsAdapter adapter;
    private final StatisticsRepository repository;
    private final StatisticsMapper mapper;

    public GetStatisticsResponse getStatistics(GetStatisticsRequest request) {
        validate(request);
        List<GetStatisticsRecord> statisticsDashboard = prepareDashBoard(request);
        return mapper.mapStatistics(statisticsDashboard);
    }

    public GetStatisticsForPeriodResponse getStatisticsForPeriod(GetStatisticsRequest request) {
        validate(request);
        List<GetStatisticsRecord> statisticsDashboard = prepareDashBoard(request);
        return mapper.mapStatisticsForPeriod(statisticsDashboard, request);
    }

    private void validate(GetStatisticsRequest request) {
        if (Objects.isNull(request.getStartDate())) {
            throw new InvalidRequestException("startDate is null");
        }
        if (CollectionUtils.isEmpty(request.getCountries())) {
            throw new InvalidRequestException("countries is empty");
        }
        if (Objects.nonNull(request.getEndDate()) && request.getStartDate().isAfter(request.getEndDate())) {
            throw new InvalidRequestException("startDate is after endDate");
        }
    }

    private List<GetStatisticsRecord> prepareDashBoard(GetStatisticsRequest request) {
        List<GetStatisticsRecord> statisticsDashboard = repository.getStatisticsDashboard(request.getCountries(), request.getStartDate(), request.getEndDate());

        statisticsDashboard
                .stream()
                .filter(r -> Objects.isNull(r.getCases()))
                .collect(Collectors.groupingBy(
                        GetStatisticsRecord::getCountrySlug
                ))
                .entrySet()
                .stream()
                .forEach(this::setCases);
        return statisticsDashboard;
    }

    //defines whole period for absent entries, query stats from remote, set to dashboard and call insert to database cache
    private void setCases(Map.Entry<String, List<GetStatisticsRecord>> pair) {
        LocalDate startDate = pair.getValue().stream()
                .map(GetStatisticsRecord::getStatDate)
                .min(LocalDate::compareTo)
                .get();
        LocalDate endDate = pair.getValue().stream()
                .map(GetStatisticsRecord::getStatDate)
                .max(LocalDate::compareTo)
                .get();
        Map<LocalDate, ByCountryStatPartition> aggregated = fetchRemoteStatistics(pair.getKey(), startDate, endDate);
        pair.getValue()
                .forEach(s -> {
                    if (Objects.isNull(s.getCases())) {
                        ByCountryStatPartition partition = aggregated.get(s.getStatDate());
                        s.setCases(partition.getCases().longValue());
                        s.setCountry(partition.getCountry());
                        s.setCountryCode(partition.getCountryCode());

                        addStatistics(s);
                    }
                });
    }

    @Async
    public void addStatistics(GetStatisticsRecord record) {
        StatisticsRecord newRecord = mapper.map(record);
        repository.addStatistics(newRecord);
    }

    //fetch absent statistics from remote and group to Map<date, partitionWithWithSumCases>
    public Map<LocalDate, ByCountryStatPartition> fetchRemoteStatistics(String countrySlug, LocalDate startDate, LocalDate endDate) {
        ByCountryStatResponse statisticsByCountryAndPeriod = adapter.getStatisticsByCountryAndPeriod(countrySlug, startDate, endDate);
        Map<LocalDate, ByCountryStatPartition> collect = statisticsByCountryAndPeriod
                .stream()
                .collect(Collectors.toMap(
                                ByCountryStatPartition::getDate,
                                Function.identity(),
                                (p1, p2) -> p2.addCases(p1.getCases())
                        )
                );
        return collect;
    }

}

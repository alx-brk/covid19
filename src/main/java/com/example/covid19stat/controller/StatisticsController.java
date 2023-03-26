package com.example.covid19stat.controller;

import com.example.covid19stat.service.StatisticsService;
import com.example.openapi.samples.gen.springbootserver.api.StatisticsApi;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsForPeriodResponse;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsRequest;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class StatisticsController implements StatisticsApi {

    private final StatisticsService service;

    @Override
    public ResponseEntity<GetStatisticsResponse> getStatistics(GetStatisticsRequest getStatisticsRequest) {
        return ResponseEntity.ok(service.getStatistics(getStatisticsRequest));
    }

    @Override
    public ResponseEntity<GetStatisticsForPeriodResponse> getStatisticsForPeriod(GetStatisticsRequest getStatisticsRequest) {
        return ResponseEntity.ok(service.getStatisticsForPeriod(getStatisticsRequest));
    }
}

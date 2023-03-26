package com.example.covid19stat.service.util;

import com.example.covid19stat.model.ByCountryStatResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@AllArgsConstructor
public class Covid19StatsAdapter {

    private final WebClient webClient;

    public ByCountryStatResponse getStatisticsByCountryAndPeriod(String countrySlug, LocalDate startDate, LocalDate endDate) {
        try {
            return webClient.get().uri(uriBuilder -> uriBuilder
                            .path("/country/{countrySlug}/status/confirmed")
                            .queryParam("from", startDate.atStartOfDay())
                            .queryParam("to", adjustEndDate(startDate, endDate))
                            .build(countrySlug)
                    )
                    .retrieve()
                    .bodyToMono(ByCountryStatResponse.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private LocalDateTime adjustEndDate(LocalDate startDate, LocalDate endDate){
        if (Objects.isNull(endDate)) {
            return startDate.atTime(23, 59, 59);
        } else {
            return endDate.atStartOfDay();
        }
    }
}

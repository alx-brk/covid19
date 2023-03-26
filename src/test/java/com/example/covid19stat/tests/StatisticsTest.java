package com.example.covid19stat.tests;

import com.example.covid19stat.model.ByCountryStatPartition;
import com.example.covid19stat.model.ByCountryStatResponse;
import com.example.covid19stat.persistence.StatisticsRepository;
import com.example.covid19stat.service.util.Covid19StatsAdapter;
import com.example.openapi.samples.gen.springbootserver.model.GetStatisticsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tej.JooQDemo.jooq.sample.model.tables.records.StatisticsRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class StatisticsTest {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StatisticsRepository repository;

    @MockBean
    private Covid19StatsAdapter adapter;

    @BeforeEach
    public void cleanTable() {
        repository.cleanTable();
    }

    @Test
    void check() throws Exception {
        repository.addStatistics(statisticsRecord(LocalDate.of(2021, 1, 1), 50l));
        repository.addStatistics(statisticsRecord(LocalDate.of(2021, 1, 2), 40l));
        repository.addStatistics(statisticsRecord(LocalDate.of(2021, 1, 5), 60l));

        when(adapter.getStatisticsByCountryAndPeriod(
                eq("china"),
                eq(LocalDate.of(2021, 1, 3)),
                eq(LocalDate.of(2021, 1, 4))
        ))
                .thenReturn(chinaStat());

        when(adapter.getStatisticsByCountryAndPeriod(
                eq("russia"),
                eq(LocalDate.of(2021, 1, 1)),
                eq(LocalDate.of(2021, 1, 5))
        ))
                .thenReturn(russiaStat());

        mvc.perform(request(HttpMethod.POST, "/statistics")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getStatisticsRequest()))
                )
                .andDo(print())
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.maxConfirmed.country").value("Russia"))
                .andExpect(jsonPath("$.maxConfirmed.countryCode").value("RU"))
                .andExpect(jsonPath("$.maxConfirmed.countrySlug").value("russia"))
                .andExpect(jsonPath("$.maxConfirmed.confirmedCases").value(200))
                .andExpect(jsonPath("$.maxConfirmed.date").value("2021-01-05"))
                .andExpect(jsonPath("$.minConfirmed.country").value("China"))
                .andExpect(jsonPath("$.minConfirmed.countryCode").value("CN"))
                .andExpect(jsonPath("$.minConfirmed.countrySlug").value("china"))
                .andExpect(jsonPath("$.minConfirmed.confirmedCases").value(40))
                .andExpect(jsonPath("$.minConfirmed.date").value("2021-01-02"));

        verify(adapter)
                .getStatisticsByCountryAndPeriod(
                        eq("china"),
                        eq(LocalDate.of(2021, 1, 3)),
                        eq(LocalDate.of(2021, 1, 4))
                );

        verify(adapter)
                .getStatisticsByCountryAndPeriod(
                        eq("russia"),
                        eq(LocalDate.of(2021, 1, 1)),
                        eq(LocalDate.of(2021, 1, 5))
                );
    }

    private ByCountryStatResponse chinaStat() {
        ByCountryStatResponse response = new ByCountryStatResponse();
        response.add(
                new ByCountryStatPartition("China", "CN", BigInteger.valueOf(45l), LocalDate.of(2021, 1, 3))
        );
        response.add(
                new ByCountryStatPartition("China", "CN", BigInteger.valueOf(55l), LocalDate.of(2021, 1, 4))
        );
        return response;
    }

    private ByCountryStatResponse russiaStat() {
        ByCountryStatResponse response = new ByCountryStatResponse();
        response.add(
                new ByCountryStatPartition("Russia", "RU", BigInteger.valueOf(100l), LocalDate.of(2021, 1, 1))
        );
        response.add(
                new ByCountryStatPartition("Russia", "RU", BigInteger.valueOf(150l), LocalDate.of(2021, 1, 2))
        );
        response.add(
                new ByCountryStatPartition("Russia", "RU", BigInteger.valueOf(100l), LocalDate.of(2021, 1, 3))
        );
        response.add(
                new ByCountryStatPartition("Russia", "RU", BigInteger.valueOf(160l), LocalDate.of(2021, 1, 4))
        );
        response.add(
                new ByCountryStatPartition("Russia", "RU", BigInteger.valueOf(200l), LocalDate.of(2021, 1, 5))
        );
        return response;
    }

    private StatisticsRecord statisticsRecord(LocalDate date, Long cases) {
        StatisticsRecord record = new StatisticsRecord();
        record.setCountry("China");
        record.setCountryCode("CN");
        record.setCountrySlug("china");
        record.setStatDate(date);
        record.setCases(cases);
        return record;
    }

    private GetStatisticsRequest getStatisticsRequest() {
        return new GetStatisticsRequest()
                .addCountriesItem("russia")
                .addCountriesItem("china")
                .startDate(LocalDate.of(2021, 1, 1))
                .endDate(LocalDate.of(2021, 1, 5));
    }
}

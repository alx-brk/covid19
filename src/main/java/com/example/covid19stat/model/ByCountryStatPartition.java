package com.example.covid19stat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ByCountryStatPartition {
    @JsonProperty("Country")
    private String country;
    @JsonProperty("CountryCode")
    private String countryCode;
    @JsonProperty("Cases")
    private BigInteger cases;
    @JsonProperty("Date")
    private LocalDate date;

    public ByCountryStatPartition addCases(BigInteger value) {
        if (Objects.isNull(cases)) {
            cases = value;
        } else {
            cases = cases.add(value);
        }
        return this;
    }
}

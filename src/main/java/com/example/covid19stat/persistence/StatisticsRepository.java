package com.example.covid19stat.persistence;

import com.tej.JooQDemo.jooq.sample.model.tables.records.GetStatisticsRecord;
import com.tej.JooQDemo.jooq.sample.model.tables.records.StatisticsRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static com.tej.JooQDemo.jooq.sample.model.Routines.getStatistics;
import static com.tej.JooQDemo.jooq.sample.model.Tables.STATISTICS;

@Component
@RequiredArgsConstructor
public class StatisticsRepository {

    private final DSLContext dslContext;

    public StatisticsRecord addStatistics(StatisticsRecord record) {
        return dslContext.insertInto(STATISTICS)
                .set(record)
                .onConflictDoNothing()
                .returning()
                .fetchOne();
    }

    public List<GetStatisticsRecord> getStatisticsDashboard(List<String> countryCodes, LocalDate startDate, LocalDate endDate) {
        return dslContext.selectFrom(getStatistics(countryCodes.toArray(new String[0]), startDate, endDate))
                .fetch();
    }

    public void cleanTable() {
        dslContext.deleteFrom(STATISTICS)
                .execute();
    }

}

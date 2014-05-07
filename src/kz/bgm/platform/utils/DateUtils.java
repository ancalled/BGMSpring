package kz.bgm.platform.utils;


import kz.bgm.platform.model.domain.Quarter;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;

public class DateUtils {

    public static java.sql.Date toSql(LocalDate ld) {
        return new java.sql.Date(toEpochMillis(ld));
    }

    public static java.sql.Date toSql(LocalDateTime ldt) {
        return new java.sql.Date(toEpochMillis(ldt));
    }

    public static LocalDate fromSql(java.sql.Date date) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault())
                .toLocalDate();
    }


    public static long toEpochMillis(LocalDate ld) {
        return Date.from(ld.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant())
                .getTime();
    }

    public static long toEpochMillis(LocalDateTime ldt) {
        return Date.from(ldt
                .atZone(ZoneId.systemDefault())
                .toInstant())
                .getTime();
    }


    public static List<Year> getQuartersBefore(LocalDate from, int quarters) {

        List<Year> yearsList = new ArrayList<>();
        int mnth = from.getMonthValue() + 1;
        int rest = 3 - (mnth % 3);
        LocalDate start = from.plus(rest, ChronoUnit.MONTHS);

        Year year = new Year(from.getYear());
        yearsList.add(year);

        for (int q = 0; q < quarters; q++) {
            Quarter quarter = new Quarter();

            for (int m = 0; m < 3; m++) {
                int mntCnt = q * 3 + m;
                LocalDate curr = start.minus(mntCnt, ChronoUnit.MONTHS);
                quarter.addMonth(new Month(curr));
            }
            quarter.setupLocal();

            if (year.getYear() != quarter.getYear()) {
                year = new Year(quarter.getYear());
                yearsList.add(year);
            }

            year.addQuartes(quarter);
        }

        return yearsList;
    }


    public static void main(String[] args) throws ParseException {

        LocalDate from = LocalDate.of(2014, 2, 1);

        int yearsAgo = 3;
        int quartersAgo = yearsAgo * 4;
        List<Year> years = getQuartersBefore(from, quartersAgo);

        String[] reportDates = {
                "2012-01-01", "2013-01-01", "2013-02-01", "2013-04-01",
                "2013-04-01", "2013-04-01", "2013-04-01", "2013-04-01",
                "2013-05-01", "2013-05-01", "2013-06-01", "2013-06-01",
                "2013-07-01", "2013-07-30", "2013-07-30", "2013-07-30",
                "2013-07-31", "2013-08-01", "2013-08-01", "2013-08-01",
                "2013-08-01", "2013-09-01", "2013-09-01", "2013-09-01"
        };

        DateTimeFormatter formatter = ofPattern("yyyy MMM dd");

        for (String reportDateStr : reportDates) {
            LocalDate reportDate = LocalDate.parse(reportDateStr, ofPattern("yyyy-MM-dd"));
            for (Year y : years) {
                for (Quarter q : y.getQuarters()) {
                    for (Month m : q.getMonths()) {
                        LocalDate monthStart = m.getLocalDate();
                        LocalDate monthEnd = monthStart.plus(1, ChronoUnit.MONTHS);

                        if (!reportDate.isBefore(monthStart) &&
                                reportDate.isBefore(monthEnd)) {
                            System.out.println(reportDate + "\t" +
                                    monthStart.format(formatter) + " - " +
                                    monthEnd.format(formatter));
                        }
                    }

                }
            }
        }
    }


}

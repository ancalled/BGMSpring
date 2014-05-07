package kz.bgm.platform.utils;

import kz.bgm.platform.model.domain.CustomerReport;
import kz.bgm.platform.model.domain.Quarter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Month {

    private Date date;
    private LocalDate localDate;

    private List<CustomerReport> reports = new ArrayList<>();

    public Month(Date date) {
        this.date = date;
    }

    public Month(LocalDate localDate) {
        this.localDate = localDate;
    }

    public Date getDate() {
        return date;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public List<CustomerReport> getReports() {
        return reports;
    }

    public void addReport(CustomerReport r) {
        reports.add(r);
    }
}

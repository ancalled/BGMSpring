package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.CustomerReport;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class CustomerReportMapper implements RowMapper<CustomerReport> {

    private final String prefix;

    public CustomerReportMapper() {
        this("");
    }

    public CustomerReportMapper(String tblPrefix) {
        this.prefix = tblPrefix;
    }

    @Override
    public CustomerReport mapRow(ResultSet rs, int i) throws SQLException {
        CustomerReport report = new CustomerReport();
        report.setId(rs.getLong(prefix + "id"));
        int type = rs.getInt(prefix + "type");
        report.setType(CustomerReport.Type.values()[type]);

        if (report.getType() == CustomerReport.Type.MOBILE) {
            long customerId = rs.getLong(prefix + "customer_id");
            report.setCustomerId(customerId);
        }

        report.setStartDate(LocalDate.ofEpochDay(rs.getDate(prefix + "start_date").getTime()));
        int period = rs.getInt(prefix + "period");
        report.setPeriod(CustomerReport.Period.values()[period]);
        report.setUploadDate(
                LocalDateTime.ofEpochSecond(
                        rs.getDate(prefix + "upload_date").getTime(),
                        0, ZoneOffset.MIN));

        report.setTracks(rs.getInt(prefix + "tracks"));
        report.setDetected(rs.getInt(prefix + "detected"));
        report.setRevenue(rs.getLong(prefix + "revenue"));
        report.setAccepted(rs.getBoolean(prefix + "accepted"));

        return report;
    }
}

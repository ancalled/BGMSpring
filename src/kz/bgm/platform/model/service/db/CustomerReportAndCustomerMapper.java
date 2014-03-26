package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Customer;
import kz.bgm.platform.model.domain.CustomerReport;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerReportAndCustomerMapper implements RowMapper<CustomerReport> {

    private final CustomerReportMapper reportMapper;
    private final CustomerMapper customerMapper;

    public CustomerReportAndCustomerMapper(String reportPrefix, String customerPrefix) {
        reportMapper = new CustomerReportMapper(reportPrefix);
        customerMapper = new CustomerMapper(customerPrefix);
    }

    @Override
    public CustomerReport mapRow(ResultSet rs, int i) throws SQLException {
        CustomerReport report = reportMapper.mapRow(rs, i);
        Customer customer = customerMapper.mapRow(rs, i);
        report.setCustomer(customer);
        return report;
    }
}

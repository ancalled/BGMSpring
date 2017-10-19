package kz.bgm.platform.utils;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import kz.bgm.platform.model.domain.Customer;
import kz.bgm.platform.model.domain.CustomerReport;
import kz.bgm.platform.model.domain.CustomerReportItem;
import kz.bgm.platform.model.service.AdminServiceImpl;
import kz.bgm.platform.model.service.CustomerReportServiceImpl;
import kz.bgm.platform.model.service.MainServiceImpl;
import kz.bgm.platform.model.service.SearchServiceImpl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("Duplicates")
public class ReportProcessor {

    private static final String SRC_JDBC_PROPERTIES = "./src/jdbc.properties";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private AdminServiceImpl adminService = new AdminServiceImpl();
    private CustomerReportServiceImpl reportService = new CustomerReportServiceImpl();
    private SearchServiceImpl searchService = new SearchServiceImpl();
    private MainServiceImpl mainService = new MainServiceImpl();


    private ReportProcessor() throws IOException {
        Properties dbProps = new Properties();
        dbProps.load(new FileReader(SRC_JDBC_PROPERTIES));
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(dbProps.getProperty("jdbc.username"));
        dataSource.setPassword(dbProps.getProperty("jdbc.password"));
        dataSource.setURL(dbProps.getProperty("jdbc.url"));

        adminService.setDataSource(dataSource);
        reportService.setDataSource(dataSource);
        searchService.setDataSource(dataSource);
        searchService.initSearcher();
        mainService.setDataSource(dataSource);
    }

    private String uploadMobileReport(
            String repDate,
            String customerName,
            File file
    ) throws IOException {

        String reportFile = file.getAbsolutePath();

        System.out.println("Processing report from file: " + reportFile);

        LocalDate startDate = LocalDate.parse(repDate, DATE_FORMATTER);

        Customer customer = adminService.getCustomer(customerName);
        if (customer == null) {
            System.err.println("Customer not found!");
            return "";
        }

        CustomerReport report = new CustomerReport();
        report.setCustomer(customer);
        report.setCustomerId(customer.getId());
        report.setUploadDate(LocalDateTime.now());
        report.setStartDate(startDate);
        report.setPeriod(CustomerReport.Period.MONTH);
        report.setType(CustomerReport.Type.MOBILE);
        List<CustomerReportItem> items = ReportParser.parseItemsFromCsv(reportFile, ";");
        System.out.println("Found " + items.size() + " items");

        report.setTracks(items.size());

        long reportId = reportService.saveCustomerReport(report);
        System.out.println("Report id: " + reportId);

        report.setId(reportId);

        List<CustomerReportItem> detected = new ArrayList<>();
        for (CustomerReportItem i : items) {
            i.setReportId(reportId);
            searchService.searchWithoutDuplicates(i.getArtist(), i.getTrack(), mainService.getAllCatalogIds())
                    .forEach(sr -> detected.add(i.duplicate(sr)));
        }

        System.out.println("Detected " + detected.size() + " items");

        reportService.saveCustomerReportItems(detected);

        return "/reports/report-upload-result";
    }

    public static void main(String[] args) {
        ReportProcessor rp = null;
        try {
            rp = new ReportProcessor();
            rp.uploadMobileReport("2017-10-19", "GSMTech Management", new File("/media/sdb/Downloads/BGM/reports/08_17_ММKZ.csv"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

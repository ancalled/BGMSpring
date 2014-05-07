package kz.bgm.platform.test;


import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.AdminService;
import kz.bgm.platform.model.service.CustomerReportService;
import kz.bgm.platform.model.service.MainService;
import kz.bgm.platform.model.service.SearchService;
import kz.bgm.platform.utils.ReportParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class SearchTest {


    private List<Long> catalogs = new ArrayList<>();

    public static final String USER_DIR = System.getProperty("user.dir");
    private final SearchService searchService;
    private final MainService mainService;
    private final CustomerReportService customerReportService;
    private final AdminService adminService;
    public static final NumberFormat NF = new DecimalFormat("###.##");


    public SearchTest() throws IOException {
        System.out.println("USER_DIR = " + USER_DIR);

        ApplicationContext context = new FileSystemXmlApplicationContext("/" + USER_DIR + "/web/WEB-INF/mvc-dispatcher-servlet.xml");
        searchService = context.getBean(SearchService.class);
        mainService = context.getBean(MainService.class);
        customerReportService = context.getBean(CustomerReportService.class);
        adminService = context.getBean(AdminService.class);


        List<Long> available = mainService.getAllCatalogIds();
//        List<Long> available = mainService.getNotEnemyCatalogIds();
        catalogs.addAll(available);
    }


    public List<SearchResultItem> search(String query) {
        String[] split = query.split("; ");
        return searchService.searchWithoutDuplicates(split[0], split[1], catalogs);
    }

    public void processMobileReport(String customerName, String reportFile, LocalDate startDate)
            throws IOException, ParseException {
        System.out.println("Processing report from file: " + reportFile);

        Customer customer = adminService.getCustomer(customerName);
        if (customer == null) {
            System.err.println("Customer not found!");
            return;
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

        long reportId = customerReportService.saveCustomerReport(report);
        System.out.println("Report id: " + reportId);

        report.setId(reportId);

        List<CustomerReportItem> detected = new ArrayList<>();
        for (CustomerReportItem i : items) {
            i.setReportId(reportId);
            searchService.searchWithoutDuplicates(i.getArtist(), i.getTrack(), catalogs)
                    .forEach(sr -> detected.add(i.duplicate(sr)));
        }

        System.out.println("Detected " + detected.size() + " items");

        customerReportService.saveCustomerReportItems(detected);
    }


    public static String fixedWidth(String text, int len) {
        if (text.length() > len) return text.substring(0, len - 4) + "...";

        StringBuilder buf = new StringBuilder();
        buf.append(text);
        for (int i = text.length(); i < len; i++) {
            buf.append(" ");
        }

        return buf.toString();
    }

    public static String getShortRightType(RightType type) {
        switch (type) {
            case AUTHOR:
                return "A";
            case RELATED:
                return "R";
            case AUTHOR_RELATED:
                return "AR";
        }

        return "";
    }




    public static void main(String[] args) throws IOException {

        final SearchTest searcher = new SearchTest();
        //"Pink; Blow Me (One Last Kiss)"

//        List<SearchResultItem> res = searcher.searchWithoutDuplicates("МИРАЖ; Снова вместе");
//
//        res.stream()
//                .filter(i -> i.getTrack() != null)
//                .forEach(i ->
//                        System.out.println(i.getScore()
//                                        + ", " + i.getTrack().getCode()
//                                        + ", " + i.getTrack().getArtist()
//                                        + ", " + i.getTrack().getName()
//                                        + ", " + i.getTrack().getCatalog()
//                                        + ", " + i.getTrack().getMobileShare()
//                        ));

        List<String> lines = Files.readAllLines(Paths.get("/home/ancalled/Documents/tmp/41/bgm/csv", "moskvafm.csv"));
        lines.forEach(l -> {

            String[] split = l.split(";");
            String artist = split[0];
            String track = split[1];
            String qty = split[2];

//            System.out.println(artist + ": " + track);
            List<SearchResultItem> res = searcher.searchService.searchWithoutDuplicates(artist, track,
                    searcher.mainService.getAllCatalogIds());

            res.stream()
                    .filter(i -> i.getTrack() != null)
                    .forEach(i ->
//                            System.out.println("\t"
//                                            + fixedWidth("[" + nf.format(i.getScore()) + "]", 10)
//                                            + fixedWidth("(" +
//                                            getShortRightType(i.getTrack().getFoundCatalog().getRightType()) + ") " +
//                                            i.getTrack().getFoundCatalog().getName(), 29)
//                                            + fixedWidth(i.getTrack().getFoundCatalog().getPlatform().getName(), 10)
//                                            + fixedWidth(i.getTrack().getCode(), 15)
//                                            + fixedWidth(i.getTrack().getMobileShare() + "", 10)
//                                            + i.getTrack().getArtist() + ": " + i.getTrack().getName()
//                            ));

                            System.out.println(i.getTrack().getFoundCatalog().getRightType() + ";" +
                                            i.getTrack().getFoundCatalog().getName() + ";" +
                                            i.getTrack().getFoundCatalog().getPlatform().getName() + ";" +
                                            i.getTrack().getCode().trim() + ";" +
                                            i.getTrack().getMobileShare() + ";" +
                                            i.getTrack().getArtist().replace(";", ",") + ";" +
                                            i.getTrack().getComposer().replace(";", ",") + ";" +
                                            i.getTrack().getName().replace(";", ",")    + ";" +
                                            qty
                            ));

//            System.out.println();
        });

    }




    public static void main1(String[] args) throws IOException, ParseException {
        String customer = "GSMTech Management";
//        String customer = "Библиотека мобильного контента";
        String reportFile = "/home/ancalled/Documents/tmp/41/bgm/csv/gsmtech-january.csv";
//        String reportFile = "/home/ancalled/Documents/tmp/41/bgm/csv/gsmtech-febrary.csv";
//        String reportFile = "/home/ancalled/Documents/tmp/41/bgm/csv/gsmtech-march.csv";
//        String reportFile = "/home/ancalled/Documents/tmp/41/bgm/csv/bmk-january.csv";
//        String reportFile = "/home/ancalled/Documents/tmp/41/bgm/csv/bmk-febrary.csv";
//        String reportFile = "/home/ancalled/Documents/tmp/41/bgm/csv/bmk-march.csv";


        final SearchTest searcher = new SearchTest();
        searcher.processMobileReport(customer, reportFile, LocalDate.of(2014, Month.JANUARY, 1));

    }
}

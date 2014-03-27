package kz.bgm.platform.web.controllers;


import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.AdminService;
import kz.bgm.platform.model.service.CustomerReportService;
import kz.bgm.platform.model.service.SearchService;
import kz.bgm.platform.utils.DateUtils;
import kz.bgm.platform.utils.Month;
import kz.bgm.platform.utils.ReportParser;
import kz.bgm.platform.utils.Year;
import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/reports")
public class Reporter {

    public static final Logger log = Logger.getLogger(Reporter.class);

    public static final String APP_HOME = System.getProperty("user.dir");
    public static final String REPORTS_HOME = APP_HOME + "/reports";

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final String FILE = "file";
    public static final int LIMIT = 10;


    @Autowired
    private CustomerReportService reportService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private AdminService adminService;


    @RequestMapping(value = "/reports")
    public String showReports(Model model,
                              @RequestParam(value = "from", required = false, defaultValue = "2011-01")
                              @DateTimeFormat(pattern = "yyyy-MM")
                              Date from,
                              @RequestParam(value = "quarters") String quartersAgoStr,
                              @RequestParam(value = "non-active") String showNonActiveStr
    ) {

        List<Customer> customers = adminService.getAllCustomers();
        model.addAttribute("customers", customers);


        int quartersAgo = 6;
        if (quartersAgoStr != null) {
            quartersAgo = Integer.parseInt(quartersAgoStr);
        }

        boolean showNonAccepted = "yes".equals(showNonActiveStr);

        int monthsAgo = quartersAgo * 3;

        Date notLaterThen = DateUtils.getPreviousMonth(from, monthsAgo);

        List<CustomerReport> reports = reportService.getAllCustomerReports(notLaterThen);

        List<Year> years = DateUtils.getQuartersBefore(from, quartersAgo);
        for (CustomerReport r : reports) {
            if (!showNonAccepted && !r.isAccepted()) continue;

            Date reportDate = r.getStartDate();

            for (Year y : years) {
                for (Quarter q : y.getQuarters()) {
                    for (Month m : q.getMonths()) {
                        Date monthStart = m.getDate();
                        Date monthEnd = DateUtils.getNextMonth(monthStart, 1);

                        if (!reportDate.before(monthStart) &&
                                reportDate.before(monthEnd)) {
                            m.addReport(r);
                        }
                    }
                }
            }
        }

        model.addAttribute("now", from);
        model.addAttribute("years", years);

        return "/reports/reports-incoming";
    }


    @RequestMapping(value = "/customer-reports")
    public String showAllCustomerReports(Model model,
                                         HttpSession ses,
                                         @RequestParam(value = "from", required = true)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd")
                                         Date from,
                                         @RequestParam(value = "to", required = true)
                                         @DateTimeFormat(pattern = "yyyy-MM-dd")
                                         Date to
    ) {
        User user = (User) ses.getAttribute("user");
        if (user != null) {
            List<CustomerReport> reports = reportService.
                    getCustomerReports(user.getCustomerId(), from, to);

            model.addAttribute("reports", reports);
        }

        return "/reports/all-customer-reports";
    }


    @RequestMapping(value = "/report")
    public String showReport(Model model,
                             @RequestParam(value = "id", required = true) long reportId,
                             @RequestParam(value = "from", defaultValue = "0") int from,
                             @RequestParam(value = "size", defaultValue = "50") int size,
                             @RequestParam(value = "page", defaultValue = "0") int page
    ) {

        CustomerReport report = reportService.getCustomerReport(reportId);
        List<CustomerReportItem> items = reportService.getCustomerReportsItems(reportId, from, size);

        model.addAttribute("report", report)
                .addAttribute("items", items)
                .addAttribute("from", from)
                .addAttribute("size", size)
                .addAttribute("page", page);

        return "/reports/report";
    }


    @RequestMapping(value = "/upload-mobile-report", method = RequestMethod.POST)
    public String uploadMobileReport(
            HttpSession ses,
            @RequestParam("file") MultipartFile file
    ) throws IOException {


//        try {
        log.info("got request admin uploader report");

        CustomerReport report = new CustomerReport();

        String updateFilePath = REPORTS_HOME + "/" + file.getName();

        Path path = Paths.get(updateFilePath);
        Files.write(path, file.getBytes());

        List<CustomerReportItem> allItems = ReportParser.parseMobileReport(updateFilePath);

        Date now = new Date();

        report.setUploadDate(now);
        report.setTracks(allItems.size());
        report.setType(CustomerReport.Type.MOBILE);

        long reportId = reportService.saveCustomerReport(report);
        report.setId(reportId);

        List<ReportItemTrack> tracks = new ArrayList<>();

        for (CustomerReportItem i : allItems) {
            i.setReportId(reportId);

            long itemId = reportService.saveCustomerReportItem(i);

            List<SearchResult> found = null;
            try {
                found = searchService.search(i.getArtist(), i.getAuthors(), i.getTrack(), LIMIT);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }

            if (found != null) {
                for (SearchResult r : found) {
                    tracks.add(new ReportItemTrack(itemId, r.getTrackId(), r.getScore()));
                }
            }
        }


        ses.setAttribute("report-" + reportId, report);

        return "redirect:/admin/view/report-upload-result.jsp?rid=" + reportId;


    }


    @RequestMapping(value = "/upload-public-report", method = RequestMethod.POST)
    public String uploadPublicReport(HttpServletRequest req) {
//        ServletFileUpload fileUploader = new ServletFileUpload(new DiskFileItemFactory());
//
//        try {
////            String dateParam = req.getParameter("dt");
////            Date reportDate = dateParam != null ? FORMAT.parse(dateParam) : new Date();
//
//            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
//            Date reportDate = format.parse("2012/01/01");
//
////            String periodParam = req.getParameter("per");
////            int per = periodParam != null ? Integer.parseInt(periodParam) : 0;
////            CustomerReport.Period period = periodParam != null ?
////                    CustomerReport.Period.values()[per] :
////                    CustomerReport.Period.MONTH;
//
//            CustomerReport.Period period = CustomerReport.Period.MONTH;
//            List<FileItem> files = fileUploader.parseRequest(req);
//
//            if (files == null) {
//                return "redirect:/reports/report?er=no-freports-uploaded";
//            }
//
//
//            List<CustomerReportItem> allItems = new ArrayList<>();
//            for (FileItem item : files) {
//
//                String reportFile = REPORTS_HOME + "/" + item.getName();
//                saveToFile(item, reportFile);
//
//                log.info("Got client report " + item.getName());
//
////                List<CustomerReportItem> items = ReportParser.parsePublicReport(reportFile);
////                allItems.addAll(items);
//            }
//
//            Date now = new Date();
//
//            CustomerReport report = new CustomerReport();
//            report.setStartDate(reportDate);
//            report.setPeriod(period);
//            report.setUploadDate(now);
//            report.setType(CustomerReport.Type.PUBLIC);
//            report.setTracks(allItems.size());
//
//            long reportId = dbService.saveCustomerReport(report);
//            report.setId(reportId);
//
//            int detected = 0;
//
//            for (CustomerReportItem i : allItems) {
//                i.setReportId(reportId);
//                List<SearchResult> ids = luceneService.search(i.getArtist(), null, i.getTrack(), 100);
//                if (ids.size() > 0) {
//                    i.setCompositionId(ids.get(0).getTrackId());
//                    detected++;
//                }
//            }
//            log.info("composition detected " + detected);
//
//            report.setDetected(detected);
//
//            dbService.saveCustomerReportItems(allItems);
//
//            HttpSession ses = req.getSession(true);
//            ses.setAttribute("report-" + reportId, report);
//
//            return "redirect:/admin/view/report-upload-result?rid=" + reportId;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "redirect:/admin/reports.html?er=" + e.getMessage();
//        }

        return null;
    }


    @RequestMapping(value = "/report-upload-result")
    public String showReportUploadResult(Model model,
                                         @RequestParam(value = "rid", required = true) long reportId,
                                         HttpSession ses) {

        CustomerReport report = (CustomerReport) ses.getAttribute("report-" + reportId);
        if (report != null) {
            model.addAttribute("report", report);
        }

        return "/reports/report-upload-result";
    }


    @RequestMapping(value = "/accept-report", method = RequestMethod.POST)
    public String acceptReport(
            HttpSession session,
            @RequestParam(value = "reportId") long reportId
    ) {

        AdminUser admin = (AdminUser) session.getAttribute("admin");

        if (admin == null) {
            System.err.println("User not found!");
            return "redirect:/admin/view/report?id=" + reportId + "&er=user-not-found";
        }

        CustomerReport report = reportService.getCustomerReport(reportId);

        if (report == null) {
            System.err.println("Report not found!");
            return "redirect:/admin/view/report?id=" + reportId + "&er=report-not-found";
        }

        reportService.acceptReport(reportId);

        return "redirect:/reports/report?id=" + reportId;
    }


//
//    @RequestMapping(value = "/report-calculator")
//    public String showReportCalculator(Model model) {
//        Collection<Platform> platforms = dbService.getPlatforms();
//        model.addAttribute("platforms", platforms);
//
//        return "/reports/report-calculator";
//    }


//    private void fillItems(List<FileItem> fields, CustomerReport report, List<CustomerReportItem> allItems) throws Exception {
//        for (FileItem item : fields) {
//            if (item.isFormField()) {
//                fillParam(item, report);
//            } else {
//
//                String reportFile = REPORTS_HOME + "/" + item.getName();
//                saveToFile(item, reportFile);
//
//                log.info("Got client report " + item.getName());
//
////                List<CustomerReportItem> items = ReportParser.parseMobileReport(reportFile);
////                allItems.addAll(items);
//            }
//        }
//    }
//
//
//    public CustomerReport fillParam(FileItem item, CustomerReport customerReport) {
//        String param = item.getFieldName();
//        String value = item.getString();
//
//        switch (param) {
//            case "dt":
//                log.info("--Report date =" + value);
//
//                Date reportDate = null;
//                try {
//                    reportDate = value != null ? FORMAT.parse(value) : new Date();
//                } catch (ParseException e) {
//                    log.warn(e.getMessage());
//                }
//
//                customerReport.setStartDate(reportDate);
//                break;
//
//            case "period":
//                int per = value != null ? Integer.parseInt(value) : 0;
//                CustomerReport.Period period = value != null ?
//                        CustomerReport.Period.values()[per] :
//                        CustomerReport.Period.MONTH;
//
//                log.info("--Report period =" + period);
//
//                customerReport.setPeriod(period);
//                break;
//
//            case "cid":
//                log.info("--Report customer id =" + value);
//
//                long customerId = Long.parseLong(value);
//                Customer customer = dbService.getCustomer(customerId);
//
//                if (customer == null) {
//                    log.info("Customer not found");
//                    return null;
//                }
//                customerReport.setCustomerId(customer.getId());
//                break;
//        }
//
//
//        return customerReport;
//    }
//
//    private void saveToFile(FileItem item, String filename) throws Exception {
//        log.info("File name:" + item.getName());
//        File reportFile = new File(filename);
//        item.write(reportFile);
//    }
//

}

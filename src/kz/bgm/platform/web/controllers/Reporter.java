package kz.bgm.platform.web.controllers;


import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.AdminService;
import kz.bgm.platform.model.service.CustomerReportService;
import kz.bgm.platform.model.service.MainService;
import kz.bgm.platform.model.service.SearchService;
import kz.bgm.platform.utils.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;

@Controller
@RequestMapping(value = "/reports")
public class Reporter {

    public static final Logger log = Logger.getLogger(Reporter.class);

    public static final String APP_HOME = System.getProperty("user.dir");
    public static final String REPORTS_HOME = APP_HOME + "/reports";

//    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final String FILE = "file";
    public static final int LIMIT = 10;

    public static final String FROM_PATTERN = "yyyy-MM";


    @Autowired
    private CustomerReportService reportService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private MainService mainService;

    @Autowired
    private AdminService adminService;


    @RequestMapping(value = "/reports")
    public String showReports(Model model,
                              @RequestParam(value = "from", required = false)
                              String fromDate,
                              @RequestParam(value = "quarters", required = false, defaultValue = "6") int quartersAgo,
                              @RequestParam(value = "non-active", required = false) String showNonActive
    ) {

        List<Customer> customers = adminService.getAllCustomers();
        model.addAttribute("customers", customers);

        boolean showNonAccepted = "yes".equals(showNonActive);

        LocalDate from = fromDate != null ?
                LocalDate.parse(fromDate, ofPattern(FROM_PATTERN)) :
                LocalDate.now();

        LocalDate notLaterThen = from.minus(quartersAgo * 3, ChronoUnit.MONTHS);

        List<CustomerReport> reports = reportService.getAllCustomerReports(notLaterThen);

        List<Year> years = DateUtils.getQuartersBefore(from, quartersAgo);
        for (CustomerReport r : reports) {
            if (!showNonAccepted && !r.isAccepted()) continue;

            LocalDate reportDate = r.getStartDate();

            for (Year y : years) {
                for (Quarter q : y.getQuarters()) {
                    for (Month m : q.getMonths()) {
                        LocalDate monthStart = m.getLocalDate();
                        LocalDate monthEnd = monthStart.plus(1, ChronoUnit.MONTHS);

                        if (!reportDate.isBefore(monthStart) &&
                                reportDate.isBefore(monthEnd)) {
                            m.addReport(r);
                        }
                    }
                }
            }
        }

        model.addAttribute("now", from);
        model.addAttribute("years", years);

        return "/reports/reports";
    }


    @RequestMapping(value = "/customer-reports")
    public String showAllCustomerReports(Model model,
                                         HttpSession ses,
                                         @RequestParam(value = "from", required = true)
//                                         @DateTimeFormat(pattern = "yyyy-MM-dd")
                                         String from,
                                         @RequestParam(value = "to", required = true)
//                                         @DateTimeFormat(pattern = "yyyy-MM-dd")
                                         String to
    ) {
        User user = (User) ses.getAttribute("user");
        if (user != null) {
            List<CustomerReport> reports = reportService.
                    getCustomerReports(user.getCustomerId(),
                            LocalDate.parse(from),
                            LocalDate.parse(to)
                    );

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
        if (report != null) {
            Customer customer = adminService.getCustomer(report.getCustomerId());
            List<CustomerReportItem> items = reportService.getCustomerReportsItems(reportId, from, size);

            model.addAttribute("report", report)
                    .addAttribute("customer", customer)
                    .addAttribute("items", items)
                    .addAttribute("from", from)
                    .addAttribute("size", size)
                    .addAttribute("page", page);
        }

        return "/reports/report";
    }


    @RequestMapping(value = "/upload-mobile-report", method = RequestMethod.POST)
    public String uploadMobileReport(
            HttpSession ses,
            @RequestParam("date") String repDate,
            @RequestParam("customer") String customerName,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        String reportFile = file.getName();
        Files.write(Paths.get(reportFile), file.getBytes());


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


    @RequestMapping(value = "/upload-public-report", method = RequestMethod.POST)
    public String uploadPublicReport(HttpServletRequest req) {


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


    //    private CatalogStorage storage;
//    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
//
//    @Override
//    public void init() throws ServletException {
//
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//
//        resp.setContentType("application/json");
//        resp.setCharacterEncoding("UTF-8");
//        PrintWriter out = resp.getWriter();
//        JSONObject jsonObj = new JSONObject();
//        try {
//
//            String platform = req.getParameter("platform");
//            String type = req.getParameter("type");
//            String fromDateStr = req.getParameter("from");
//            String toDateStr = req.getParameter("to");
//
//            log.info(" Got request with params \n" +
//                    "platform  "+platform+"\n" +
//                    "type      "+type+"\n" +
//                    "from date "+fromDateStr+"\n" +
//                    "to date   "+toDateStr);
//
//            Date from = FORMAT.parse(fromDateStr);
//            Date to = FORMAT.parse(toDateStr);
//
//            log.info("Calculating report");
//
//            List<CalculatedReportItem> items;
//            switch (type) {
//                case "mobile":
//                    items = storage.calculateMobileReport(platform, from, to);
//                    break;
//                case "public":
//                    items = storage.calculatePublicReport(platform, from, to);
//                    break;
//                default:
//                    jsonObj.put("status", "error");
//                    jsonObj.put("er", "Unknown report type: " + type);
//                    jsonObj.writeJSONString(out);
//                    return;
//            }
//
//            log.info("Calculating done report items size "+items.size());
//
//            jsonObj.put("type", type);
//            jsonObj.put("status", "ok");
//
//            JSONArray array = makeJsonArray(items);
//
//            jsonObj.put("report_items", array);
//            jsonObj.writeJSONString(out);
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.info(e.getMessage());
////            resp.sendRedirect(RESULT_URL + "?er=" + e.getMessage());
//            jsonObj.put("status", "error");
//            jsonObj.put("er", e.getMessage());
//            jsonObj.writeJSONString(out);
//
//        }
//    }
//
//
//    @SuppressWarnings("unchecked")
//    private JSONArray makeJsonArray(List<CalculatedReportItem> reports) {
//        if (reports == null) return null;
//        JSONArray mainArray = new JSONArray();
//
//        for (CalculatedReportItem rp : reports) {
//            JSONObject jsonReport = new JSONObject();
//            jsonReport.put("qty", rp.getQty());
//            jsonReport.put("cust_royal", rp.getCustomerRoyalty());  //&??
//            jsonReport.put("content_type", rp.getContentType());
//            jsonReport.put("vol", rp.getVol());
//            jsonReport.put("artist", rp.getArtist());
//            jsonReport.put("price", rp.getPrice());
//            jsonReport.put("revenue", rp.getRevenue());
//            jsonReport.put("catalog", rp.getCatalog());
//            jsonReport.put("cat_royal", rp.getCatalogRoyalty());
//            jsonReport.put("composer", rp.getComposer());
//            jsonReport.put("code", rp.getCompositionCode());
//            jsonReport.put("name", rp.getCompositionName());
//            jsonReport.put("copyright", rp.getCopyright());
//            jsonReport.put("share_mobile", rp.getShareMobile());
//            jsonReport.put("share_public", rp.getSharePublic());
//            jsonReport.put("report_id", rp.getReportItemId());
//
//            mainArray.add(jsonReport);
//        }
//        return mainArray;
//    }


}

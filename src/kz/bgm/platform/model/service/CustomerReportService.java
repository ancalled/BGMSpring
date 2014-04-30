package kz.bgm.platform.model.service;


import kz.bgm.platform.model.domain.CalculatedReportItem;
import kz.bgm.platform.model.domain.CustomerReport;
import kz.bgm.platform.model.domain.CustomerReportItem;

import java.util.Date;
import java.util.List;

public interface CustomerReportService {




    long saveCustomerReport(CustomerReport report);


    long updtDetectedTracksInCustomerReport(long id, int detected);

    long updtTracksInCustomerReport(long id, int tracks);

    long saveCustomerReportItem(CustomerReportItem item);

    long getUpdateCatalogQueryId();

//    String getQueryProcessTime(long processId);

    void saveCustomerReportItems(List<CustomerReportItem> reportItemList);

    CustomerReport getCustomerReport(long id);


    List<CustomerReport> getAllCustomerReports(Date later);

    List<CustomerReport> getCustomerReports(long customerId, Date from, Date to);

    CustomerReportItem getCustomerReportsItem(long id);

    boolean acceptReport(long reportId);

    List<CustomerReportItem> getCustomerReportsItems(long reportId);

    List<CustomerReportItem> getCustomerReportsItems(long reportId, int from, int size);

    List<CalculatedReportItem> calculatePublicReport(String platform, Date from, Date to);

    List<CalculatedReportItem> calculateMobileReport(String platform, Date from, Date to);
}

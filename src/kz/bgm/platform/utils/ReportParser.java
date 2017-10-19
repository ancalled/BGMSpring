package kz.bgm.platform.utils;

import kz.bgm.platform.model.domain.CustomerReportItem;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class ReportParser {

    private static final Logger log = Logger.getLogger(ReportParser.class);
    public static final int FIRST_SHEET = 0;
    public static final int COL_NAME = 2;
    public static final int COL_ARTIST = 3;
    public static final int COL_AUTHOR = 4;

    //    public static List<CustomerReportItem> parseNotForBaseReports(String fileName)
//            throws IOException, InvalidFormatException {
//
//        log.info("Parsing mobile report from: " + fileName + "... ");
//        Workbook wb = ExcelUtils.openFile(new File(fileName));
//
//        List<CustomerReportItem> items = new ArrayList<>();
//        Sheet sheet = wb.getSheetAt(FIRST_SHEET);
//        int rows = sheet.getPhysicalNumberOfRows();
//
//        //todo use templates instead of hardcoded cell numbers
//        int startRow = 7;
//        for (int k = startRow; k < rows; k++) {
//            Row row = sheet.getRow(k);
//            String num = ExcelUtils.getCellVal(row, 0);
//
//            if (num == null || "".equals(num.trim())) continue;
//
//            CustomerReportItem item = new CustomerReportItem();
//
//            String name = ExcelUtils.getCellVal(row, COL_NAME);
//            String artist = ExcelUtils.getCellVal(row, COL_ARTIST);
//            String auth = ExcelUtils.getCellVal(row, COL_AUTHOR);
//
//            item.setTrack(name);
//            item.setArtist(artist);
//            item.setAuthors(auth);
//
//            String priceStr = ExcelUtils.getCellVal(row, 5);
//
//            if ("".equals(priceStr.trim())) {
//                item.setPrice(0F);
//            } else {
//                item.setPrice(Float.parseFloat(priceStr.replace("\\.", ",").trim()));
//            }
//
//            String strQty = ExcelUtils.getCellVal(row, 6);
//
//            if ("".equals(strQty.trim())) {
//                item.setQty(0);
//            } else {
//                item.setQty(Integer.parseInt(strQty.replace("$,", "").replace("\\.", ",").trim()));
//            }
//            items.add(item);
//        }
////        }
//        return items;
//    }
//
    public static List<CustomerReportItem> parseMobileReport(String fileName)
            throws IOException/*, InvalidFormatException*/ {
//
//        log.info("Parsing mobile report from: " + fileName + "... ");
//
//        Workbook wb = ExcelUtils.openFile(new File(fileName));
//        Sheet sheet = wb.getSheetAt(1);
//
//        List<CustomerReportItem> items = new ArrayList<>();
//
//        //todo use templates instead of hardcoded cell numbers
//        int rows = sheet.getPhysicalNumberOfRows();
//        int startRow = 6;
//        int number = 1;
//        for (int i = startRow; i < rows; i++) {
//            try {
//                Row row = sheet.getRow(i);
//
//                String name = ExcelUtils.getCellVal(row, 1);
//
//                if (name == null || "".equals(name.trim())) continue;
//
//                CustomerReportItem item = new CustomerReportItem();
//                item.setNumber(number);
//                item.setTrack(name);
//                item.setArtist(ExcelUtils.getCellVal(row, 2));
//                item.setContentType(ExcelUtils.getCellVal(row, 3));
//                item.setQty(Integer.parseInt(ExcelUtils.getCellVal(row, 4).trim()));
//                item.setPrice(Float.parseFloat(ExcelUtils.getCellVal(row, 7).trim()));
//                items.add(item);
//                number++;
//
//            } catch (Exception e) {
//                log.warn("Got exception: " + e.getMessage());
//            }
//        }
//        log.info("Mobile report parsed done, tracks count: " + items.size());
//        return items;

        return null;
    }
//
//
//    public static List<CustomerReportItem> parsePublicReport(String fileName)
//            throws IOException, InvalidFormatException {
//
//        log.info("Parsing public report from: " + fileName + "... ");
//
//        Workbook wb = ExcelUtils.openFile(new File(fileName));
//
//        List<CustomerReportItem> items = new ArrayList<>();
//
//        Sheet sheet = wb.getSheetAt(0);
//        int rows = sheet.getPhysicalNumberOfRows();
//
//        int startRow = 0;
//        int number = 1;
//        for (int i = startRow; i < rows; i++) {
//            Row row = sheet.getRow(i);
//
//            CustomerReportItem item = new CustomerReportItem();
//            String artist = ExcelUtils.getCellVal(row, 0);
//            String name = ExcelUtils.getCellVal(row, 1);
//            String qrtStr = ExcelUtils.getCellVal(row, 2);
//            int qty = Integer.parseInt(qrtStr.trim());
//
//            if (qty == 0) continue;
//
//            item.setArtist(artist);
//            item.setTrack(name);
//            item.setQty(qty);
//            item.setNumber(number);
//            items.add(item);
//            number++;
//        }
//
//        return items;
//    }
//
//
//    public static void main(String[] args) throws IOException, InvalidFormatException {
//        String filepath = "/home/ancalled/Documents/tmp/25/bgm/mul_lab/april.xls";
//        ReportParser.parseMobileReport(filepath);
//    }

    private static int trackCol;
    private static int artistCol;
    private static int contentCol;
    private static int qtyCol;
    private static int priceCol;

    private static boolean columnsFound = false;

    private static CustomerReportItem parseFromLine(String l, String sep) {
        String[] split = l.split(sep);
        if (split.length < 6) return null;
        for (String s : split) {
            if (s.isEmpty()) return null;
        }

        CustomerReportItem item = new CustomerReportItem();
//            item.setNumber(Integer.parseInt(split[0]));

        if (!columnsFound) {
            trackCol = IntStream.range(0, split.length).filter(i -> "Название Произведения".equals(split[i]))
                    .findFirst().orElse(0);
            artistCol = IntStream.range(0, split.length)
                    .filter(i -> "Исполнитель Произведения".equals(split[i]))
                    .findFirst().orElse(0);
            contentCol = IntStream.range(0, split.length)
                    .filter(i -> "Название Сервиса".equals(split[i]))
                    .findFirst().orElse(0);
            qtyCol = IntStream.range(0, split.length)
                    .filter(i -> "Количество Запросов".equals(split[i]))
                    .findFirst().orElse(0);
            priceCol = IntStream.range(0, split.length)
                    .filter(i -> "Стоимость подписки для Абонентов без НДС".equals(split[i]))
                    .findFirst().orElse(0);


            if (trackCol == 0 && artistCol == 0 &&
                    contentCol == 0 && qtyCol == 0 && priceCol == 0) {
                System.out.println("Some columns was not found");
                return null;
            } else {
                columnsFound = true;
                return null;
            }
        }


        item.setTrack(split[trackCol]);//Название Произведения
        item.setArtist(split[artistCol]);//Исполнитель Произведения
        item.setContentType(split[contentCol]);//Название Сервиса
        item.setQty(Integer.parseInt(split[qtyCol]));//Количество Запросов
        item.setPrice(Float.parseFloat(split[priceCol]));//Стоимость подписки для Абонентов без НДС
        return item;
    }


    public static List<CustomerReportItem> parseItemsFromCsv(String fileName, String sep) throws IOException {
        return Files.readAllLines(Paths.get(fileName)).stream()
                .map(l -> parseFromLine(l, sep))
                .filter(i -> i != null)
                .collect(Collectors.toList());
    }

}

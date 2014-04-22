package kz.bgm.platform.utils;


import kz.bgm.platform.model.domain.SearchResultItem;
import kz.bgm.platform.model.domain.Track;
import kz.bgm.platform.model.service.MainService;
import kz.bgm.platform.model.service.SearchService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BulkSearchUtil {

    public static final String APP_DIR = System.getProperty("user.dir");
    public static final String INDEX_DIR = APP_DIR + "/lucen-indexes";

    public static final String BASE_NAME = "base.name";
    public static final String BASE_LOGIN = "base.login";
    public static final String BASE_PASS = "base.pass";
    public static final String BASE_HOST = "base.host";
    public static final String BASE_PORT = "base.port";

    public static final int RESULT_LIMIT = 3;
    public static final float RESULT_MIN_SCORE = 1.0f;
    public static final String IN_FIELD_SEP = ",";
    public static final String OUT_FIELD_SEP = ";";

    public static final String[] columnNames = new String[]{"Code",
            "Catalog",
            "Mobile Share"};


    private static final HSSFColor[] excelColors = new HSSFColor[]{
            new HSSFColor.LIGHT_BLUE(),
            new HSSFColor.LIGHT_GREEN(),
            new HSSFColor.LIGHT_ORANGE()

    };

    private final MainService mainService;
    private final SearchService searchService;

    private static boolean debug;

    public BulkSearchUtil(MainService mainService, SearchService searchService) {
//        try {
//            initDatabase("db.properties");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        this.mainService = mainService;
        this.searchService = searchService;
    }


    public void search(String artist, String authors, String track) throws IOException, ParseException {
        List<SearchResultItem> res = searchService.search(artist, authors, track, 100);

        for (SearchResultItem r : res) {
            System.out.println("[" + r.getScore() + "] id: " + r.getTrackId());
            Track t = mainService.getTrack(r.getTrackId());
            if (t != null && debug) {
                System.out.println("\tartist: '" + t.getArtist() + "'" +
                                "\n\ttrack: '" + t.getName() + "'" +
                                "\n\tcomposer: '" + t.getComposer() + "'" +
                                "\n\tcatalog: '" + t.getCatalog() + "'" +
                                "\n\tcode: '" + t.getCode() + "'" +
                                "\n\tmobileShare: '" + t.getMobileShare() + "'"
                );
                System.out.println();
            }
        }
    }

   //todo доделать выгрузку статистики по найденным трекам на страничку массового поиска
    public void bulkSearch(String infile, String outfile, boolean withHeader, int artistRow, int trackRow)
            throws IOException {
        if (infile == null || outfile == null || artistRow < 0 || trackRow < 0) return;

        List<String> lines = readFilesByLines(Paths.get(infile));

        HSSFWorkbook workbook = new HSSFWorkbook();

        HSSFSheet firstSheet = workbook.createSheet("Result");
        int idx = 0;
        for (String line : lines) {
            HSSFRow row = firstSheet.createRow(idx);
            String[] sourceFields = line.split(IN_FIELD_SEP);

            if (sourceFields.length <= trackRow || sourceFields.length <= artistRow) {
                continue;
            }

            String track = sourceFields[trackRow];
            String artist = sourceFields[artistRow];

            List<SearchResultItem> res = null;
            if (idx > 0 || !withHeader) {
                try {
                    res = searchService.search(artist, artist, track, RESULT_LIMIT * 3);
                } catch (ParseException e) {
                    System.err.println(e.getMessage());
                }
            }

            if (withHeader && idx == 0) {
                toRow(row, sourceFields);
                int k = 0;
                while (RESULT_LIMIT - k != 0) {
                    toRowFoundData(row,
                            columnNames
                    );
                    k++;
                }

            } else {
                toRowWrapped(row, sourceFields);
            }

            if (debug) {
                System.out.println(artist + ": " + track);
            }

            if (res != null && res.size() != 0) {
                int colorSize = excelColors.length;
                int currColorIdx = 0;

                for (SearchResultItem r : filterByHighScore(res, RESULT_LIMIT)) {

                    if (r.getScore() < RESULT_MIN_SCORE) continue;

                    if (currColorIdx >= colorSize) {
                        currColorIdx = 0;
                    }

                    Track t = mainService.getTrack(r.getTrackId());
                    if (t != null) {
                        //                        buf.append(toRowWrapped(
                        //                                t.getArtist() + ": " + t.getName(),
                        //                                t.getCode(),
                        //                                t.getMobileShare(),
                        //                                t.getPublicShare(),
                        //                                t.getCatalog()));
                        toRowFoundDataWithColor(row, workbook,
                                excelColors[currColorIdx++],
                                t.getCode(),
                                t.getCatalog(),
                                t.getMobileShare());

                        if (debug) {
                            System.out.println("[" + r.getScore() + "] id: " + r.getTrackId());
                            System.out.println("\tartist: '" + t.getArtist() + "'" +
                                            "\n\ttrack: '" + t.getName() + "'" +
                                            "\n\tcomposer: '" + t.getComposer() + "'" +
                                            "\n\tcatalog: '" + t.getCatalog() + "'" +
                                            "\n\tcode: '" + t.getCode() + "'" +
                                            "\n\tmobileShare: '" + t.getMobileShare() + "'"
                            );

                            System.out.println();
                        }
                    }
                }
            }
            if (debug) {
                System.out.println("----------------------------------------------------");
            }

            idx++;
        }

        for (int colNum = 0; colNum < 20; colNum++) {
            workbook.getSheetAt(0).autoSizeColumn(colNum);
        }

        writeToFile(workbook, outfile);

    }


    private static void fillCellBkgrd(HSSFWorkbook wb, HSSFCell cell, HSSFColor color) {
        HSSFCellStyle style = wb.createCellStyle();
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(color.getIndex());
        cell.setCellStyle(style);
    }

    public static void toRowFoundData(HSSFRow row,
                                      Object... fields) {
        int lastColNum = row.getLastCellNum();
        for (int i = 0; i < fields.length; i++) {
            String val;
            if (fields[i] instanceof String) {
                val = wrap((String) fields[i]);
            } else if (fields[i] instanceof Number) {
                val = fields[i].toString();
            } else {
                val = "";
            }
            HSSFCell cell = row.createCell(lastColNum++);
            cell.setCellValue(val);
        }
    }

    public static void toRowFoundDataWithColor(HSSFRow row,
                                               HSSFWorkbook wb, HSSFColor color,
                                               Object... fields) {
        int lastColNum = row.getLastCellNum();

        for (Object field : fields) {
            String val;
            if (field instanceof String) {
                val = wrap((String) field);
            } else if (field instanceof Number) {
                val = field.toString();
            } else {
                val = "";
            }
            HSSFCell cell = row.createCell(lastColNum++);
            fillCellBkgrd(wb, cell, color);
            cell.setCellValue(val);
        }
    }


    public void toRow(HSSFRow row, String... fields) {
        for (int i = 0; i < fields.length; i++) {
            row.createCell(i).setCellValue(fields[i]);
        }

    }


//    public static void initDatabase(String propsFile) throws IOException {
//        Properties props = new Properties();
//        props.load(new FileInputStream(propsFile));
//
//        String dbHost = props.getProperty(BASE_HOST);
//        String dbPort = props.getProperty(BASE_PORT);
//        String dbName = props.getProperty(BASE_NAME);
//        String dbLogin = props.getProperty(BASE_LOGIN);
//        String dbPass = props.getProperty(BASE_PASS);
//
//        System.out.println("Initializing data storage...");
//        CatalogFactory.initDBStorage(dbHost, dbPort, dbName, dbLogin, dbPass);
//    }
//

    public static List<String> readFilesByLines(Path path) throws IOException {

        List<String> res = new ArrayList<>();
        BufferedReader reader =
                Files.newBufferedReader(path, Charset.forName("utf-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            res.add(line);
        }
        return res;
    }


    public static List<SearchResultItem> filterByHighScore(List<SearchResultItem> results, int limit) {
        if (results == null) return null;
        if (results.isEmpty()) return Collections.emptyList();

        List<SearchResultItem> filtered = new ArrayList<>();
        SearchResultItem first = results.get(0);

        filtered.add(first);

        for (int i = 1; i < Math.min(results.size(), limit); i++) {
            SearchResultItem sr = results.get(i);
            if (sr.getScore() < first.getScore()) {
                break;
            }
            filtered.add(sr);
        }
        return filtered;
    }

    public static void writeToFile(HSSFWorkbook workbook, String outFile) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(outFile));
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeToFile(String oufile, String content) throws IOException {

        BufferedWriter writer = Files.newBufferedWriter(Paths.get(oufile), Charset.forName("utf-8"));
        writer.append(content);
        writer.flush();
    }

    public static String wrap(String value) {
        return "'" + value.replace("'", "").replace(OUT_FIELD_SEP, "") + "'";
    }

    public static String toRow(String... fields) {
        StringBuilder buf = new StringBuilder();
        for (String field : fields) {
            buf.append(field).append(OUT_FIELD_SEP);
        }
        return buf.toString();
    }

    public static String toRowWrapped(java.lang.Object... fields) {
        StringBuilder buf = new StringBuilder();
        for (Object field : fields) {
            String val;
            if (field instanceof String) {
                val = wrap((String) field);
            } else if (field instanceof Number) {
                val = field.toString();
            } else {
                val = "";
            }

            buf.append(val).append(OUT_FIELD_SEP);
        }
        return buf.toString();
    }


//    public static void main(String[] args) throws IOException {
//        if (args.length < 2) {
//            System.err.println("Not enough params!");
//            System.out.println("Expected: $IN_FILE $OUT_FILE");
//            return;
//        }
//
//        String infile = args[0];
//        String outfile = args[1];
//        boolean withHeader = true;
//        int artistRow = 2;
//        int trackRow = 1;
//
//
//        BulkSearchUtil util = new BulkSearchUtil();
//        util.bulkSearch(infile, outfile, withHeader, artistRow, trackRow);
//
//    }
}

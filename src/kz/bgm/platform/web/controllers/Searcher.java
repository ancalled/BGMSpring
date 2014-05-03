package kz.bgm.platform.web.controllers;

import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.MainService;
import kz.bgm.platform.model.service.SearchService;
import kz.bgm.platform.utils.BulkSearchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/search")
public class Searcher {

    private static final Logger log = Logger.getLogger("Searcher");

    public static final int LIMIT = 100;


    @Autowired
    private SearchService searchService;

    @Autowired
    private MainService mainService;

    private final AtomicInteger requesCounter = new AtomicInteger(1000000);

//    public BulkSearchUtil bulkUtil = new BulkSearchUtil(mainService, searchService);


    public static final String APP_HOME = System.getProperty("user.dir");
    public static final String FILE_SEARCHES_HOME = APP_HOME + "/file-searches";

    public static final int RESULT_LIMIT = 3;
    public static final float RESULT_MIN_SCORE = 1.0f;


    @RequestMapping(value = "/bulk-search", method = RequestMethod.POST)
    public String massSearch(Model model,
                             @RequestParam(value = "file", required = true) MultipartFile file) throws IOException {
        String searchFileName = FILE_SEARCHES_HOME + "/" + file.getOriginalFilename();
        File searchFile = new File(searchFileName);

        try {
            saveToFile(file, searchFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

//        String encoding = "utf-8";
//        String fieldSeparator = ",";
        int artistRow = 2;
        int trackRow = 1;
        BulkSearchUtil searcher = new BulkSearchUtil(mainService, searchService);
        String outFilePath = FILE_SEARCHES_HOME + "/result_" + file.getOriginalFilename();

        int size = searcher.bulkSearch(searchFile.getPath(), outFilePath, true, artistRow, trackRow);
        model.addAttribute("result", new File(outFilePath).toURI().getPath());
        model.addAttribute("result_size", size);

        return "search/mass-search";

    }

//    public List<Track> bulkSearch(String infile,
//                                  String encoding,
//                                  boolean withHeader,
//                                  int artistRow,
//                                  int trackRow,
//                                  String fieldSeparator)
//            throws IOException {
//
//        if (infile == null || artistRow < 0 || trackRow < 0) return null;
//
//
//        List<String> lines = Files.readAllLines(Paths.get(infile), Charset.forName(encoding));
//        List<Track> tracks = new ArrayList<>();
//
//        int idx = 0;
//        for (String line : lines) {
//
//            String[] sourceFields = line.split(fieldSeparator);
//            if (sourceFields.length <= trackRow || sourceFields.length <= artistRow) {
//                continue;
//            }
//
//
//            String track = sourceFields[trackRow];
//            String artist = sourceFields[artistRow];
//
//            List<SearchResultItem> res = null;
//            if (idx > 0 || !withHeader) {
//                try {
//                    res = searchService.search(artist, artist, track, RESULT_LIMIT * 3);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//
//
//            if (res != null) {
//                for (SearchResultItem r : filterByHighScore(res, RESULT_LIMIT)) {
//                    if (r.getScore() < RESULT_MIN_SCORE) continue;
//
//                    Track t = mainService.getTrack(r.getTrackId());
//                    if (t != null) {
//                        tracks.add(t);
//                    }
//                }
//            }
//            idx++;
//        }
//
//        return tracks;
//    }

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


    private void saveToFile(MultipartFile multipartFile, File file) throws IOException {
        File searchFileDir = new File(FILE_SEARCHES_HOME);

        if (searchFileDir.exists()) {
            searchFileDir.mkdirs();
        }
        Path path = Paths.get(file.getAbsolutePath());
        log.info("Saving upload to " + file.getAbsolutePath());
        Files.write(path, multipartFile.getBytes());
        log.info("File saved");
    }


    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String showSearch(Model model,
                             @RequestParam(value = "rid", required = false) Long resultId,
                             HttpSession ses) {


        List<Platform> platforms = mainService.getPlatformsWithCatalogs();

        model.addAttribute("platforms", platforms);

        if (resultId != null) {
            SearchResult sr = (SearchResult) ses.getAttribute("search-result-" + resultId);
            if (sr != null && sr.getTracks() != null && !sr.getTracks().isEmpty()) {

                double bestScore = sr.getTracks().stream()
                        .mapToDouble(SearchResultItem::getScore)
                        .max().getAsDouble();


                sr.getTracks().stream()
                        .collect(Collectors.groupingBy(i -> i.getTrack().getArtist() + ";" +
                                i.getTrack().getName() + ";" +
                                i.getTrack().getCatalog() + ";" +
                                i.getTrack().getMobileShare() + ";" +
                                i.getTrack().getPublicShare()))
                        .forEach((s, rl) -> {

                            SearchResultItem i = rl.get(0);

                            if (i.getScore() > bestScore * 0.33) {

                                String codes = rl.stream().map(r -> r.getTrack().getCode()).collect(Collectors.joining(", "));
                                i.getTrack().setCode(codes);

                                SearchResultGroup group = new SearchResultGroup(i);
//                            SearchResultGroup group = new SearchResultGroup(rl);
                                sr.getGroups().add(group);
                            }

                        });

                sr.getGroups().sort((g1, g2) -> {

                    int c = Double.compare(g2.getScore(), g1.getScore());
                    if (c == 0) {
                        float sh2 = g2.getTracks().get(0).getTrack().getMobileShare();
                        float sh1 = g1.getTracks().get(0).getTrack().getMobileShare();
                        c = Float.compare(sh2, sh1);
                    }
                    if (c == 0) {
                        float sh2 = g2.getTracks().get(0).getTrack().getPublicShare();
                        float sh1 = g1.getTracks().get(0).getTrack().getPublicShare();
                        c = Float.compare(sh2, sh1);
                    }
                    return c;
                });

                model.addAttribute("bestScore", bestScore);

            }


            model.addAttribute("result", sr);
        }

////      model.addAttribute("from", from);
//        model.addAttribute("pageSize", TRACKS_PER_PAGE);

        return "search/search";
    }


    @RequestMapping(value = "/do-search", method = RequestMethod.POST)
    public String doSearch(HttpServletRequest req,
                           @RequestParam(value = "q", required = true) String query,
                           @RequestParam(value = "type", required = false, defaultValue = "ALL") SearchType searchType,
                           @RequestParam(value = "catalogs", required = false) Long[] reqCatalogs

    ) throws Exception {

        req.setCharacterEncoding("UTF-8");

        int requestId = requesCounter.incrementAndGet();

        log.info("\nGot search request:\n" +
                        "\tid: " + requestId + "\n" +
                        "\tquery: '" + query + "'\n" +
                        "\ttype: '" + searchType + "'\n" +
                        "\tcatalogs: '" + (reqCatalogs != null ? Arrays.toString(reqCatalogs) : "none") + "'\n"
        );

        int limit = LIMIT;

        List<Long> catalogs = new ArrayList<>();
        if (reqCatalogs == null) {
            List<Long> available = mainService.getAllCatalogIds();
            catalogs.addAll(available);
        } else {
            catalogs.addAll(Arrays.asList(reqCatalogs));
        }


        SearchResult result = new SearchResult(requestId,
                query, searchType, catalogs);

        List<SearchResultItem> tracks = Collections.emptyList();
        String first = null, second = null;
        if (query.contains(";")) {
            String[] fields = query.split(";");
            first = fields[0].trim();
            second = fields[1].trim();
        }

        try {

            switch (searchType) {
                case ALL:
                    tracks = searchService.getTracks(
                            searchService.search(query, limit),
                            catalogs);
                    break;

                case CODE:
                    tracks = searchService.searchTracksByCode(query, catalogs);
                    break;

                case TRACK:
                    tracks = searchService.getTracks(
                            searchService.search(null, null, query, limit),
                            catalogs);
                    break;

                case ARTIST:
                    tracks = searchService.getTracks(
                            searchService.search(query, null, null, limit),
                            catalogs);
                    break;

                case COMPOSER:
                    tracks = searchService.getTracks(
                            searchService.search(null, query, null, limit),
                            catalogs);
                    break;

                case ARTIST_TRACK:
                    tracks = searchService.getTracks(
                            searchService.search(first, null, second, limit),
                            catalogs);
                    break;

                case COMPOSER_TRACK:
                    tracks = searchService.getTracks(
                            searchService.search(null, first, second, limit),
                            catalogs);
                    break;


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpSession session = req.getSession();

        if (tracks != null) {
            log.info("\nFound " + tracks.size() + " tracks");
            tracks.sort((o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
            result.setTracks(tracks);
        } else {
            log.info("\nNot tracks found!");
        }

        session.setAttribute("search-result-" + result.getId(), result);

        return "redirect:search?q=" + URLEncoder.encode(query, "UTF-8") + "&rid=" + result.getId();
    }


}

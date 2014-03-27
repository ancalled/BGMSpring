package kz.bgm.platform.web.controllers;

import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.MainService;
import kz.bgm.platform.model.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

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


    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String showSearch(Model model,
                             @RequestParam(value = "rid", required = false) Long resultId,
                             HttpSession ses) {


        List<Platform> platforms = mainService.getPlatformsWithCatalogs();

        model.addAttribute("platforms", platforms);

        if (resultId != null) {
            SearchResult searchResult = (SearchResult) ses.getAttribute("search-result-" + resultId);
            model.addAttribute("result", searchResult);
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

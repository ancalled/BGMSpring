package kz.bgm.platform.web.controllers;

import kz.bgm.platform.model.domain.Platform;
import kz.bgm.platform.model.domain.SearchResult;
import kz.bgm.platform.model.domain.SearchType;
import kz.bgm.platform.model.domain.Track;
import kz.bgm.platform.model.service.CatalogStorage;
import kz.bgm.platform.model.service.LuceneSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Controller
@RequestMapping(value = "/search")
public class Searcher {

    public static final int LIMIT = 100;


    @Autowired
    private CatalogStorage dbService;

    @Autowired
    private LuceneSearch luceneService;

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String showSearch(Model model, HttpSession ses) {
        Collection<Platform> platforms = dbService.getPlatforms();

        model.addAttribute("platforms", platforms);
        model.addAttribute("query", ses.getAttribute("query"));
        model.addAttribute("tracks", ses.getAttribute("tracks"));
////                        model.addAttribute("from", from);
//        model.addAttribute("pageSize", TRACKS_PER_PAGE);

        return "search/search";
    }


    @RequestMapping(value = "/api/search", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public List<Track> showAdminApiSearchJson(
            @RequestParam(value = "find", required = true) String find
    ) {

//        JSONArray mass = new JSONArray();
//        if (foundTracks == null) {
//            resp.sendRedirect("/find.html");
//            return;
//        }
//        for (Track t : foundTracks) {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("id", t.getId());
//            jsonObject.put("code", t.getCode());
//            jsonObject.put("composition", t.getName());
//            jsonObject.put("artist", t.getArtist());
//            jsonObject.put("authors", t.getComposer());
//            jsonObject.put("mobile_share", (double) t.getMobileShare());
//            jsonObject.put("public_share", (double) t.getPublicShare());
//
//            mass.add(jsonObject);
//        }

        return dbService.searchTracksByName(find);
    }

    @RequestMapping(value = "/do-search", method = RequestMethod.POST)
    public String doSearch(HttpServletRequest req,
                           HttpServletResponse resp,
                           @RequestParam(value = "q", required = true) String query,
                           @RequestParam(value = "field", required = false, defaultValue = "all") String fieldStr
    )
            throws Exception {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");


        SearchType searchType = SearchType.ALL;
        if (fieldStr != null) {
            try {
                searchType = SearchType.valueOf(fieldStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        int limit = LIMIT;

        List<Long> requested = getCatalogsId(req);
        List<Long> catalogs = new ArrayList<>();
        if (requested.isEmpty()) {
            List<Long> available = dbService.getAllCatalogIds();
            catalogs.addAll(available);
        } else {
            catalogs.addAll(requested);
        }


        List<SearchResult> result = Collections.emptyList();
        String first = null, second = null;
        if (query.contains(";")) {
            String[] fields = query.split(";");
            first = fields[0].trim();
            second = fields[1].trim();
        }

        try {

            switch (searchType) {
                case ALL:
                    result = dbService.getTracks(
                            luceneService.search(query, limit),
                            catalogs);
                    break;

                case CODE:
                    result = dbService.searchTracksByCode(query, catalogs);
                    break;

                case TRACK:
                    result = dbService.getTracks(
                            luceneService.search(null, null, query, limit),
                            catalogs);
                    break;

                case ARTIST:
                    result = dbService.getTracks(
                            luceneService.search(query, null, null, limit),
                            catalogs);
                    break;

                case COMPOSER:
                    result = dbService.getTracks(
                            luceneService.search(null, query, null, limit),
                            catalogs);
                    break;

                case ARTIST_TRACK:
                    result = dbService.getTracks(
                            luceneService.search(first, null, second, limit),
                            catalogs);
                    break;

                case COMPOSER_TRACK:
                    result = dbService.getTracks(
                            luceneService.search(null, first, second, limit),
                            catalogs);
                    break;


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpSession session = req.getSession();

        if (result != null) {
            Collections.sort(result, (o1, o2) -> Double.compare(o2.getScore(), o1.getScore()));
        }
        session.setAttribute("tracks", result);
        session.setAttribute("query", query);
        session.setAttribute("searchType", searchType);

        resp.sendRedirect(buildResponseUrl(req));

        return "redirect:search/search";
    }


    private String buildResponseUrl(HttpServletRequest req) {

        StringBuilder buf = new StringBuilder();
        buf.append("/admin/view/search");

        Map<String, String[]> params = req.getParameterMap();
        if (!params.isEmpty()) {
            buf.append("?");
            int i = 0;
            for (String key : params.keySet()) {

                buf.append(key);
                buf.append("=");
                String value = params.get(key)[0];
                if ("q".equals(key)) {
                    try {
                        buf.append(URLEncoder.encode(value, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    buf.append(value);
                }
                if (i++ < params.size() - 1) {
                    buf.append("&");
                }
            }

        }
        return buf.toString();
    }


    private List<Long> getCatalogsId(HttpServletRequest req) {
        Enumeration<String> paramNames = req.getParameterNames();
        List<Long> ids = new ArrayList<>();

        while (paramNames.hasMoreElements()) {
            String param = paramNames.nextElement();

            if (param.contains("catalog")) {
                String strCatId = req.getParameter(param);
                Long id = Long.parseLong(strCatId);
                if (id == -1) {
                    continue;
                }
                ids.add(id);
            }
        }

        return ids;

    }


}

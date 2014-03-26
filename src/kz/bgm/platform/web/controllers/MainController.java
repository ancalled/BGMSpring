package kz.bgm.platform.web.controllers;

import kz.bgm.platform.model.domain.Catalog;
import kz.bgm.platform.model.domain.CatalogUpdate;
import kz.bgm.platform.model.domain.Platform;
import kz.bgm.platform.model.domain.Track;
import kz.bgm.platform.model.service.CatalogStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@Controller
@RequestMapping(value = "/main")
public class MainController {


    @Autowired
    private CatalogStorage dbService;


    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String showIndex(Model model) {
        List<Platform> platforms = dbService.getPlatforms();
        List<Catalog> catalogs = dbService.getCatalogs();

        Map<Long, List<Catalog>> catMap =
                catalogs.stream()
                        .collect(groupingBy(Catalog::getPlatformId,
                                mapping(c -> c, toList())));

        platforms.stream()
                .forEach(p -> p.setCatalogs(catMap.get(p.getId())));

        int totalTracks =
                platforms.stream()
                        .mapToInt(p ->
                                p.getCatalogs()
                                        .stream()
                                        .mapToInt(Catalog::getTracks)
                                        .sum())
                        .sum();

        model.addAttribute("platforms", platforms);
        model.addAttribute("totalTracks", totalTracks);

        return "index";
    }


    @RequestMapping(value = "/catalog", method = RequestMethod.GET)
    public String showCatalog(Model model,
                              @RequestParam(value = "catId", required = true) long catId
    ) {

        Catalog catalog = dbService.getCatalog(catId);
        model.addAttribute("catalog", catalog);

        List<CatalogUpdate> updates = dbService.getAllCatalogUpdates(catId);
        model.addAttribute("updates", updates);

        return "catalog";
    }


    @RequestMapping(value = "download-catalog", method = RequestMethod.POST)
    public void downloadCatalog() {


    }


    @RequestMapping(value = "get-random-tracks", method = RequestMethod.GET/*, produces = "application/json"*/)
    @ResponseBody
    public List<Track> getRandomTracks(
            @RequestParam(value = "n", required = false, defaultValue = "10") int num,
            @RequestParam(value = "catId", required = false) Long catId
    ) {
        if (catId != null) {
            return dbService.getRandomTracks(catId, num);
        } else {
            return dbService.getRandomTracks(num);
        }
    }


}
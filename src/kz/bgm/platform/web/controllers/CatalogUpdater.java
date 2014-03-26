package kz.bgm.platform.web.controllers;

import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.CatalogStorage;
import kz.bgm.platform.utils.LuceneIndexRebuildUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping(value = "/catalog-update")
public class CatalogUpdater {

    private static final Logger log = Logger.getLogger(CatalogUpdater.class);


    public static final int TRACKS_PER_PAGE = 50;

    public static final String APP_HOME = System.getProperty("user.dir");
    public static final String CATALOG_UPDATES_HOME = APP_HOME + "/catalog-updates";

    public static final String DEFAULT_ENCODING = "utf8";
    public static final String DEFAULT_FIELD_SEPARATOR = ";";
    public static final String DEFAULT_ENCLOSED_BY = "\"";
    public static final String DEFAULT_NEWLINE = "\n";
    public static final int DEFAULT_FROM_LINE = 1;


    @Autowired
    private CatalogStorage dbService;


    @RequestMapping(value = "/update-catalog",
            method = RequestMethod.POST,
            produces = "application/json")
    @ResponseBody
    public UpdateResult updateCatalog(
            @RequestParam(value = "catId", required = true) Long catalogId,
            @RequestParam(value = "file", required = true) MultipartFile file,
            @RequestParam(value = "enc", defaultValue = DEFAULT_ENCODING) String encoding,
            @RequestParam(value = "fs", defaultValue = DEFAULT_FIELD_SEPARATOR) String fieldSeparator,
            @RequestParam(value = "eb", defaultValue = DEFAULT_ENCLOSED_BY) String enclosedBy,
            @RequestParam(value = "nl", defaultValue = DEFAULT_NEWLINE) String newline,
            @RequestParam(value = "fl", defaultValue = "" + DEFAULT_FROM_LINE) int frimLine
    ) {

        try {

            String updateFilePath = CATALOG_UPDATES_HOME + "/" + file.getName();
            if (updateFilePath.isEmpty()) {
                updateFilePath = "tmp_file_" + new Random().nextInt(100000);
            }

            Path path = Paths.get(updateFilePath);
            Files.write(path, file.getBytes());

            CatalogUpdate update = new CatalogUpdate();
            update.setCatalogId(catalogId);
            update.setFilePath(path.toString());
            update.setFileName(file.getName());
            update.setEncoding(encoding);
            update.setSeparator(fieldSeparator);
            update.setEnclosedBy(enclosedBy);
            update.setNewline(newline);
            update.setFromLine(frimLine);

            log.info("Got catalog updates " + file.getName());

            update = dbService.saveCatalogUpdate(update);
            doUpdate(update);

            return new UpdateResult("ok", null, update.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return new UpdateResult("error", e.getMessage(), null);
        }
    }

    @Async
    private void doUpdate(CatalogUpdate update) {

        //todo
        log.debug("Starting update catalog...");
//        changeStatus(UpdateStatus.FILE_UPLOADED);
        CatalogUpdate updateResult = dbService.importCatalogUpdate(update);

        log.debug("Load complete, calc stats...");
//        changeStatus(UpdateStatus.SQL_LOAD_COMPLETE);
        dbService.calculateCatalogUpdateStats(update.getId(), updateResult.getStatus());

        log.debug("Stat calc complete.");
//        changeStatus(UpdateStatus.UPDATE_STATISTICS_FINISHED);
    }


    @RequestMapping(value = "check-update-status", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public UpdateResult checkUpdateResult(
            HttpSession ses,
            @RequestParam(value = "uid", required = true) long updateId
    ) {

        CatalogUpdate catUpdate = (CatalogUpdate) ses.getAttribute("catalog-update-" + updateId);
         //todo
        return null;
    }



    @RequestMapping(value = "check-apply-status", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public UpdateResult checkApplyResult(
            HttpSession ses,
            @RequestParam(value = "uid", required = true) long updateId
    ) {

        CatalogUpdate catUpdate = (CatalogUpdate) ses.getAttribute("catalog-update-" + updateId);
        //todo
        return null;
    }





    @RequestMapping(value = "/catalog-update", method = RequestMethod.GET)   //todo split into 3 different models
    public String showCatalogUpdate(Model model,
                                    @RequestParam(value = "id", required = true) long updateId,
                                    @RequestParam(value = "er", required = false) String erCode,
                                    @RequestParam(value = "from", defaultValue = "0") int from,
                                    @RequestParam(value = "from-new", defaultValue = "0") int fromNew,
                                    @RequestParam(value = "active-tab", required = false) String activeTab,
                                    @RequestParam(value = "page", defaultValue = "0") int page
    ) {

        model.addAttribute("erCode", erCode);

        CatalogUpdate update = dbService.getCatalogUpdate(updateId);
        if (update != null) {
            model.addAttribute("update", update);

            Catalog catalog = dbService.getCatalog(update.getCatalogId());
            model.addAttribute("catalog", catalog);

            if (from > update.getCrossing()) {
                from = update.getCrossing();
            }


            switch (activeTab) {
                case "tab1": {

                    model.addAttribute("tab1", "active");

                    List<TrackDiff> diffs =
                            dbService.
                                    geChangedTracksOfCatalogUpdate(updateId, from, TRACKS_PER_PAGE);
                    model.addAttribute("diffs", diffs);

                    break;
                }
                case "tab2": {

                    model.addAttribute("tab2", "active");

                    List<Track> allNewTracks =
                            dbService.
                                    getNewTracksOfCatalogUpdate(updateId, fromNew, TRACKS_PER_PAGE);
//                                                    getTempTracks(catalog.getId(), fromNew, TRACKS_PER_PAGE);
                    model.addAttribute("tracks", allNewTracks);

                    break;
                }
                default: {

                    model.addAttribute("tab1", "active");

                    List<TrackDiff> diffs =
                            dbService.
                                    geChangedTracksOfCatalogUpdate(updateId, from, TRACKS_PER_PAGE);
                    model.addAttribute("diffs", diffs);

                    List<Track> allNewTracks =
                            dbService.
                                    getNewTracksOfCatalogUpdate(updateId, fromNew, TRACKS_PER_PAGE);
//                                                    getTempTracks(catalog.getId(), fromNew, TRACKS_PER_PAGE);
                    model.addAttribute("tracks", allNewTracks);

                    break;
                }
            }

            model.addAttribute("fromNew", fromNew);
            model.addAttribute("from", from);
            model.addAttribute("page", page);
            model.addAttribute("pageSize", TRACKS_PER_PAGE);
        }


        return "catalog-update/catalog-update";
    }


    @RequestMapping(value = "apply-update", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String applyUpdate(
            @RequestParam(value = "uid") long id
    ) {

        doApplyUpdate(id);
        return "status:ok";
    }


    @Async
    private void doApplyUpdate(long id) {
        log.info("Applying catalog updates, id: " + id);
//        changeStatus(CatalogUpdateApplyStatus.APPLY_CATALOG_STEP1);
        dbService.applyCatalogUpdateStep1(id);

//        changeStatus(CatalogUpdateApplyStatus.APPLY_CATALOG_STEP2);
        dbService.applyCatalogUpdateStep2(id);

//        changeStatus(CatalogUpdateApplyStatus.APPLY_CATALOG_STEP3);
        dbService.applyCatalogUpdateStep3(id);

        log.info("Get all new tracks for reindex");
//        changeStatus(CatalogUpdateApplyStatus.APPLY_CATALOG_STEP3);
        List<Track> updatedTracks = dbService.getAllTracksOfCatalogUpdate(id);

        log.info("Found " + updatedTracks.size() + " indexes. Rebuilding index for this tracks");
        try {
            LuceneIndexRebuildUtil.rebuildIndex(updatedTracks);
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Done. Reinitializing searcher");
        //reinit searcher after index update
//            LuceneSearch.getInstance().initSearcher(INDEX_DIR);
//            luceneSearch.index(tracks, LuceneIndexRebuildUtil.INDEX_DIR);

//        changeStatus(CatalogUpdateApplyStatus.FINISHED);

        log.info("Applied.");
    }



}

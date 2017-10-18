package kz.bgm.platform.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import kz.bgm.platform.model.domain.CatalogUpdate;
import kz.bgm.platform.model.domain.Track;
import kz.bgm.platform.model.service.CatalogUpdateService;
import kz.bgm.platform.model.service.CatalogUpdateServiceImpl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class CatalogUpdaterUtil {

    private static final String SRC_JDBC_PROPERTIES = "./src/jdbc.properties";
    private CatalogUpdateService catalogUpdateService;

    private CatalogUpdaterUtil(Properties dbProps) {
        catalogUpdateService = new CatalogUpdateServiceImpl();
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(dbProps.getProperty("jdbc.username"));
        dataSource.setPassword(dbProps.getProperty("jdbc.password"));
        dataSource.setURL(dbProps.getProperty("jdbc.url"));
        ((CatalogUpdateServiceImpl) catalogUpdateService).setDataSource(dataSource);
    }

    private void update(CatalogUpdate update) {
        System.out.println("Got catalog updates " + update.getFileName());

        update = catalogUpdateService.saveCatalogUpdate(update);
        System.out.println("Starting update catalog...");
        CatalogUpdate updateResult = catalogUpdateService.importCatalogUpdate(update);

        System.out.println("Load complete, calc stats...");
        catalogUpdateService.calculateCatalogUpdateStats(update.getId(), updateResult.getStatus());

        System.out.println("Applying catalog updates, id: " + update.getId());
        catalogUpdateService.applyCatalogUpdateStep1(update.getId());
        catalogUpdateService.applyCatalogUpdateStep2(update.getId());
        catalogUpdateService.applyCatalogUpdateStep3(update.getId());

        System.out.println("Get all new tracks for reindex");
        List<Track> updatedTracks = catalogUpdateService.getAllTracksOfCatalogUpdate(update.getId());

        System.out.println("Found " + updatedTracks.size() + " indexes. Rebuilding index for this tracks");
//        System.out.println("Done. Reinitializing searcher");
        System.out.println("Applied.");
    }

    private static CatalogUpdate readUpdateInfoFromJson(String fpath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(fpath);
        return mapper
                .reader(CatalogUpdate.class)
                .readValue(file);
    }


    public static void main(String[] args) throws IOException {
        String updateJsonPath = args[0];
        CatalogUpdate updateInfo = readUpdateInfoFromJson(updateJsonPath);
        System.out.println(updateInfo.fieldsAsQuery());

        Properties dbProps = new Properties();
        dbProps.load(new FileReader(SRC_JDBC_PROPERTIES));
        CatalogUpdaterUtil util = new CatalogUpdaterUtil(dbProps);
        util.update(updateInfo);
    }
}

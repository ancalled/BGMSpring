package kz.bgm.platform.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import kz.bgm.platform.model.domain.CatalogUpdate;
import kz.bgm.platform.model.service.CatalogUpdateService;
import kz.bgm.platform.model.service.CatalogUpdateServiceImpl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class CatalogUpdaterUtil {

    public static final String SRC_JDBC_PROPERTIES = "./src/jdbc.properties";
    private CatalogUpdateService catalogUpdateService;

    private CatalogUpdaterUtil(Properties dbProps) {
        catalogUpdateService = new CatalogUpdateServiceImpl();
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(dbProps.getProperty("jdbc.username"));
        dataSource.setPassword(dbProps.getProperty("jdbc.password"));
        dataSource.setURL(dbProps.getProperty("jdbc.url"));
        ((CatalogUpdateServiceImpl) catalogUpdateService).setDataSource(dataSource);
    }

    private void update(UpdateInfo info) {
        CatalogUpdate update = new CatalogUpdate();
        update.setCatalogId(info.catalogId);
        update.setFilePath(info.path);
        update.setFileName(info.fname);
        update.setEncoding(info.encoding);
        update.setSeparator(info.fieldSeparator);
        update.setEnclosedBy(info.enclosedBy);
        update.setNewline(info.newline);
        update.setFromLine(info.fromLine);

        System.out.println("Got catalog updates " + info.fname);

        update = catalogUpdateService.saveCatalogUpdate(update);
        System.out.println("Starting update catalog...");
//        changeStatus(UpdateStatus.FILE_UPLOADED);
        CatalogUpdate updateResult = catalogUpdateService.importCatalogUpdate(update);

        System.out.println("Load complete, calc stats...");
        catalogUpdateService.calculateCatalogUpdateStats(update.getId(), updateResult.getStatus());
    }

    public static UpdateInfo readInfo(String fpath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(fpath);
        return mapper
                .reader(UpdateInfo.class)
                .readValue(file);
    }

    public static void main1(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("./data/1.json");
        UpdateInfo info = mapper
                .reader(UpdateInfo.class)
                .readValue(file);

        System.out.println(info.catalogId);
    }

    public static void main(String[] args) throws IOException {
        String updatePath = args[0];
        UpdateInfo info = readInfo(updatePath);

        Properties dbProps = new Properties();
        dbProps.load(new FileReader(SRC_JDBC_PROPERTIES));
        CatalogUpdaterUtil util = new CatalogUpdaterUtil(dbProps);
        util.update(info);
    }
}

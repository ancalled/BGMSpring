package kz.bgm.platform.model.service;


import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.db.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
public class MainServiceImpl implements MainService {


    private JdbcTemplate db;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.db = new JdbcTemplate(dataSource);
    }


    private int getCatalogId(String cat) {
        return db.queryForObject(
                "SELECT id FROM catalog WHERE " +
                        "name=?",
                Integer.class, cat
        );
    }


    @Override
    public Platform getPlatform(long id) {
        List<Platform> res = db.query(
                "SELECT * FROM platform WHERE " +
                        "id=?",
                new PlatformMapper(), id
        );

        return !res.isEmpty() ? res.get(0) : null;
    }


    @Override
    public Catalog getCatalog(long id) {
        List<Catalog> res = db.query(
                "SELECT * FROM catalog WHERE " +
                        "id=?",
                new CatalogMapper(), id
        );

        return !res.isEmpty() ? res.get(0) : null;
    }

    @Override
    public Track getTrack(long id) {
        List<Track> res = db.query(
                "SELECT * FROM composition WHERE " +
                        "id=?",
                new TrackMapper(), id
        );

        return !res.isEmpty() ? res.get(0) : null;
    }


    @Override
    public List<Platform> getPlatforms() {
        return db.query(
                "SELECT * FROM platform",
                new PlatformMapper()
        );
    }


    @Override
    public List<Platform> getPlatformsWithCatalogs() {
        List<Platform> platforms = getPlatforms();
        List<Catalog> catalogs = getCatalogs();

        Map<Long, List<Catalog>> catMap =
                catalogs.stream()
                        .collect(groupingBy(Catalog::getPlatformId,
                                mapping(c -> c, toList())));

        platforms.stream()
                .forEach(p -> p.setCatalogs(catMap.get(p.getId())));

        return platforms;
    }

    @Override
    public List<Catalog> getCatalogs() {
        return db.query(
                "SELECT * FROM catalog",
                new CatalogMapper()
        );
    }


    @Override
    public List<Platform> getOwnPlatforms() {
        return db.query(
                "SELECT p.id p_id, " +
                        "p.name p_name, " +
                        "p.rights p_rights, " +
                        "c.id c_id, " +
                        "c.name c_name, " +
                        "c.royalty c_royalty, " +
                        "c.platform_id c_platform_id, " +
                        "c.tracks c_tracks, " +
                        "c.artists c_artists, " +
                        "c.right_type c_right_type, " +
                        "c.color c_color " +
                        "FROM platform p " +
                        "LEFT JOIN catalog c " +
                        "ON p.id = c.platform_id  " +
                        "WHERE p.rights = TRUE",
                new PlatformAndCatalogsMapper("p_", "c_")
        );
    }




    @Override
    public List<Long> getAllCatalogIds() {
        return db.queryForList("SELECT id FROM catalog", Long.class);
    }


    @Override
    public List<Long> getOwnCatalogIds() {
        return db.queryForList(
                "SELECT c.id FROM catalog c " +
                        "LEFT JOIN platform p " +
                        "ON (c.platform_id = p.id) " +
                        "WHERE p.rights = TRUE",
                Long.class
        );
    }


    @Override
    public List<Track> getRandomTracks(int num) {
        List<Track> tracks = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            List<Track> res = db.query(
                    "SELECT * FROM composition AS c JOIN " +
                            "  (SELECT (RAND() * (SELECT MAX(id) FROM composition)) AS id) r " +
                            "WHERE c.id >= r.id " +
                            "ORDER BY c.id ASC " +
                            "LIMIT 1;",
                    new TrackMapper()
            );

            tracks.addAll(res);
        }
        return tracks;
    }


    @Override
    public List<Track> getRandomTracks(long catalogId, int num) {
        List<Track> tracks = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            List<Track> res = db.query(
                    "SELECT * FROM composition AS c JOIN" +
                            "  (SELECT (RAND() * (SELECT MAX(id) FROM composition)) AS id) r " +
                            "WHERE c.id >= r.id " +
                            "AND catalog_id = ? " +
                            "ORDER BY c.id ASC " +
                            "LIMIT 1;",
                    new TrackMapper(), catalogId
            );

            tracks.addAll(res);
        }

        return tracks;
    }



    @Override
    public AdminUser getAdmin(String name, String pass) {
        List<AdminUser> res = db.query(
                "SELECT * FROM user_admin WHERE " +
                        "login = ? " +
                        "AND password = ?",
                new AdminUserMapper(),
                name, pass
        );

        return !res.isEmpty() ? res.get(0) : null;
    }

    @Override
    public User getUser(String name, String pass) {
        List<User> res = db.query(
                "SELECT * FROM user WHERE " +
                        "login = ? " +
                        "AND password = ?",
                new UserMapper(),
                name, pass
        );

        return !res.isEmpty() ? res.get(0) : null;
    }

    @Override
    public User getUser(String name) {
        List<User> res = db.query(
                "SELECT * FROM user WHERE " +
                        "login = ?",
                new UserMapper(),
                name
        );

        return !res.isEmpty() ? res.get(0) : null;
    }


    @Override
    public void removeItemFromBasket(long trackId, long customerId) {
        db.update(
                "DELETE FROM customer_basket_item WHERE " +
                        "track_id = ? " +
                        "AND customer_id= ?",
                trackId, customerId
        );
    }


    @Override
    public void removeItemFromReport(long itemId) {
        db.update(
                "UPDATE customer_report_item SET " +
                        "deleted = TRUE " +
                        "WHERE id = ?",
                itemId
        );
    }


    @Override
    public List<Long> getAvailableCatalogs(long customerId) {
        return db.queryForList(
                "SELECT cat.id " +
                        "FROM catalog cat " +
                        "LEFT JOIN customer c " +
                        "ON c.right_type = cat.right_type " +
                        "OR c.right_type=? " +
                        "WHERE c.id = ?",
                Long.class, customerId
        );
    }



    @Override
    public int getTrackCount() {
        return db.queryForObject("SELECT COUNT(*) cnt FROM composition",
                Integer.class);
    }


    public List<Track> getTracks(int from, int size) {
        return db.query(
                "SELECT * FROM composition LIMIT ?, ?",
                new TrackMapper(),
                from, size
        );
    }


    //  User Basket  ----------------------------------------------------------------------------------------


    @Override
    public List<Long> getCustomerBasket(long customerId) {
        return db.queryForList(
                "SELECT track_id FROM customer_basket_item WHERE customer_id = ?",
                Long.class, customerId
        );
    }


    @Override
    public void addItemToBasket(long customerId, long trackId) {
        db.update(
                "INSERT INTO customer_basket_item (customer_id, track_id) VALUES (?,?)",
                customerId, trackId

        );
    }


    // --------------------------------------------------------------------------------------------------------

    @Override
    public void exportCatalogToCSV(long catalogId,
                                   String path,
                                   String fieldTerminator,
                                   String enclosedBy,
                                   String linesTerminator) {

        db.update(
                "SELECT " +
                        "            'code', " +
                        "            'track', " +
                        "            'artist', " +
                        "            'composer', " +
                        "            'shareMobile', " +
                        "            'sharePublic' " +
                        "          UNION ALL " +
                        "          SELECT " +
                        "            code, " +
                        "            replace(replace(name, '" + fieldTerminator + "', ''), '" + enclosedBy + "', ''), " +
                        "            replace(replace(artist, '" + fieldTerminator + "', ''), '" + enclosedBy + "', ''), " +
                        "            replace(replace(composer, '" + fieldTerminator + "', ''), '" + enclosedBy + "', ''), " +
                        "            shareMobile, " +
                        "            sharePublic " +
                        "          FROM composition " +
                        "          WHERE catalog_id = ? " +
                        "        INTO OUTFILE ? " +
                        "        FIELDS TERMINATED BY '" + fieldTerminator + "' " +
                        (enclosedBy != null ? "          ENCLOSED BY '" + enclosedBy + "' " : "") +
                        "        LINES TERMINATED BY '" + linesTerminator + "';",
                catalogId, path
        );
    }
}

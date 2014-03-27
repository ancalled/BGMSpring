package kz.bgm.platform.model.service;


import kz.bgm.platform.model.domain.CatalogUpdate;
import kz.bgm.platform.model.domain.Track;
import kz.bgm.platform.model.domain.TrackDiff;
import kz.bgm.platform.model.domain.UpdateWarning;
import kz.bgm.platform.model.service.db.CatalogUpdateMapper;
import kz.bgm.platform.model.service.db.TrackDiffMapper;
import kz.bgm.platform.model.service.db.TrackMapper;
import kz.bgm.platform.model.service.db.TrackSetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;

@Service
public class CatalogUpdateServiceImpl implements CatalogUpdateService {

    private JdbcTemplate db;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.db = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Track> getNewTracksOfCatalogUpdate(long updateId, int from, int size) {
        return db.query(
                "SELECT    " +
                        "t.id t_id,   " +
                        "t.code t_code,   " +
                        "t.catalog_id t_catalog_id,   " +
                        "t.name t_name,   " +
                        "t.artist t_artist,   " +
                        "t.composer t_composer,   " +
                        "t.shareMobile t_shareMobile,   " +
                        "t.sharePublic t_sharePublic   " +
                        "FROM comp_tmp t   " +
                        "LEFT JOIN composition c   " +
                        "ON c.code = t.code   " +
                        "AND c.catalog_id = t.catalog_id   " +
                        "WHERE t.update_id = ? " +
                        "     AND c.id IS NULL " +
                        "LIMIT ?, ?;",
                new TrackMapper("t_"),
                updateId, from, size
        );
    }

    public List<TrackDiff> geChangedTracksOfCatalogUpdate(long updateId, int from, int size) {
        return db.query(
                "SELECT c.code code," +
                        "c.id c_id, " +
                        "c.code c_code, " +
                        "c.catalog_id c_catalog_id, " +
                        "c.name c_name, " +
                        "c.artist c_artist, " +
                        "c.composer c_composer, " +
                        "c.shareMobile c_shareMobile, " +
                        "c.sharePublic c_sharePublic, " +
                        "t.id t_id, " +
                        "t.code t_code, " +
                        "t.catalog_id t_catalog_id, " +
                        "t.name t_name, " +
                        "t.artist t_artist, " +
                        "t.composer t_composer, " +
                        "t.shareMobile t_shareMobile, " +
                        "t.sharePublic t_sharePublic " +
                        "FROM comp_tmp t " +
                        "INNER JOIN composition c " +
                        "ON c.code = t.code " +
                        "AND c.catalog_id = t.catalog_id " +
                        "WHERE t.update_id = ? " +
                        " AND (c.name != t.name OR  " +
                        "       c.artist != t.artist OR  " +
                        "       c.composer != t.composer OR  " +
                        "       c.shareMobile != t.shareMobile OR  " +
                        "       c.sharePublic != t.sharePublic) " +
                        "LIMIT ?, ?",
                new TrackDiffMapper("c_", "t_"),
                updateId, from, size
        );
    }


    public List<TrackDiff> getTracksWithChangedRoyaltyOfCatalogUpdate(long updateId, int from, int size) {
        return db.query(
                "SELECT c.code code," +
                        "c.id c_id, " +
                        "c.code c_code, " +
                        "c.catalog_id c_catalog_id, " +
                        "c.name c_name, " +
                        "c.artist c_artist, " +
                        "c.composer c_composer, " +
                        "c.shareMobile c_shareMobile, " +
                        "c.sharePublic c_sharePublic, " +
                        "t.id t_id, " +
                        "t.code t_code, " +
                        "t.catalog_id t_catalog_id, " +
                        "t.name t_name, " +
                        "t.artist t_artist, " +
                        "t.composer t_composer, " +
                        "t.shareMobile t_shareMobile, " +
                        "t.sharePublic t_sharePublic " +
                        "FROM comp_tmp t " +
                        "INNER JOIN composition c " +
                        "ON c.code = t.code " +
                        "AND c.catalog_id = t.catalog_id " +
                        "WHERE t.update_id = ? " +
                        " AND (c.shareMobile != t.shareMobile OR " +
                        "       c.sharePublic != t.sharePublic) " +
                        "LIMIT ?, ?",
                new TrackDiffMapper("c_", "t_"),
                updateId, from, size

        );
    }




    /*
       Apply changed tracks
    */
    @Override
    public void applyCatalogUpdateStep1(long updateId) {
        db.update(
                "UPDATE composition c " +
                        "  INNER JOIN comp_tmp t " +
                        "    ON c.code = t.code " +
                        "     AND c.catalog_id = t.catalog_id " +
                        "SET c.name = IF(t.name != '', t.name, c.name), " +
                        "  c.composer = IF(t.composer != '', t.composer, c.composer), " +
                        "  c.artist = IF(t.artist != '', t.artist, c.artist), " +
                        "  c.shareMobile = IF(t.shareMobile != '', t.shareMobile, c.shareMobile), " +
                        "  c.sharePublic = IF(t.sharePublic != '', t.sharePublic, c.sharePublic), " +
                        "  t.done = 1 " +
                        "WHERE t.update_id = ?", updateId
        );
    }


    /*
        Apply new tracks
     */
    @Override
    public void applyCatalogUpdateStep2(long updateId) {
        db.update(
                "INSERT INTO composition (code, name, composer, artist, shareMobile, sharePublic, catalog_id) " +
                        "  SELECT " +
                        "    code, " +
                        "    name, " +
                        "    composer, " +
                        "    artist, " +
                        "    shareMobile, " +
                        "    sharePublic, " +
                        "    catalog_id " +
                        "  FROM comp_tmp " +
                        "  WHERE done IS NULL AND update_id = ?",
                updateId
        );
    }

    /*
        Apply changed tracks
     */
    @Override
    public void applyCatalogUpdateStep3(long updateId) {
        db.update(
                "UPDATE catalog_update SET applied = TRUE WHERE id = ?",
                updateId
        );

        db.update(
                "UPDATE catalog cat, " +
                        "(SELECT * FROM catalog_update WHERE id =?)cat_upd " +
                        "SET cat.tracks = (SELECT count(*) FROM composition WHERE catalog_id=cat_upd.catalog_id), " +
                        "cat.artists=(SELECT count(DISTINCT (artist)) FROM composition WHERE catalog_id=cat_upd.catalog_id) " +
                        "                         WHERE cat.id=cat_upd.catalog_id;",
                updateId
        );

    }


    @Override
    public CatalogUpdate getCatalogUpdate(long updateId) {
        List<CatalogUpdate> res = db.query(
                "SELECT * FROM catalog_update WHERE id = ?",
                new CatalogUpdateMapper(), updateId
        );

        return !res.isEmpty() ? res.get(0) : null;
    }


    @Override
    public List<CatalogUpdate> getAllCatalogUpdates(final long catalogId) {
        return db.query(
                "SELECT * FROM catalog_update WHERE catalog_id = ? " +
                        "ORDER BY whenUpdated DESC ",
                new CatalogUpdateMapper(), catalogId
        );
    }


    public CatalogUpdate saveCatalogUpdate(final CatalogUpdate update) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        db.update(c -> {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO catalog_update(" +
                            "catalog_id, " +
                            "filepath, " +
                            "filename, " +
                            "whenUpdated, " +
                            "status) " +
                            "VALUES (?, ?, ?, NOW(), ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, update.getCatalogId());
            ps.setString(2, update.getFilePath());
            ps.setString(3, update.getFileName());
            ps.setString(4, CatalogUpdate.Status.NONE.toString());

            return ps;
        }, keyHolder);

        long updateId = keyHolder.getKey().longValue();
        update.setId(updateId);
        return update;
    }


    public CatalogUpdate importCatalogUpdate(final CatalogUpdate update) {
        db.update(
                "LOAD DATA LOCAL INFILE ? " +
                        "INTO TABLE comp_tmp " +
                        "CHARACTER SET ? " +
                        "FIELDS TERMINATED BY ? " +
                        "OPTIONALLY ENCLOSED BY ?" +
                        "LINES TERMINATED BY ? " +
                        "IGNORE ? LINES " +
                        "(@dummy, code, name, composer, artist, @shareMobile, @sharePublic) " +
//                                "(@dummy, code, name, composer, artist, @dummy, @dummy, @shareMobile, @sharePublic) " +
                        "SET update_id=?, " +
                        "  catalog_id=?, " +
                        "  shareMobile=IF(@shareMobile != '', @shareMobile, 0), " +
                        "  sharePublic=IF(@sharePublic != '', @sharePublic, 0)",
                update.getFilePath(),
                update.getEncoding(),
                update.getSeparator(),
                update.getEnclosedBy(),
                update.getNewline(),
                update.getFromLine(),
                update.getId(),
                update.getCatalogId()
        );


        List<UpdateWarning> warnList = db.query("SHOW WARNINGS", (rs, i) -> {
            String level = rs.getString("Level");
            if ("Warning".equals(level)) {
                UpdateWarning w = new UpdateWarning();
                w.setNumber(rs.getInt("Code"));
                w.parseMessage(rs.getString("Message"));
                return w;
            }

            return null;
        });

        warnList.stream().forEach(w -> {
            if (w != null) update.addWarning(w);
        });


        return update;
    }


    public CatalogUpdate calculateCatalogUpdateStats(final long updateId, final CatalogUpdate.Status st) {
        db.update(
                "UPDATE catalog_update u " +

                        "SET status = ?, " +

                        "tracks = (" +
                        "   SELECT count(*) " +
                        "   FROM comp_tmp " +
                        "   WHERE update_id = u.id" +
                        "), " +

                        "crossing = (" +
                        "   SELECT count(DISTINCT t.id) " +
                        "   FROM composition c " +
                        "       INNER JOIN comp_tmp t " +
                        "           ON c.code = t.code " +
                        "           AND c.catalog_id = t.catalog_id " +
                        "   WHERE t.update_id = u.id" +
                        "), " +

                        "new_tracks = (" +
                        "   SELECT count(DISTINCT c.id) " +
                        "   FROM composition c " +
                        "       INNER JOIN comp_tmp t" +
                        "           ON c.code = t.code " +
                        "           AND c.catalog_id = t.catalog_id " +
                        "   WHERE t.update_id = u.id " +
                        "           AND t.id IS NULL" +
                        "), " +

                        "changed_tracks = (" +
                        "   SELECT count(DISTINCT t.id) " +
                        "   FROM comp_tmp t " +
                        "       INNER JOIN composition c" +
                        "           ON c.code = t.code" +
                        "           AND c.catalog_id = t.catalog_id" +
                        "   WHERE t.update_id = u.id " +
                        "           AND (t.name != c.name OR " +
                        "                t.artist != c.artist OR " +
                        "                t.composer != c.composer OR " +
                        "                t.shareMobile != c.shareMobile OR " +
                        "                t.sharePublic != c.sharePublic)" +
                        "), " +

                        "rate_changed_tracks = (" +
                        "   SELECT count(DISTINCT t.id) " +
                        "   FROM comp_tmp t " +
                        "       INNER JOIN composition c" +
                        "           ON c.code = t.code" +
                        "           AND c.catalog_id = t.catalog_id" +
                        "   WHERE t.update_id = u.id " +
                        "           AND (t.shareMobile != c.shareMobile OR " +
                        "                t.sharePublic != c.sharePublic)" +
                        ") " +


                        "WHERE u.id = ?",
                st.toString(), updateId
        );


        return null; //todo why not int?
    }


    public List<Track> getAllTracksOfCatalogUpdate(long updateId) {
        return db.query(
                "SELECT * FROM comp_tmp WHERE " +
                        "update_id = ?",
                new TrackMapper(), updateId);
    }



    public void saveTracks(final List<Track> tracks, long catId) {

        db.batchUpdate("INSERT INTO " +
                        "composition(catalog_id, code, name, artist, " +
                        "composer,shareMobile,sharePublic) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                new TrackSetter(tracks, catId)
        );
    }


}

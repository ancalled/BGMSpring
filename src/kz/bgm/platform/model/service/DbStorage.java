package kz.bgm.platform.model.service;

import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.db.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;
import static kz.bgm.platform.model.domain.CatalogUpdate.Status;

@Repository
public class DbStorage implements CatalogStorage {


//    private static final Logger log = Logger.getLogger(DbStorage.class);

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
    public List<SearchResult> getTracks(List<SearchResult> found, List<Long> catalogIds) {

        if (found == null || catalogIds == null ||
                found.isEmpty() || catalogIds.isEmpty()) return null;

        List<Long> trackIds = found
                .stream()
                .map(SearchResult::getTrackId)
                .distinct()
                .collect(Collectors.toList());

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("trackIds", trackIds);
        params.addValue("catIds", catalogIds);

        List<Track> tracks = db.query(
                "SELECT * FROM composition WHERE " +
                        "id IN (:trackIds) " +
                        "AND catalog_id IN (:catIds)",
                new TrackMapper(),
                params
        );

        Map<Long, Track> trackMap = tracks
                .stream()
                .collect(Collectors.toMap(Track::getId, (t) -> t));

        found.stream().forEach(sr -> sr.setTrack(trackMap.get(sr.getTrackId())));

        return found;
    }

    @Override
    public List<Track> getTracks(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        return db.query(
                "SELECT * FROM composition WHERE " +
                        "id IN (:ids)",
                new TrackMapper(),
                singletonMap("ids", ids)
        );
    }


    @Override          //todo!
    public List<Track> searchTracks(final String field, final String value, final List<Long> catalogIds) {
        return null;


//        return query(new Action<List<Track>>() {
//
//            @Override
//            public List<Track> execute(Connection con) throws SQLException {
//
//                String catalogsPart = "";
//                if (catalogIds != null) {
//                    for (Long cat : catalogIds) {
//                        if (catalogIds.indexOf(cat) == 0) {
//                            catalogsPart = catalogsPart.concat("AND (");
//                        }
//                        catalogsPart = catalogsPart.concat("catalog_id=" + cat);
//                        if (catalogIds.indexOf(cat) != catalogIds.size() - 1) {
//                            catalogsPart = catalogsPart.concat(" OR ");
//                        } else {
//                            catalogsPart = catalogsPart.concat(")");
//                        }
//                    }
//                }
//                PreparedStatement stmt = con.prepareStatement(
//                        "SELECT * FROM composition WHERE " +
//                                field + "= ? " +
//                                catalogsPart,
//                        ResultSet.TYPE_FORWARD_ONLY,
//                        ResultSet.CONCUR_READ_ONLY
//                );
//                if ("code".equals(field)) {
//                    try {
//                        stmt.setLong(1, Long.parseLong(value));
//                    } catch (NumberFormatException ne) {
//                        System.out.println("not digits input");
//                        return Collections.emptyList();
//                    }
//                } else {
//                    stmt.setString(1, value);
//                }
//
//                stmt.setMaxRows(100);
//
//                ResultSet rs = stmt.executeQuery();
//
//                List<Track> tracks = new ArrayList<>();
//                while (rs.next()) {
//                    tracks.add(parseTrack(rs));
//                }
//                return tracks;
//            }
//        });
    }


    @Override
    public List<Track> searchTracksByName(String name) {
        return db.query(
                "SELECT * FROM composition WHERE " +
                        "name=?",
                new TrackMapper(), name
        );
    }


    @Override
    public List<SearchResult> searchTracksByCode(String code, List<Long> catIds) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("code", code);
        source.addValue("catIds", catIds);

        List<Track> tracks = db.query(
                "SELECT * FROM composition WHERE " +
                        "code=:code " +
                        "AND catalog_id IN (:catIds)",
                new TrackMapper(), source
        );

        return tracks
                .stream()
                .map(SearchResult::new)
                .collect(Collectors.toList());
    }


    @Override
    public List<Track> searchTracksByComposer(String composer) {
        return db.query(
                "SELECT * FROM composition WHERE " +
                        "composer=?",
                new TrackMapper(), composer
        );
    }


    @Override
    public List<Track> searchTracksByArtist(String artist) {
        return db.query(
                "SELECT * FROM composition WHERE " +
                        "artist=?",
                new TrackMapper(), artist
        );
    }

    @Override
    public List<Track> searchTrackByArtistLike(String artist) {
        return db.query(
                "SELECT * FROM composition WHERE " +
                        "artist LIKE ?",
                new TrackMapper(), artist
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
    public List<Customer> getAllCustomers() {
        return db.query("SELECT * FROM customer",
                new CustomerMapper());
    }


    @Override
    public Customer getCustomer(long id) {
        List<Customer> res = db.query(
                "SELECT * FROM customer WHERE " +
                        "id=?",
                new CustomerMapper(), id
        );

        return !res.isEmpty() ? res.get(0) : null;
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


//    ----------------------------------------------------------------------------------------


    public void saveTracks(final List<Track> tracks, String catalog) {
        final int catId = getCatalogId(catalog);

        db.batchUpdate("INSERT INTO " +
                        "composition(catalog_id, code, name, artist, " +
                        "composer,shareMobile,sharePublic) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                new TrackSetter(tracks, catId)
        );
    }


    @Override
    public long saveCustomerReport(final CustomerReport report) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        db.update(c -> {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO customer_report(" +
                            "customer_id, " +
                            "start_date, " +
                            "upload_date, " +
                            "type, " +
                            "period, " +
                            "tracks, " +
                            "detected, " +
                            "revenue, " +
                            "accepted) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );

            ps.setLong(1, report.getCustomerId());
            ps.setDate(2, new java.sql.Date(report.getStartDate().getTime()));
            ps.setDate(3, new java.sql.Date(report.getUploadDate().getTime()));
            ps.setInt(4, report.getType().ordinal());
            ps.setInt(5, report.getPeriod().ordinal());
            ps.setInt(6, report.getTracks());
            ps.setInt(7, report.getDetected());
            ps.setLong(8, report.getRevenue());
            ps.setBoolean(9, report.isAccepted());

            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }


    @Override
    public long updtTracksInCustomerReport(long id, int tracks) {
        db.update(
                "UPDATE customer_report " +
                        "SET tracks = ? " +
                        "WHERE id = ?",
                tracks, id
        );
        return id;
    }


    @Override
    public long updtDetectedTracksInCustomerReport(long id, int detected) {
        db.update(
                "UPDATE customer_report " +
                        "SET detected = ? " +
                        "WHERE id = ?",
                detected, id
        );
        return id;
    }


    @Override
    public long saveCustomerReportItem(final CustomerReportItem item) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        db.update(c -> {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO customer_report_item(" +
                            "report_id, " +
                            "composition_id, " +
                            "name, " +
                            "artist, " +
                            "content_type, " +
                            "qty, " +
                            "price, " +
                            "number" +
                            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );

            ps.setLong(1, item.getReportId());
            if (item.getCompositionId() != null) {
                ps.setLong(2, item.getCompositionId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            ps.setString(3, item.getTrack());
            ps.setString(4, item.getArtist());
            ps.setString(5, item.getContentType());
            ps.setInt(6, item.getQty());
            ps.setFloat(7, item.getPrice());
            ps.setInt(8, item.getNumber());

            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }


    @Override
    public long getUpdateCatalogQueryId() {
        return 0;
    }


    @Override
    public void saveCustomerReportItems(final List<CustomerReportItem> items) {
        db.batchUpdate("INSERT INTO customer_report_item(" +
                        "report_id, " +
                        "composition_id, " +
                        "name, " +
                        "artist, " +
                        "content_type, " +
                        "qty, " +
                        "price, " +
                        "detected, " +
                        "number" +
                        ") " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        CustomerReportItem cr = items.get(i);

                        ps.setLong(1, cr.getReportId());
                        if (cr.getCompositionId() != null) {
                            ps.setLong(2, cr.getCompositionId());
                        } else {
                            ps.setNull(2, Types.INTEGER);
                        }

                        ps.setString(3, cr.getTrack());
                        ps.setString(4, cr.getArtist());
                        ps.setString(5, cr.getContentType());
                        ps.setInt(6, cr.getQty());
                        ps.setFloat(7, cr.getPrice());
                        ps.setBoolean(8, cr.isDetected());
                        ps.setInt(9, cr.getNumber());
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                }
        );
    }


    @Override
    public List<CustomerReport> getAllCustomerReports(Date later) {
        return db.query(
                "SELECT" +
                        "  cr.id cr_id, " +
                        "  cr.customer_id cr_customer_id, " +
                        "  cr.start_date cr_start_date, " +
                        "  cr.upload_date cr_upload_date, " +
                        "  cr.type cr_type, " +
                        "  cr.period cr_period, " +
                        "  cr.tracks cr_tracks, " +
                        "  cr.detected cr_detected, " +
                        "  cr.revenue cr_revenue, " +
                        "  cr.accepted cr_accepted, " +
                        "  c.id c_id, " +
                        "  c.name c_name,  " +
                        "  c.shortName c_shortName,  " +
                        "  c.customer_type c_customer_type, \n" +
                        "  c.right_type c_right_type, \n" +
                        "  c.authorRoyalty c_authorRoyalty,  " +
                        "  c.relatedRoyalty c_relatedRoyalty,  " +
                        "  c.contract c_contract  " +
                        "FROM customer_report cr " +
                        "  LEFT JOIN customer c  " +
                        "  ON c.id = cr.customer_id " +
                        "WHERE cr.start_date > ? " +
                        "ORDER BY start_date",
                new CustomerReportAndCustomerMapper("cr_", "c_"),
                new java.sql.Date(later.getTime())
        );
    }


    @Override
    public CustomerReport getCustomerReport(long id) {
        List<CustomerReport> res = db.query(
                "SELECT * FROM customer_report WHERE id = ?",
                new CustomerReportMapper(), id);

        return !res.isEmpty() ? res.get(0) : null;
    }


    @Override
    public List<CustomerReport> getCustomerReports(long customerId, Date from, Date to) {
        return db.query(
                "SELECT * FROM customer_report " +
                        "WHERE customer_id = ? " +
                        "AND start_date BETWEEN ? AND ?",
                new CustomerReportMapper(),
                customerId, from, to
        );
    }


    @Override
    public CustomerReportItem getCustomerReportsItem(long id) {
        List<CustomerReportItem> res = db.query(
                "SELECT * FROM customer_report_item WHERE id = ?",
                new CustomerReportItemMapper(), id);

        return !res.isEmpty() ? res.get(0) : null;
    }


    public boolean acceptReport(long reportId) {
        int rs = db.update(
                "UPDATE customer_report " +
                        "SET accepted=TRUE " +
                        "WHERE id=?",
                reportId
        );
        return rs > 0;
    }


    @Override
    public List<CustomerReportItem> getCustomerReportsItems(long reportId) {
        return db.query(
                "SELECT * FROM customer_report_item WHERE " +
                        "report_id = ?",
                new CustomerReportItemMapper(),
                reportId);
    }


    @Override
    public List<CustomerReportItem> getCustomerReportsItems(long reportId, int from, int size) {
        return db.query(
                "SELECT i.id item_id, " +
                        "  i.report_id item_report_id, " +
                        "  i.composition_id item_composition_id, " +
                        "  i.name item_name, " +
                        "  i.artist item_artist, " +
                        "  i.content_type item_content_type, " +
                        "  i.qty item_qty, " +
                        "  i.price item_price, " +
                        "  i.detected item_detected, " +
                        "  i.number item_number, " +
                        "  i.deleted item_deleted, " +
                        "  t.id track_id, " +
                        "  t.catalog_id track_catalog_id, " +
                        "  t.code track_code, " +
                        "  t.name track_name, " +
                        "  t.artist track_artist, " +
                        "  t.composer track_composer, " +
                        "  t.shareMobile track_shareMobile, " +
                        "  t.sharePublic track_sharePublic,   " +
                        "  c.id cat_id, " +
                        "  c.name cat_name, " +
                        "  c.right_type cat_right_type, " +
                        "  c.platform_id cat_platform_id, " +
                        "  c.royalty cat_royalty, " +
                        "  c.tracks cat_tracks, " +
                        "  c.artists cat_artists, " +
                        "  c.color cat_color " +
                        "FROM customer_report_item i " +
                        "  LEFT JOIN composition t ON (i.composition_id = t.id) " +
                        "  LEFT JOIN catalog c ON (t.catalog_id = c.id) " +
                        "WHERE report_id = ? " +
//                                "AND t.shareMobile > 0 " +
                        "AND (deleted IS NULL OR NOT deleted)  " +
                        "  ORDER BY item_number, cat_right_type, track_shareMobile, track_sharePublic DESC  " +
                        "LIMIT ?, ?",
                new CustomerReportItemFullMapper("item_", "track_", "cat_"),
                reportId, from, size
        );
    }


    @Override
    public List<CalculatedReportItem> calculateMobileReport(String platform, Date from, Date to) {
        if (platform == null) return null;


        return db.query(
                "SELECT " +
                        "  i.id, " +
                        "  c.code, " +
                        "  i.content_type, " +
                        "  replace(c.name, CHAR(9), ' ') name, " +
                        "  replace(c.artist, CHAR(9), ' ') artist, " +
                        "  replace(c.composer, CHAR(9), ' ') composer, " +
                        "  IF(cat.right_type = 1, 'author', 'related') right_type, " +
                        "  p.name platform, " +
                        "  cat.name catalog, " +
                        " " +
                        "  c.shareMobile, " +
                        "  cat.royalty cat_royalty, " +
                        "  IF(cat.right_type = 1, cm.authorRoyalty, cm.relatedRoyalty) royalty, " +
                        " " +
                        "  price, " +
                        "  sum(qty) totalQty, " +
                        "  (price * sum(qty)) vol, " +
                        " " +
                        "  round((sum(qty) * price * (shareMobile / 100) * (IF(cat.right_type = 1, cm.authorRoyalty, cm.relatedRoyalty) / 100) * (cat.royalty / 100)), 3) revenue " +
                        " " +
                        " " +
                        "FROM customer_report_item i " +
                        " " +
                        "  LEFT JOIN composition c " +
                        "    ON (i.composition_id = c.id) " +
                        " " +
                        "  LEFT JOIN catalog cat " +
                        "    ON (cat.id = c.catalog_id) " +
                        " " +
                        "  LEFT JOIN platform p " +
                        "    ON (cat.platform_id = p.id) " +
                        " " +
                        "  LEFT JOIN customer_report r " +
                        "    ON (i.report_id = r.id) " +
                        " " +
                        "  LEFT JOIN customer cm " +
                        "    ON (r.customer_id = cm.id) " +
                        " " +
                        "WHERE p.name = ? " +
                        "      AND r.accepted=TRUE  " +
                        "      AND r.type = 0 " +
                        "      AND r.start_date BETWEEN ? AND ? " +
                        "      AND i.detected = TRUE " +
                        "      AND (i.deleted IS NULL OR NOT i.deleted) " +
                        " " +
                        "GROUP BY i.composition_id",
                new CalculatedReportItemMapper(),
                platform, 
                new java.sql.Date(from.getTime()), 
                new java.sql.Date(to.getTime())
        );

    }


    @Override
    public List<CalculatedReportItem> calculatePublicReport(String platform, Date from, Date to) {
        if (platform == null) return null;
        
        return db.query(
                "SELECT " +
                        "  i.id, " +
                        "  c.code, " +
                        "  replace(c.name, CHAR(9), ' ') name, " +
                        "  replace(c.artist, CHAR(9), ' ') artist, " +
                        "  replace(c.composer, CHAR(9), ' ') composer, " +
                        "  IF(cat.right_type = 1, 'author', 'related') right_type, " +
                        "  p.name platform, " +
                        "  cat.name catalog, " +
                        " " +
                        "  c.sharePublic, " +
                        "  cat.royalty cat_royalty, " +
                        "  sum(qty) totalQty " +
                        " " +
                        "FROM customer_report_item i " +
                        " " +
                        "  LEFT JOIN composition c " +
                        "    ON (i.composition_id = c.id) " +
                        " " +
                        "  LEFT JOIN catalog cat " +
                        "    ON (cat.id = c.catalog_id) " +
                        " " +
                        "  LEFT JOIN platform p " +
                        "    ON (cat.platform_id = p.id) " +
                        " " +
                        "  LEFT JOIN customer_report r " +
                        "    ON (i.report_id = r.id) " +
                        " " +
                        " " +
                        "WHERE p.name = ? " +
                        "      AND r.accepted=TRUE  " +
                        "      AND r.type = 1 " +
                        "      AND r.start_date BETWEEN ? AND ? " +
                        "      AND i.detected = TRUE " +
                        "      AND (i.deleted IS NULL OR NOT i.deleted) " +
                        " " +
                        "GROUP BY i.composition_id ;",
                new CalculatedPublicReportItemMapper(),
                platform, 
                new java.sql.Date(from.getTime()),
                new java.sql.Date(to.getTime())
        );
    }


    @Override
    public long createCustomer(Customer customer) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        db.update(c -> {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO customer(" +
                            "name, " +
                            "customer_type, " +
                            "right_type, " +
                            "authorRoyalty, " +
                            "relatedRoyalty, " +
                            "contract" +
                            ") " +
                            "VALUES(?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );

            ps.setString(1, customer.getName());
            ps.setInt(2, customer.getCustomerType().ordinal());
            ps.setInt(3, customer.getRightType().ordinal());
            ps.setFloat(4, customer.getAuthorRoyalty());
            ps.setFloat(5, customer.getRelatedRoyalty());
            ps.setString(6, customer.getContract());

            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }


    @Override
    public long createUser(final User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        db.update(c -> {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO user(" +
                            "login, " +
                            "password, " +
                            "customer_id, " +
                            "full_name, email" +
                            ") " +
                            "VALUES(?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );

            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPass());
            ps.setLong(3, user.getCustomerId());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());

            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
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
                itemId);
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
                Long.class, customerId);
    }


    @Override
    public Integer updateTrack(Track t) {
        return db.update(
                "UPDATE composition SET " +
                        "catalog_id = ?, " +
                        "code = ?," +
                        "name = ?," +
                        "artist = ?," +
                        "composer = ?," +
                        "shareMobile = ?," +
                        "sharePublic = ? " +
                        "WHERE id = ?",
                t.getCatalogId(),
                t.getCode(),
                t.getName(),
                t.getArtist(),
                t.getComposer(),
                t.getMobileShare(),
                t.getPublicShare(),
                t.getId()
        );
    }


    @Override
    public void removeUser(long id) {
        db.update("DELETE FROM user WHERE id = ?", id);
    }


    @Override
    public void removeCustomer(long id) {
        db.update("DELETE FROM customer WHERE id = ?", id);
    }


    @Override
    public List<User> getUsersByCustomerId(long id) {
        return db.query(
                "SELECT * FROM user WHERE " +
                        "customer_id = ?",
                new UserMapper(), id);
    }


    //  Catalog update -----------------------------------


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
            ps.setString(4, Status.NONE.toString());

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


    public CatalogUpdate calculateCatalogUpdateStats(final long updateId, final Status st) {
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


    // --- Index rebuild utils ---------------------


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


    @Override
    public void createCatalog(Catalog cat) {
        db.update("INSERT INTO catalog (" +
                        "name, " +
                        "royalty, " +
                        "platform_id, " +
                        "right_type) " +
                        "VALUES (?, ?, ?, ?)",
                cat.getName(),
                cat.getRoyalty(),
                cat.getPlatformId(),
                cat.getRightType().ordinal()
        );
    }


    @Override
    public void createPlatform(Platform p) {
        db.update(
                "INSERT INTO platform (" +
                        "name," +
                        "rights) " +
                        "VALUES (?, ?)",
                p.getName() ,
                p.isRights() ? 1 : 0
        );
    }



}

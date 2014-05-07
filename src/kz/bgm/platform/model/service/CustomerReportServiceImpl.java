package kz.bgm.platform.model.service;

import kz.bgm.platform.model.domain.CalculatedReportItem;
import kz.bgm.platform.model.domain.CustomerReport;
import kz.bgm.platform.model.domain.CustomerReportItem;
import kz.bgm.platform.model.service.db.*;
import kz.bgm.platform.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class CustomerReportServiceImpl implements CustomerReportService {

    private JdbcTemplate db;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.db = new JdbcTemplate(dataSource);
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
            ps.setDate(2, DateUtils.toSql(report.getStartDate()));
            ps.setDate(3, DateUtils.toSql(report.getUploadDate()));
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
    public List<CustomerReport> getAllCustomerReports(LocalDate later) {
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
                        "  c.name c_name, " +
                        "  c.shortName c_shortName, " +
                        "  c.customer_type c_customer_type, " +
                        "  c.right_type c_right_type, " +
                        "  c.authorRoyalty c_authorRoyalty, " +
                        "  c.relatedRoyalty c_relatedRoyalty, " +
                        "  c.contract c_contract  " +
                        "FROM customer_report cr " +
                        "  LEFT JOIN customer c  " +
                        "  ON c.id = cr.customer_id " +
                        "WHERE cr.start_date > ? " +
                        "ORDER BY start_date",
                new CustomerReportAndCustomerMapper("cr_", "c_"),
                DateUtils.toSql(later)
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
    public List<CustomerReport> getCustomerReports(long customerId, LocalDate from, LocalDate to) {
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
                        "  ORDER BY item_qty DESC, cat_right_type   " +
//                        "  ORDER BY item_number, cat_right_type, track_shareMobile, track_sharePublic DESC  " +
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
}

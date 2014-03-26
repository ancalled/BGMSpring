package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.CalculatedReportItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CalculatedReportItemMapper implements RowMapper<CalculatedReportItem> {

    private final String tblPrefix;

    public CalculatedReportItemMapper() {
        this("");
    }

    public CalculatedReportItemMapper(String tblPrefix) {
        this.tblPrefix = tblPrefix;
    }

    @Override
    public CalculatedReportItem mapRow(ResultSet rs, int i) throws SQLException {
        CalculatedReportItem report = new CalculatedReportItem();
        report.setReportItemId(rs.getLong(tblPrefix + "id"));
        report.setCompositionCode(rs.getString(tblPrefix + "code"));
        report.setCompositionName(rs.getString(tblPrefix + "name"));
        report.setArtist(rs.getString(tblPrefix + "artist"));
        report.setComposer(rs.getString(tblPrefix + "composer"));
        report.setPrice(rs.getFloat(tblPrefix + "price"));
        report.setQty(rs.getInt(tblPrefix + "totalQty"));
        report.setContentType(rs.getString(tblPrefix + "content_type"));
        report.setVol(rs.getFloat(tblPrefix + "vol"));
        report.setShareMobile(rs.getFloat(tblPrefix + "shareMobile"));
        report.setCustomerRoyalty(rs.getFloat(tblPrefix + "royalty"));
        report.setCatalogRoyalty(rs.getFloat(tblPrefix + "cat_royalty"));
        report.setRevenue(rs.getFloat(tblPrefix + "revenue"));
        report.setCatalog(rs.getString(tblPrefix + "catalog"));
        report.setCopyright(rs.getString(tblPrefix + "right_type"));
        return report;
    }
}

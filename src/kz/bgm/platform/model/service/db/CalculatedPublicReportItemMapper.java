package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.CalculatedReportItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CalculatedPublicReportItemMapper implements RowMapper<CalculatedReportItem> {

    private final String tblPrefix;

    public CalculatedPublicReportItemMapper() {
        this("");
    }

    public CalculatedPublicReportItemMapper(String tblPrefix) {
        this.tblPrefix = tblPrefix;
    }

    @Override
    public CalculatedReportItem mapRow(ResultSet rs, int i) throws SQLException {

        CalculatedReportItem report = new CalculatedReportItem();
        report.setReportItemId(rs.getLong("id"));
        report.setCompositionCode(rs.getString("code"));
        report.setCompositionName(rs.getString("name"));
        report.setArtist(rs.getString("artist"));
        report.setComposer(rs.getString("composer"));
//        report.setPrice(rs.getFloat("price"));
        report.setQty(rs.getInt("totalQty"));
//        report.setContentType(rs.getString("content_type"));
//        report.setVol(rs.getFloat("vol"));
        report.setShareMobile(rs.getFloat("sharePublic"));
//        report.setCustomerRoyalty(rs.getFloat("royalty"));
        report.setCatalogRoyalty(rs.getFloat("cat_royalty"));
//        report.setRevenue(rs.getFloat("revenue"));
        report.setCatalog(rs.getString("catalog"));
        report.setCopyright(rs.getString("right_type"));
        return report;
    }
}

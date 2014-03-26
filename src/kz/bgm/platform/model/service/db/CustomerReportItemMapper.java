package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.CustomerReportItem;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerReportItemMapper implements RowMapper<CustomerReportItem> {

    private final String tablePrefix;

    public CustomerReportItemMapper() {
        this("");
    }

    public CustomerReportItemMapper(String tblPrefix) {
        this.tablePrefix = tblPrefix;
    }

    @Override
    public CustomerReportItem mapRow(ResultSet rs, int i) throws SQLException {
        CustomerReportItem item = new CustomerReportItem();
        item.setId(rs.getLong(tablePrefix + "id"));
        item.setReportId(rs.getLong(tablePrefix + "report_id"));
        item.setCompositionId(rs.getLong(tablePrefix + "composition_id"));
        item.setTrack(rs.getString(tablePrefix + "name"));
        item.setArtist(rs.getString(tablePrefix + "artist"));
        item.setContentType(rs.getString(tablePrefix + "content_type"));
        item.setQty(rs.getInt(tablePrefix + "qty"));
        item.setPrice(rs.getFloat(tablePrefix + "price"));
        item.setDetected(rs.getBoolean(tablePrefix + "detected"));
        item.setNumber(rs.getInt(tablePrefix + "number"));

        return item;
    }
}

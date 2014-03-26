package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Catalog;
import kz.bgm.platform.model.domain.CustomerReportItem;
import kz.bgm.platform.model.domain.Track;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerReportItemFullMapper implements RowMapper<CustomerReportItem> {

    private final CustomerReportItemMapper itemMapper;
    private final TrackMapper trackMapper;
    private final CatalogMapper catMapper;

    public CustomerReportItemFullMapper(String itemPrefix, String trackPrefix, String catPrefix) {
        this.itemMapper = new CustomerReportItemMapper(itemPrefix);
        this.trackMapper = new TrackMapper(trackPrefix);
        this.catMapper = new CatalogMapper(catPrefix);
    }

    @Override
    public CustomerReportItem mapRow(ResultSet rs, int i) throws SQLException {
        CustomerReportItem item = itemMapper.mapRow(rs, i);
        Track track = trackMapper.mapRow(rs, i);
        Catalog catalog = catMapper.mapRow(rs, i);
        track.setFoundCatalog(catalog);
        item.setFoundTrack(track);
        return item;
    }
}

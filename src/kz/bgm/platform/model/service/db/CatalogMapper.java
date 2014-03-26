package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Catalog;
import kz.bgm.platform.model.domain.RightType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CatalogMapper implements RowMapper<Catalog> {

    private final String tblPrefix;

    public CatalogMapper() {
        this("");
    }

    public CatalogMapper(String tblPrefix) {
        this.tblPrefix = tblPrefix;
    }

    @Override
    public Catalog mapRow(ResultSet rs, int i) throws SQLException {
        Catalog catalog = new Catalog();
        catalog.setId(rs.getLong(tblPrefix + "id"));
        catalog.setName(rs.getString(tblPrefix + "name"));
        catalog.setRoyalty(rs.getFloat(tblPrefix + "royalty"));
        int rightType = rs.getInt(tblPrefix + "right_type");
        catalog.setRightType(RightType.values()[rightType]);
        catalog.setTracks(rs.getInt(tblPrefix + "tracks"));
        catalog.setPlatformId(rs.getLong(tblPrefix + "platform_id"));
        catalog.setArtists(rs.getInt(tblPrefix + "artists"));
        catalog.setColor(rs.getString(tblPrefix + "color"));
        return catalog;
    }
}

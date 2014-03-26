package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.CatalogUpdate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class CatalogUpdateMapper implements RowMapper<CatalogUpdate> {

    private final String tblPrefix;

    public CatalogUpdateMapper() {
        this("");
    }

    public CatalogUpdateMapper(String tblPrefix) {
        this.tblPrefix = tblPrefix;
    }

    @Override
    public CatalogUpdate mapRow(ResultSet rs, int i) throws SQLException {
        CatalogUpdate update = new CatalogUpdate();
        update.setId(rs.getLong(tblPrefix + "id"));
        update.setWhenUpdated(new Date(rs.getTimestamp(tblPrefix + "whenUpdated").getTime()));
        update.setCatalogId(rs.getLong(tblPrefix + "catalog_id"));
        update.setStatus(CatalogUpdate.Status.valueOf(rs.getString(tblPrefix + "status")));
        update.setTracks(rs.getInt(tblPrefix + "tracks"));
        update.setCrossing(rs.getInt(tblPrefix + "crossing"));
        update.setNewTracks(rs.getInt(tblPrefix + "new_tracks"));
        update.setChangedTracks(rs.getInt(tblPrefix + "changed_tracks"));
        update.setRateChangedTracks(rs.getInt(tblPrefix + "rate_changed_tracks"));
        update.setApplied(rs.getBoolean(tblPrefix + "applied"));
        update.setFilePath(rs.getString(tblPrefix + "filepath"));
        update.setFileName(rs.getString(tblPrefix + "filename"));

        return update;
    }
}

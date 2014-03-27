package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Track;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackMapper implements RowMapper<Track> {

    private final String tblPrefix;

    public TrackMapper(String tblPrefix) {
        this.tblPrefix = tblPrefix;
    }

    public TrackMapper() {
        this("");
    }

    @Override
    public Track mapRow(ResultSet rs, int i) throws SQLException {
        Track track = new Track();
        track.setId(rs.getLong(tblPrefix + "id"));
        track.setCatalogId(rs.getLong(tblPrefix + "catalog_id"));
        track.setCode(rs.getString(tblPrefix + "code"));
        track.setName(rs.getString(tblPrefix + "name"));
        track.setArtist(rs.getString(tblPrefix + "artist"));
        track.setComposer(rs.getString(tblPrefix + "composer"));
        track.setMobileShare(rs.getFloat(tblPrefix + "shareMobile"));
        track.setPublicShare(rs.getFloat(tblPrefix + "sharePublic"));
        return track;
    }
}

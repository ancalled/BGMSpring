package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Track;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackAndCatalogMapper implements RowMapper<Track> {

    private final TrackMapper trackMapper;
    private final String catNameField;

    public TrackAndCatalogMapper(String trackPrefix, String catNameField) {
        trackMapper = new TrackMapper(trackPrefix);
        this.catNameField = catNameField;
    }


    @Override
    public Track mapRow(ResultSet rs, int i) throws SQLException {
        Track track = trackMapper.mapRow(rs, i);
        String catName = rs.getString(catNameField);
        track.setCatalog(catName);
        return track;
    }
}

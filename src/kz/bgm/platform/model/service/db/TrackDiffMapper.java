package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.TrackDiff;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackDiffMapper implements RowMapper<TrackDiff> {

    private final TrackMapper oldTrackMapper;
    private final TrackMapper newTrackMapper;

    public TrackDiffMapper(String oldTrackPrefix, String newTrackPrefix) {
        this.oldTrackMapper = new TrackMapper(oldTrackPrefix);
        this.newTrackMapper = new TrackMapper(newTrackPrefix);
    }

    @Override
    public TrackDiff mapRow(ResultSet rs, int i) throws SQLException {
        TrackDiff d = new TrackDiff();
        d.setNumber(i);
        d.setCode(rs.getString("code"));
        d.setOldTrack(oldTrackMapper.mapRow(rs, i));
        d.setNewTrack(newTrackMapper.mapRow(rs, i));
        return d;
    }
}

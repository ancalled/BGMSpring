package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Track;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TrackSetter implements BatchPreparedStatementSetter {
    private final List<Track> tracks;
    private final long catId;

    public TrackSetter(List<Track> tracks, long catId) {
        this.tracks = tracks;
        this.catId = catId;
    }

    public void setValues(PreparedStatement ps, int i) throws SQLException {
        Track t = tracks.get(i);
        ps.setLong(1, catId);
        ps.setString(2, t.getCode());
        ps.setString(3, t.getName());
        ps.setString(4, t.getArtist());
        ps.setString(5, t.getComposer());
        ps.setFloat(6, t.getMobileShare());
        ps.setFloat(7, t.getPublicShare());
    }

    public int getBatchSize() {
        return tracks.size();
    }
}

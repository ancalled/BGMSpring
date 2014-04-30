package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Catalog;
import kz.bgm.platform.model.domain.Platform;
import kz.bgm.platform.model.domain.Track;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackAndCatalogMapper implements RowMapper<Track> {

    private final TrackMapper trackMapper;
    private final CatalogMapper catMapper;
    private final PlatformMapper platformMapper;

    public TrackAndCatalogMapper(String trackPrefix, String catalogPrefix, String platformPrefix) {
        trackMapper = new TrackMapper(trackPrefix);
        catMapper = new CatalogMapper(catalogPrefix);
        platformMapper = new PlatformMapper(platformPrefix);
    }


    @Override
    public Track mapRow(ResultSet rs, int i) throws SQLException {
        Track track = trackMapper.mapRow(rs, i);

        Catalog catalog = catMapper.mapRow(rs, i);
        if (catalog != null) {
            track.setFoundCatalog(catalog);

            Platform platform = platformMapper.mapRow(rs, i);
            catalog.setPlatform(platform);
        }
        return track;
    }
}

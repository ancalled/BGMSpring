package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Catalog;
import kz.bgm.platform.model.domain.Platform;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlatformAndCatalogsMapper implements RowMapper<Platform> {

    private final String platformPrefix;
    private final PlatformMapper platformMapper;
    private final CatalogMapper catalogMapper;
    private final Map<Long, Platform> platforms = new HashMap<>();

    public PlatformAndCatalogsMapper(String platformPrefix, String catalogPrefix) {
        this.platformPrefix = platformPrefix;
        this.platformMapper = new PlatformMapper(platformPrefix);
        this.catalogMapper = new CatalogMapper(catalogPrefix);
    }

    @Override
    public Platform mapRow(ResultSet rs, int i) throws SQLException {
        Long platformId = rs.getLong(platformPrefix + "id");

        Platform p = platforms.get(platformId);
        if (p == null) {
            p = platformMapper.mapRow(rs, i);
            p.setCatalogs(new ArrayList<>());
            platforms.put(platformId, p);
        }

        Catalog cat = catalogMapper.mapRow(rs, i);
        p.getCatalogs().add(cat);

        return p;
    }
}

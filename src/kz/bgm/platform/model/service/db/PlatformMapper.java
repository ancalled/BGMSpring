package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Platform;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlatformMapper implements RowMapper<Platform> {

    private final String tblPrefix;

    public PlatformMapper(String tblPrefix) {
        this.tblPrefix = tblPrefix;
    }

    public PlatformMapper() {
        this("");
    }

    @Override
    public Platform mapRow(ResultSet rs, int i) throws SQLException {

        Platform platform = new Platform();
        platform.setId(rs.getLong(tblPrefix + "id"));
        platform.setName(rs.getString(tblPrefix + "name"));
        platform.setRights(rs.getBoolean(tblPrefix + "rights"));
        return platform;
    }
}

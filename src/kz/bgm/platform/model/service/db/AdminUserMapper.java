package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.AdminUser;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminUserMapper implements RowMapper<AdminUser> {

    private final String tblPrefix;

    public AdminUserMapper(String tblPrefix) {
        this.tblPrefix = tblPrefix;
    }

    public AdminUserMapper() {
        this("");
    }

    @Override
    public AdminUser mapRow(ResultSet rs, int i) throws SQLException {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(rs.getLong(tblPrefix + "id"));
        adminUser.setLogin(rs.getString(tblPrefix + "login"));
        adminUser.setPass(rs.getString(tblPrefix + "password"));
        return adminUser;
    }
}

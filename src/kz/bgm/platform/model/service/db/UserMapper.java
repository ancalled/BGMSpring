package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {

    private final String tblPrefix;

    public UserMapper(String tblPrefix) {
        this.tblPrefix = tblPrefix;
    }

    public UserMapper() {
        this("");
    }

    @Override
    public User mapRow(ResultSet rs, int i) throws SQLException {
        User user = new User();
        user.setId(rs.getLong(tblPrefix + "id"));
        user.setLogin(rs.getString(tblPrefix + "login"));
        user.setPass(rs.getString(tblPrefix + "password"));
        user.setFullName(rs.getString(tblPrefix + "full_name"));
        user.setEmail(rs.getString(tblPrefix + "email"));
        user.setCustomerId(rs.getLong(tblPrefix + "customer_id"));
        return user;
    }
}

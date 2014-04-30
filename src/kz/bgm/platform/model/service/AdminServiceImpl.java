package kz.bgm.platform.model.service;

import kz.bgm.platform.model.domain.*;
import kz.bgm.platform.model.service.db.CustomerMapper;
import kz.bgm.platform.model.service.db.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private JdbcTemplate db;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.db = new JdbcTemplate(dataSource);
    }


    @Override
    public List<Customer> getAllCustomers() {
        return db.query("SELECT * FROM customer",
                new CustomerMapper());
    }


    @Override
    public Customer getCustomer(long id) {
        List<Customer> res = db.query(
                "SELECT * FROM customer WHERE " +
                        "id=?",
                new CustomerMapper(), id
        );

        return !res.isEmpty() ? res.get(0) : null;
    }

    @Override
    public Customer getCustomer(String name) {
        List<Customer> res = db.query(
                "SELECT * FROM customer WHERE " +
                        "name=?",
                new CustomerMapper(), name
        );

        return !res.isEmpty() ? res.get(0) : null;
    }

    @Override
    public List<User> getUsersByCustomerId(long id) {
        return db.query(
                "SELECT * FROM user WHERE " +
                        "customer_id = ?",
                new UserMapper(), id);
    }





    @Override
    public long createCustomer(Customer customer) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        db.update(c -> {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO customer(" +
                            "name, " +
                            "customer_type, " +
                            "right_type, " +
                            "authorRoyalty, " +
                            "relatedRoyalty, " +
                            "contract" +
                            ") " +
                            "VALUES(?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );

            ps.setString(1, customer.getName());
            ps.setInt(2, customer.getCustomerType().ordinal());
            ps.setInt(3, customer.getRightType().ordinal());
            ps.setFloat(4, customer.getAuthorRoyalty());
            ps.setFloat(5, customer.getRelatedRoyalty());
            ps.setString(6, customer.getContract());

            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }


    @Override
    public long createUser(final User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        db.update(c -> {
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO user(" +
                            "login, " +
                            "password, " +
                            "customer_id, " +
                            "full_name, email" +
                            ") " +
                            "VALUES(?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );

            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPass());
            ps.setLong(3, user.getCustomerId());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());

            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void removeUser(long id) {
        db.update("DELETE FROM user WHERE id = ?", id);
    }


    @Override
    public void removeCustomer(long id) {
        db.update("DELETE FROM customer WHERE id = ?", id);
    }


    @Override
    public Integer updateTrack(Track t) {
        return db.update(
                "UPDATE composition SET " +
                        "catalog_id = ?, " +
                        "code = ?," +
                        "name = ?," +
                        "artist = ?," +
                        "composer = ?," +
                        "shareMobile = ?," +
                        "sharePublic = ? " +
                        "WHERE id = ?",
                t.getCatalogId(),
                t.getCode(),
                t.getName(),
                t.getArtist(),
                t.getComposer(),
                t.getMobileShare(),
                t.getPublicShare(),
                t.getId()
        );
    }

    @Override
    public void createCatalog(Catalog cat) {
        db.update("INSERT INTO catalog (" +
                        "name, " +
                        "royalty, " +
                        "platform_id, " +
                        "right_type) " +
                        "VALUES (?, ?, ?, ?)",
                cat.getName(),
                cat.getRoyalty(),
                cat.getPlatformId(),
                cat.getRightType().ordinal()
        );
    }


    @Override
    public void createPlatform(Platform p) {
        db.update(
                "INSERT INTO platform (" +
                        "name," +
                        "rights) " +
                        "VALUES (?, ?)",
                p.getName() ,
                p.isRights() ? 1 : 0
        );
    }
}

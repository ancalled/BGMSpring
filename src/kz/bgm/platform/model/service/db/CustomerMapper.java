package kz.bgm.platform.model.service.db;

import kz.bgm.platform.model.domain.Customer;
import kz.bgm.platform.model.domain.CustomerType;
import kz.bgm.platform.model.domain.RightType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerMapper implements RowMapper<Customer> {

    private final String prefix;

    public CustomerMapper(String tblPrefix) {
        this.prefix = tblPrefix;
    }

    public CustomerMapper() {
        this("");
    }

    @Override
    public Customer mapRow(ResultSet rs, int i) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getLong(prefix + "id"));
        customer.setName(rs.getString(prefix + "name"));
        customer.setShortName(rs.getString(prefix + "shortName"));
        int type = rs.getInt(prefix + "customer_type");
        customer.setCustomerType(CustomerType.values()[type]);
        int rightType = rs.getInt(prefix + "right_type");
        customer.setRightType(RightType.values()[rightType]);
        customer.setAuthorRoyalty(rs.getFloat(prefix + "authorRoyalty"));
        customer.setRelatedRoyalty(rs.getFloat(prefix + "relatedRoyalty"));
        customer.setContract(rs.getString(prefix + "contract"));

        return customer;
    }
}

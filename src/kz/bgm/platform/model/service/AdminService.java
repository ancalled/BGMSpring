package kz.bgm.platform.model.service;


import kz.bgm.platform.model.domain.*;

import java.util.List;

public interface AdminService {

    List<Customer> getAllCustomers();

    Customer getCustomer(long id);

    Customer getCustomer(String name);

    List<User> getUsersByCustomerId(long customerId);


    long createUser(User user);

    long createCustomer(Customer customer);

    void removeUser(long id);

    void removeCustomer(long id);

    Integer updateTrack(Track track);

    void createCatalog(Catalog catalog);

    void createPlatform(Platform platform);


}

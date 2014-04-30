package kz.bgm.platform.model.service;


import kz.bgm.platform.model.domain.*;

import java.util.List;

public interface MainService {


    Platform getPlatform(long id);

    Catalog getCatalog(long id);

    Track getTrack(long id);

    List<Platform> getPlatforms();

    List<Platform> getPlatformsWithCatalogs();

    List<Platform> getOwnPlatforms();

    List<Long> getOwnCatalogIds();

    List<Catalog> getCatalogs();


    List<Long> getAllCatalogIds();

    List<Long> getNotEnemyCatalogIds();

    List<Track> getRandomTracks(int num);

    List<Track> getRandomTracks(long catalogId, int num);


    User getUser(String name, String pass);

    User getUser(String name);

    AdminUser getAdmin(String name, String pass);



    int getTrackCount();

    List<Track> getTracks(int from, int size);

    List<Long> getCustomerBasket(long userId);

    void addItemToBasket(long customerId, long trackId);

    void removeItemFromBasket(long trackId, long userId);

    void removeItemFromReport(long itemId);

    List<Long> getAvailableCatalogs(long customerId);



    void exportCatalogToCSV(long catalogId, String catalogName,
                            String fieldTerminator,
                            String enclosedBy, String linesTerminator);
}

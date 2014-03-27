package kz.bgm.platform.model.service;


import kz.bgm.platform.model.domain.CatalogUpdate;
import kz.bgm.platform.model.domain.Track;
import kz.bgm.platform.model.domain.TrackDiff;

import java.util.List;

public interface CatalogUpdateService {


    CatalogUpdate saveCatalogUpdate(CatalogUpdate update);

    CatalogUpdate importCatalogUpdate(CatalogUpdate update);

    public CatalogUpdate calculateCatalogUpdateStats(long updateId, CatalogUpdate.Status st);

    List<Track> getAllTracksOfCatalogUpdate(long updateId);

    List<Track> getNewTracksOfCatalogUpdate(long updateId, int from, final int size);

    List<TrackDiff> geChangedTracksOfCatalogUpdate(long updateId, int from, final int size);

    List<TrackDiff> getTracksWithChangedRoyaltyOfCatalogUpdate(long updateId, int from, final int size);


    void applyCatalogUpdateStep1(long updateId);

    void applyCatalogUpdateStep2(long updateId);

    void applyCatalogUpdateStep3(long updateId);


    CatalogUpdate getCatalogUpdate(long updateId);

    List<CatalogUpdate> getAllCatalogUpdates(long catalogId);


    void saveTracks(List<Track> trackList, long catId);

}

package kz.bgm.platform.model.service;


import kz.bgm.platform.model.domain.SearchResultItem;
import kz.bgm.platform.model.domain.Track;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

public interface SearchService {


    List<Track> searchTracks(String field, String value, List<Long> catalogIds);

    List<Track> searchTracksByName(String songName);

    List<SearchResultItem> searchTracksByCode(String code, List<Long> catalogs);

    List<Track> searchTracksByComposer(String composer);

    List<Track> searchTracksByArtist(String artist);

    List<Track> searchTrackByArtistLike(String artist);


    List<Track> getTracks(List<Long> ids);

    List<SearchResultItem> getTracks(List<SearchResultItem> ids, List<Long> catalogIds);

    List<SearchResultItem> search(String queryString, int limit) throws IOException, ParseException;

    List<SearchResultItem> search(String artist, String authors, String composition, int limit)
            throws IOException, ParseException;

}

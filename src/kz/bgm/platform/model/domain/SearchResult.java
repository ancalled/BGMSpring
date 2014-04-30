package kz.bgm.platform.model.domain;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchResult {

    private final long id;
    private final String query;
    private final SearchType searchType;
    private final List<Long> catalogIds;
    private final Date whenQueried;
    private List<SearchResultItem> tracks;
    private List<SearchResultGroup> groups = new ArrayList<>();

    public SearchResult(long id, String query, SearchType type, List<Long> catalogIds) {
        this.id = id;
        this.query = query;
        this.searchType = type;
        this.catalogIds = catalogIds;
        whenQueried = new Date();
    }

    public long getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public List<Long> getCatalogIds() {
        return catalogIds;
    }

    public Date getWhenQueried() {
        return whenQueried;
    }


    public List<SearchResultItem> getTracks() {
        return tracks;
    }

    public void setTracks(List<SearchResultItem> tracks) {
        this.tracks = tracks;
    }

    public List<SearchResultGroup> getGroups() {
        return groups;
    }

}

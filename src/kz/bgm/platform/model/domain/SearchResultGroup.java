package kz.bgm.platform.model.domain;


import java.util.ArrayList;
import java.util.List;

public class SearchResultGroup {

    private double score;
    private List<SearchResultItem> tracks;


    public SearchResultGroup(SearchResultItem item) {
        tracks = new ArrayList<>();
        tracks.add(item);
        score = item.getScore();
    }


    public SearchResultGroup(List<SearchResultItem> tracks) {
        this.tracks = tracks;
        score = tracks.stream().mapToDouble(SearchResultItem::getScore).max().getAsDouble();
    }



    public double getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public List<SearchResultItem> getTracks() {
        return tracks;
    }

    public void setTracks(List<SearchResultItem> tracks) {
        this.tracks = tracks;
    }
}

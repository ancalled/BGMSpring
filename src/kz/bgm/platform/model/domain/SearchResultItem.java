package kz.bgm.platform.model.domain;


public class SearchResultItem {

    private long trackId;
    private float score;
    private Track track;


    public SearchResultItem() {
    }

    public SearchResultItem(Track track) {
        this.track = track;
        if (track != null) {
            this.trackId = track.getId();
            this.score = 100;
        }
    }

    public SearchResultItem(long trackId, float score) {
        this.trackId = trackId;
        this.score = score;
    }

    public long getTrackId() {
        return trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }


}

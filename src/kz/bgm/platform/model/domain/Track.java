package kz.bgm.platform.model.domain;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Track implements Serializable {

    private long id = 0L;
    private long catalogId = 0;
    private String catalog = "";
    private String code = "";
    private String artist = "";
    private String name = "";
    private String composer = "";
    private float publicShare = 0F;
    private float mobileShare = 0F;
    private Catalog foundCatalog;

    public void setMobileShare(float mobileShare) {
        this.mobileShare = mobileShare;
    }
    public void setPublicShare(float publicShare) {
        this.publicShare = publicShare;
    }

    public Float getPublicShare() {
        return publicShare;
    }

    public Track() {
    }

    public Track(Long id, String code, String name, String artist) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.artist = artist;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String composition) {
        this.name = composition;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Float sharePublic() {
        return publicShare;
    }

    public Float getMobileShare() {
        return mobileShare;
    }


    public long getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(long catalogId) {
        this.catalogId = catalogId;
    }

    public void setComposer(String compositor) {
        this.composer = compositor;
    }

    public String getComposer() {
        return composer;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public Catalog getFoundCatalog() {
        return foundCatalog;
    }

    public void setFoundCatalog(Catalog foundCatalog) {
        this.foundCatalog = foundCatalog;
    }

    @Override
    public String toString() {
        return "code: " + code +
                ", name: " + name +
                ", artist: " + artist +
                ", composer: " + composer +
                ", mobileShare: " + mobileShare +
                ", publicShare: " + publicShare +
                ", catalogId: " + catalogId;
    }
}

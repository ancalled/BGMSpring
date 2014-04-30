package kz.bgm.platform.model.service;


import kz.bgm.platform.model.domain.SearchResultItem;
import kz.bgm.platform.model.domain.Track;
import kz.bgm.platform.model.service.db.TrackAndCatalogMapper;
import kz.bgm.platform.model.service.db.TrackMapper;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;

@Service
public class SearchServiceImpl implements SearchService {


    private static final Logger log = Logger.getLogger(SearchServiceImpl.class);

    public static final int LIMIT = 100;

    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ARTIST = "artist";
    public static final String FIELD_COMPOSER = "composer";

    private StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
    private IndexSearcher searcher;


    private JdbcTemplate db;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.db = new JdbcTemplate(dataSource);
    }
    ///home/ancalled/Documents/tmp/39/bgm-lucene
    @PostConstruct
    public void initSearcher() throws IOException {
        String indexDir = "/home/ancalled/Documents/tmp/39/bgm-lucene";
        FSDirectory index = FSDirectory.open(new File(indexDir));
        IndexReader reader = DirectoryReader.open(index);
        searcher = new IndexSearcher(reader);
    }


    @Override          //todo!
    public List<Track> searchTracks(final String field, final String value, final List<Long> catalogIds) {
        return null;


//        return query(new Action<List<Track>>() {
//
//            @Override
//            public List<Track> execute(Connection con) throws SQLException {
//
//                String catalogsPart = "";
//                if (catalogIds != null) {
//                    for (Long cat : catalogIds) {
//                        if (catalogIds.indexOf(cat) == 0) {
//                            catalogsPart = catalogsPart.concat("AND (");
//                        }
//                        catalogsPart = catalogsPart.concat("catalog_id=" + cat);
//                        if (catalogIds.indexOf(cat) != catalogIds.size() - 1) {
//                            catalogsPart = catalogsPart.concat(" OR ");
//                        } else {
//                            catalogsPart = catalogsPart.concat(")");
//                        }
//                    }
//                }
//                PreparedStatement stmt = con.prepareStatement(
//                        "SELECT * FROM composition WHERE " +
//                                field + "= ? " +
//                                catalogsPart,
//                        ResultSet.TYPE_FORWARD_ONLY,
//                        ResultSet.CONCUR_READ_ONLY
//                );
//                if ("code".equals(field)) {
//                    try {
//                        stmt.setLong(1, Long.parseLong(value));
//                    } catch (NumberFormatException ne) {
//                        System.out.println("not digits input");
//                        return Collections.emptyList();
//                    }
//                } else {
//                    stmt.setString(1, value);
//                }
//
//                stmt.setMaxRows(100);
//
//                ResultSet rs = stmt.executeQuery();
//
//                List<Track> tracks = new ArrayList<>();
//                while (rs.next()) {
//                    tracks.add(parseTrack(rs));
//                }
//                return tracks;
//            }
//        });
    }


    @Override
    public List<Track> searchTracksByName(String name) {
        return db.query(
                "SELECT * FROM composition WHERE " +
                        "name=?",
                new TrackMapper(), name
        );
    }


    @Override
    public List<SearchResultItem> searchTracksByCode(String code, List<Long> catIds) {

        String catIdsStr = catIds
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        List<Track> tracks = db.query(
                "SELECT t.*, " +
                        "c.id cat_id, " +
                        "c.name cat_name, " +
                        "c.royalty cat_royalty, " +
                        "c.platform_id cat_platform_id, " +
                        "c.tracks cat_tracks, " +
                        "c.artists cat_artists, " +
                        "c.right_type cat_right_type, " +
                        "c.color cat_color, " +
                        "p.id plat_id, " +
                        "p.name plat_name, " +
                        "p.rights plat_rights " +
                        "FROM composition t " +
                        "LEFT JOIN catalog c ON (c.id = t.catalog_id) " +
                        "LEFT JOIN platform p ON (c.platform_id = p.id) " +
                        "WHERE t.code=? " +
                        "AND t.catalog_id IN (" + catIdsStr + ")",
                new TrackAndCatalogMapper("", "cat_", "plat_"), code
        );

        return tracks
                .stream()
                .map(SearchResultItem::new)
                .collect(Collectors.toList());
    }


    @Override
    public List<Track> searchTracksByComposer(String composer) {
        return db.query(
                "SELECT * FROM composition WHERE " +
                        "composer=?",
                new TrackMapper(), composer
        );
    }


    @Override
    public List<Track> searchTracksByArtist(String artist) {
        return db.query(
                "SELECT * FROM composition WHERE " +
                        "artist=?",
                new TrackMapper(), artist
        );
    }

    @Override
    public List<Track> searchTrackByArtistLike(String artist) {
        return db.query(
                "SELECT * FROM composition WHERE " +
                        "artist LIKE ?",
                new TrackMapper(), artist
        );
    }


    @Override
    public List<SearchResultItem> getTracks(List<SearchResultItem> found, List<Long> catalogIds) {

        if (found == null || catalogIds == null) return null;
        if (found.isEmpty() || catalogIds.isEmpty()) return Collections.emptyList();

        String trackIdsStr = found
                .stream()
                .map(r -> String.valueOf(r.getTrackId()))
                .collect(Collectors.joining(", "));

        String catIdsStr = catalogIds
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        List<Track> tracks = db.query(
                "SELECT t.*," +
                        "c.id cat_id, " +
                        "c.name cat_name, " +
                        "c.royalty cat_royalty, " +
                        "c.platform_id cat_platform_id, " +
                        "c.tracks cat_tracks, " +
                        "c.artists cat_artists, " +
                        "c.right_type cat_right_type, " +
                        "c.color cat_color, " +
                        "p.id plat_id, " +
                        "p.name plat_name, " +
                        "p.rights plat_rights " +
                        " FROM composition t " +
                        "LEFT JOIN catalog c ON (t.catalog_id = c.id) " +
                        "LEFT JOIN platform p ON (c.platform_id = p.id) " +
                        "WHERE t.id IN (" + trackIdsStr + ") " +
                        "AND catalog_id IN (" + catIdsStr + ")",
                new TrackAndCatalogMapper("", "cat_", "plat_")
        );

        Map<Long, Track> trackMap = tracks
                .stream()
                .collect(Collectors.toMap(Track::getId, (t) -> t));

        found.stream()
                .forEach(sr -> sr.setTrack(trackMap.get(sr.getTrackId())));

        return found.stream()
                .filter(sr -> sr.getTrack() != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Track> getTracks(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        return db.query(
                "SELECT * FROM composition WHERE " +
                        "id IN (:ids)",
                new TrackMapper(),
                singletonMap("ids", ids)
        );
    }


    public List<SearchResultItem> search(String queryString, int limit) throws IOException, ParseException {
        log.info("Got query '" + queryString + "'");

        String[] fields = new String[]{FIELD_ARTIST, FIELD_NAME, FIELD_COMPOSER};
        QueryParser queryParser =
                new MultiFieldQueryParser(Version.LUCENE_41, fields, analyzer);

        Query query = queryParser.parse(queryString);
        TopScoreDocCollector collector = TopScoreDocCollector.create(limit, true);
        searcher.search(query, collector);

        int totalHits = collector.getTotalHits();
        TopDocs topDocs = collector.topDocs();

        ScoreDoc[] hits = topDocs.scoreDocs;

        List<SearchResultItem> result = new ArrayList<>();

        log.info("Found " + totalHits + " tracks id.");

        for (int k = 0; k < Math.min(totalHits, limit); k++) {
            ScoreDoc hit = hits[k];
            Document d = searcher.doc(hit.doc);
            long id = Long.parseLong(d.get(FIELD_ID));
            result.add(new SearchResultItem(id, hit.score));
        }

        return result;
    }


    public List<SearchResultItem> search(String artist, String authors, String composition, int limit)
            throws IOException, ParseException {

        BooleanQuery query = new BooleanQuery();
        if (composition != null && !composition.isEmpty()) {
            query.add(createTermQuery(FIELD_NAME, composition, 1), BooleanClause.Occur.MUST);
        }

        if (authors != null && artist != null) {
            BooleanQuery sq = new BooleanQuery();
            sq.add(createTermQuery(FIELD_COMPOSER, authors, 1), BooleanClause.Occur.SHOULD);
            sq.add(createTermQuery(FIELD_ARTIST, artist, 1), BooleanClause.Occur.SHOULD);
            query.add(sq, BooleanClause.Occur.MUST);

        } else if (artist != null) {
            query.add(createTermQuery(FIELD_ARTIST, artist, 1), BooleanClause.Occur.MUST);

        } else if (authors != null) {
            query.add(createTermQuery(FIELD_COMPOSER, authors, 1), BooleanClause.Occur.MUST);
        }


        TopDocs hits = searcher.search(query, limit);

        List<SearchResultItem> result = new ArrayList<>();

        for (ScoreDoc hit : hits.scoreDocs) {
            Document d = searcher.doc(hit.doc);
            long id = Long.parseLong(d.get(FIELD_ID));
            result.add(new SearchResultItem(id, hit.score));
        }

        return result;
    }

    private Query createTermQuery(String field, String value, float boost) throws ParseException {
        Query parse = new QueryParser(Version.LUCENE_41, field, analyzer).parse(QueryParser.escape(value));
        parse.setBoost(boost);
        return parse;
    }


}

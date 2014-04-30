package kz.bgm.platform.test;


import kz.bgm.platform.model.domain.SearchResultItem;
import kz.bgm.platform.model.service.SearchService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchTest {

    private SearchService searchService;

    public static final String USER_DIR = System.getProperty("user.dir");


    public SearchTest() throws IOException {
        System.out.println("USER_DIR = " + USER_DIR);

        ApplicationContext context = new FileSystemXmlApplicationContext("/" + USER_DIR + "/web/WEB-INF/mvc-dispatcher-servlet.xml");
        searchService = context.getBean(SearchService.class);
    }


    public  List<SearchResultItem> search(String artist, String track) {
        System.out.println("Searching for: " + artist + ": " + track);

        try {
            return searchService.search(artist, null, track, 10);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }


    public static void main(String[] args) throws IOException {

        final SearchTest searcher = new SearchTest();
//        searcher.search("The Script");

        List<String> lines = Files.readAllLines(Paths.get("/Users/ancalled/Documents/tmp/17", "tracks-test.txt"));
        lines.forEach(l -> {

            String[] split = l.split("; ");
            String artist = split[0];
            String track = split[1];
            List<SearchResultItem> res = searcher.search(artist, track);

//            res.stream().map(i -> i.getTrack().)



//            System.out.println(Arrays.toString(split));
        });

    }
}

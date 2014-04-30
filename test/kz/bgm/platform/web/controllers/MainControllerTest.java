package kz.bgm.platform.web.controllers;


import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MainControllerTest {


    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MainController()).build();
    }

    @Test
    public void testShowIndex() throws Exception {
        mockMvc.perform(get("/index"))
        .andExpect(status().isOk());
    }

    @Ignore
    @Test
    public void testShowCatalog() throws Exception {

    }

    @Ignore
    @Test
    public void testDownloadCatalog() throws Exception {

    }

    @Ignore
    @Test
    public void testGetRandomTracks() throws Exception {

    }
}

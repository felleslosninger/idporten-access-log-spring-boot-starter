package no.idporten.logging.access;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static no.idporten.logging.access.TestController.GET_BODY;
import static no.idporten.logging.access.TestController.JSON_BODY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

// NB: Ser at ResponseFilter køyrer og set content-length via debuggeren,
// men mockmvc-filter ser ut til uansett å legge på content-length, så får ikkje køyrt sikkeleg test eller negativ test.
public class ResponseFilterApiTest {

    @Test
    public void when_post_and_responseBody_has_content_then_return_contentLength_of_responseBody() throws Exception {
        standaloneSetup(new TestController())
                .addFilters(new ResponseFilter()).build()
                .perform(post("/test"))
                .andExpect(status().isOk())
                .andExpect(content().json(JSON_BODY))
                .andExpect(header().string("content-type", APPLICATION_JSON_VALUE))
                .andExpect(header().string("content-length", String.valueOf(JSON_BODY.length())));

    }

    @Test
    void when_get_and_responseBody_has_content_then_return_contentLength_of_responseBody() throws Exception {

        standaloneSetup(new TestController())
                .addFilters(new ResponseFilter()).build()
                .perform(MockMvcRequestBuilders.get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(GET_BODY))
                .andExpect(header().string("content-length", String.valueOf(GET_BODY.length())));
    }

    @Test
    void when_responseBody_with_empty_content_then_return_no_contentLength() throws Exception {

        standaloneSetup(new TestController())
                .addFilters(new ResponseFilter()).build()
                .perform(MockMvcRequestBuilders.get("/nobody"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("content-length"));
    }

}
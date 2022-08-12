package no.idporten.logging.access;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.ServletException;
import java.io.IOException;

import static no.idporten.logging.access.TestController.BODY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ResponseFilterTest {

    @Mock
    MockHttpServletRequest request;

    @Mock
    MockHttpServletResponse response;

    @Mock
    MockFilterChain filerChain;

    @Test
    void testDoFilterOnEmptyResponseBody() throws ServletException, IOException {
        new ResponseFilter().doFilter(request, response, filerChain);
        assertEquals(0, response.getContentLength());
    }

    @Test
    void testDoFilterOnResponseBodyWithContent() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .addFilter(new ResponseFilter())
                .build();

        mockMvc
                .perform(MockMvcRequestBuilders.get("/test"))
                .andExpect(status().isOk())
                .andExpect(content().string(BODY))
                .andExpect(header().string("content-length", String.valueOf(BODY.length())));

        mockMvc
                .perform(MockMvcRequestBuilders.post("/test"))
                .andExpect(status().isOk())
                .andExpect(header().string("content-length", String.valueOf(13)));

        mockMvc
                .perform(MockMvcRequestBuilders.get("/nobody"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("content-length"));

    }
}
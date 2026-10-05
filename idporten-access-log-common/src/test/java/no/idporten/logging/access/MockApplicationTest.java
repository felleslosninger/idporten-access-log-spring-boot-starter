package no.idporten.logging.access;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {MockApplication.class})
@ActiveProfiles("enabled")
class MockApplicationTest {

    @Test
    @DisplayName("then application context can be loaded when config is set correctly")
    void contextLoads() {
        // NOOP
    }
}

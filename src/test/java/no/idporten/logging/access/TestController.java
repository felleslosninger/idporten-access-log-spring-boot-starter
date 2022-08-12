package no.idporten.logging.access;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    protected static String BODY = "Flott response body";

    @GetMapping("/test")
    public ResponseEntity<String> testGet() {
        return ResponseEntity.ok(BODY);
    }

    @PostMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> testPost() {
        return ResponseEntity.ok("{\"my\":\"body\"}");
    }


    @GetMapping("/nobody")
    public ResponseEntity<String> nobody() {
        return ResponseEntity.ok(null);
    }

}

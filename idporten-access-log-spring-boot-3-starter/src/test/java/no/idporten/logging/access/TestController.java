package no.idporten.logging.access;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    protected static String GET_BODY = "Flott response body";
    protected static String JSON_BODY = "{\"my\":\"body\"}";

    @GetMapping("/test")
    public ResponseEntity<String> testGetWithResponsebody() {
        return ResponseEntity.ok(GET_BODY);
    }

    @PostMapping(path = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> testPostWithResponsebody() {
        return ResponseEntity.ok(JSON_BODY);
    }


    @GetMapping("/nobody")
    public ResponseEntity<String> noBody() {
        return ResponseEntity.ok(null);
    }

}

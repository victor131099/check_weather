import com.example.checkweather.CheckWeatherApiApplication;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = {CheckWeatherApiApplication.class})
@ActiveProfiles("test")
public class BlackboxTest {

    private static WireMockServer wm;

    @Autowired
    private TestRestTemplate rest;

    private final String appUrl = "/api/weather?city=London&country=gb&apiKey=test-api-key2";
    private final String weatherUpstreamApiUrl = "/?q=London,gb&appid=test-api-key2";

    @BeforeAll
    public static void before() {
        wm = new WireMockServer(options().port(3000));
        wm.start();
    }
    @AfterAll
    public static void after() {
        wm.shutdown();
    }

    @Test
    public void shouldGetWeatherDescription() {
        wm.resetAll();
        wm.stubFor(get(urlPathEqualTo("/"))
                .withQueryParam("q", equalTo("London,gb"))
                .withQueryParam("appid", matching(".*")) // Accept any API key
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody("""
            {
                "weather": [
                    { "description": "good" }
                ]
            }
        """)
                        .withStatus(HttpStatus.OK.value())));
        ResponseEntity<String> response = rest.getForEntity(appUrl, String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assertions.assertEquals(response.getBody(), "good");
    }

    @Test
    public void shouldReturn500() {
        wm.resetAll();
        wm.stubFor(get(weatherUpstreamApiUrl).willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
        ResponseEntity<String> response = rest.getForEntity(appUrl, String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Test
    public void shouldReturn429() {
        wm.resetAll();
        wm.stubFor(get(weatherUpstreamApiUrl).willReturn(aResponse().withStatus(HttpStatus.TOO_MANY_REQUESTS.value())));
        ResponseEntity<String> response = rest.getForEntity(appUrl, String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.TOO_MANY_REQUESTS);

    }
}

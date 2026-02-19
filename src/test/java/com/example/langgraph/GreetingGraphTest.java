package com.example.langgraph;

import com.example.langgraph.greeting.GreetRequest;
import com.example.langgraph.greeting.GreetResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the Greeting Graph.
 *
 * NOTE: This test requires a valid OPENAI_API_KEY environment variable.
 * Run with:  OPENAI_API_KEY=sk-xxx mvn test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GreetingGraphTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldGreetUserAndClassifySentiment() {
        // Arrange
        GreetRequest request = new GreetRequest("Ganesh");

        // Act
        ResponseEntity<GreetResponse> responseEntity = restTemplate.postForEntity(
                "http://localhost:" + port + "/greet",
                request,
                GreetResponse.class
        );

        // Assert
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        GreetResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals("Ganesh", response.userName());
        assertNotNull(response.greeting());
        assertFalse(response.greeting().isEmpty(), "Greeting should not be empty");
        assertTrue(
                response.sentiment().equals("positive") || response.sentiment().equals("neutral"),
                "Sentiment must be 'positive' or 'neutral'"
        );
        assertNotNull(response.response());
        assertFalse(response.response().isEmpty(), "Response should not be empty");

        System.out.println("=== Graph Output ===");
        System.out.println("User:      " + response.userName());
        System.out.println("Greeting:  " + response.greeting());
        System.out.println("Sentiment: " + response.sentiment());
        System.out.println("Response:  " + response.response());
    }
}

package com.example.langgraph.greeting;

/**
 * Response body from the /greet endpoint.
 */
public record GreetResponse(
        String userName,
        String greeting,
        String sentiment,
        String response
) {
}

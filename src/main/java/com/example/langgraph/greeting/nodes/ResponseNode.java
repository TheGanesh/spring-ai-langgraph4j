package com.example.langgraph.greeting.nodes;

import com.example.langgraph.greeting.GreetingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * NODE 3: Response Formatter
 *
 * This node has TWO entry points — one for "positive" sentiment, one for "neutral".
 * It formats the final response string and writes it into the state.
 *
 * Flow: (conditional edge) → [celebrate or encourage] → END
 */
@Component
public class ResponseNode {

    private static final Logger log = LoggerFactory.getLogger(ResponseNode.class);

    /**
     * Called when sentiment is "positive".
     * Wraps the greeting with a celebratory note.
     */
    public Map<String, Object> celebrateResponse(GreetingState state) {
        log.debug("ResponseNode (celebrate): Formatting positive response");

        String response = String.format(
                "🎉 Great news! Here's your greeting for %s:\n\n%s\n\n✨ The sentiment is glowing POSITIVE!",
                state.userName(),
                state.greeting()
        );

        return Map.of("response", response);
    }

    /**
     * Called when sentiment is "neutral".
     * Wraps the greeting with an encouraging note.
     */
    public Map<String, Object> encourageResponse(GreetingState state) {
        log.debug("ResponseNode (encourage): Formatting neutral response");

        String response = String.format(
                "👋 Here's your greeting for %s:\n\n%s\n\n💪 The sentiment is neutral — still a solid greeting!",
                state.userName(),
                state.greeting()
        );

        return Map.of("response", response);
    }
}

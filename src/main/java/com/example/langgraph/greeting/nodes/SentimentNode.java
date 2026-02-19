package com.example.langgraph.greeting.nodes;

import com.example.langgraph.greeting.GreetingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * NODE 2: Sentiment Classifier
 *
 * This node takes the greeting from state,
 * calls the LLM to classify its sentiment as "positive" or "neutral",
 * and writes the sentiment back into the state.
 *
 * Flow: GreetingNode → [this node] → (conditional edge) → ResponseNode
 */
@Component
public class SentimentNode {

    private static final Logger log = LoggerFactory.getLogger(SentimentNode.class);

    private final ChatClient chatClient;

    public SentimentNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Process function — classifies the greeting sentiment.
     *
     * @param state the current graph state (must contain "greeting")
     * @return a map with the "sentiment" field set to "positive" or "neutral"
     */
    public Map<String, Object> process(GreetingState state) {
        String greeting = state.greeting();
        log.debug("SentimentNode: Classifying sentiment of: {}", greeting);

        String sentiment = chatClient.prompt()
                .user("Classify the sentiment of the following text as exactly one word: "
                        + "'positive' or 'neutral'. Only respond with that single word.\n\n"
                        + "Text: \"" + greeting + "\"")
                .call()
                .content()
                .trim()
                .toLowerCase();

        // Normalize: anything that's not exactly "positive" defaults to "neutral"
        if (!sentiment.equals("positive")) {
            sentiment = "neutral";
        }

        log.debug("SentimentNode: Classified sentiment as: {}", sentiment);

        return Map.of("sentiment", sentiment);
    }
}

package com.example.langgraph.greeting.nodes;

import com.example.langgraph.greeting.GreetingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * NODE 1: Greeting Generator
 *
 * This node receives the user's name from the state,
 * calls the LLM (via Spring AI ChatClient) to generate a personalized greeting,
 * and writes the greeting back into the state.
 *
 * Flow: START → [this node] → SentimentNode
 */
@Component
public class GreetingNode {

    private static final Logger log = LoggerFactory.getLogger(GreetingNode.class);

    private final ChatClient chatClient;

    public GreetingNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Process function — this is what gets registered as a node in the graph.
     *
     * @param state the current graph state
     * @return a map of state updates (only the fields that changed)
     */
    public Map<String, Object> process(GreetingState state) {
        String userName = state.userName();
        log.debug("GreetingNode: Generating greeting for '{}'", userName);

        String greeting = chatClient.prompt()
                .user("""
                        Generate a warm, personalized greeting for a person named %s. \
                        Keep it to 1-2 sentences. Be creative and friendly.\
                        """.formatted(userName))
                .call()
                .content();

        log.debug("GreetingNode: Generated greeting: {}", greeting);

        // Return ONLY the fields that this node updates
        return Map.of("greeting", greeting);
    }
}

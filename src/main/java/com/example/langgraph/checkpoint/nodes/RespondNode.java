package com.example.langgraph.checkpoint.nodes;

import com.example.langgraph.checkpoint.CheckpointState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * NODE: Respond
 *
 * Uses full conversation history to generate a context-aware response.
 */
@Component
public class RespondNode implements Function<CheckpointState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(RespondNode.class);

    private final ChatClient chatClient;

    public RespondNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(CheckpointState state) {
        List<String> history = state.history();
        String historyText = String.join("\n", history);

        log.debug("Responding with {} history entries", history.size());

        String response = chatClient.prompt()
                .user("You are a helpful assistant. Here is the conversation history:\n\n"
                        + historyText
                        + "\n\nRespond to the latest user message. Keep it concise (1-2 sentences).")
                .call()
                .content();

        // Append AI response to history too
        return Map.of(
                "currentOutput", response,
                "history", List.of("Assistant: " + response)
        );
    }
}

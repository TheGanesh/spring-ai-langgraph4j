package com.example.langgraph.streaming.nodes;

import com.example.langgraph.streaming.StreamingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Node 3: Polish and finalize the content.
 */
@Component
public class PolishNode implements Function<StreamingState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(PolishNode.class);

    private final ChatClient chatClient;

    public PolishNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(StreamingState state) {
        log.info("✨ Step 3: Polishing final content");

        String polished = chatClient.prompt()
                .user("Polish and improve this article. Make it engaging and professional. " +
                      "Add a catchy title:\n\n" + state.content())
                .call()
                .content();

        return Map.of(
                "polished", polished,
                "steps", List.of("polish_complete")
        );
    }
}

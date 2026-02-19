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
 * Node 1: Create Outline from topic.
 */
@Component
public class CreateOutlineNode implements Function<StreamingState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(CreateOutlineNode.class);

    private final ChatClient chatClient;

    public CreateOutlineNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(StreamingState state) {
        log.info("📋 Step 1: Creating outline for '{}'", state.topic());

        String outline = chatClient.prompt()
                .user("Create a brief 3-point outline for a short article about: " + state.topic()
                      + "\nFormat as numbered points.")
                .call()
                .content();

        return Map.of(
                "outline", outline,
                "steps", List.of("outline_complete")
        );
    }
}

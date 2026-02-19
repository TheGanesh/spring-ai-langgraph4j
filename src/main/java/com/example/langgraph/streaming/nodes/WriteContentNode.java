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
 * Node 2: Write Content from outline.
 */
@Component
public class WriteContentNode implements Function<StreamingState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(WriteContentNode.class);

    private final ChatClient chatClient;

    public WriteContentNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(StreamingState state) {
        log.info("✍️ Step 2: Writing content from outline");

        String content = chatClient.prompt()
                .user("Write a short article (3-4 sentences per point) based on this outline:\n\n"
                      + state.outline())
                .call()
                .content();

        return Map.of(
                "content", content,
                "steps", List.of("content_complete")
        );
    }
}

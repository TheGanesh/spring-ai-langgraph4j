package com.example.langgraph.subgraph.nodes;

import com.example.langgraph.subgraph.SubGraphState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Inner Subgraph Node 2: Generate Summary from key points.
 */
@Component
public class GenerateSummaryNode implements Function<SubGraphState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(GenerateSummaryNode.class);

    private final ChatClient chatClient;

    public GenerateSummaryNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(SubGraphState state) {
        String keyPoints = state.keyPoints();
        log.info("  [Subgraph] Generating summary from key points...");

        String summary = chatClient.prompt()
                .user("Write a concise 2-3 sentence summary based on these key points:\n\n" + keyPoints)
                .call()
                .content();

        return Map.of("summary", summary);
    }
}

package com.example.langgraph.subgraph.nodes;

import com.example.langgraph.subgraph.SubGraphState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Inner Subgraph Node 1: Extract Key Points from document.
 */
@Component
public class ExtractKeyPointsNode implements Function<SubGraphState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ExtractKeyPointsNode.class);

    private final ChatClient chatClient;

    public ExtractKeyPointsNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(SubGraphState state) {
        String doc = state.document();
        log.info("  [Subgraph] Extracting key points...");

        String keyPoints = chatClient.prompt()
                .user("Extract 3-5 key points from this document as a bullet list:\n\n" + doc)
                .call()
                .content();

        return Map.of("keyPoints", keyPoints);
    }
}

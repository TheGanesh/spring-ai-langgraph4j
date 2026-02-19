package com.example.langgraph.subgraph.nodes;

import com.example.langgraph.subgraph.SubGraphState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Outer Graph Node 1: Classify Document into a category.
 */
@Component
public class ClassifyDocumentNode implements Function<SubGraphState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ClassifyDocumentNode.class);

    private final ChatClient chatClient;

    public ClassifyDocumentNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(SubGraphState state) {
        String doc = state.document();
        log.info("[Outer] Classifying document...");

        String category = chatClient.prompt()
                .user("Classify this document into one category (e.g., Technology, Science, Business, Health). " +
                      "Respond with just the category name:\n\n" + doc)
                .call()
                .content();

        return Map.of("category", category != null ? category.trim() : "Unknown");
    }
}

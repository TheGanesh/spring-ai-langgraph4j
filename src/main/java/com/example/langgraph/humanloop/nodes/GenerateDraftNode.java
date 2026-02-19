package com.example.langgraph.humanloop.nodes;

import com.example.langgraph.humanloop.HumanLoopState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * NODE: Generate Draft
 *
 * LLM creates draft content that needs human review.
 */
@Component
public class GenerateDraftNode implements Function<HumanLoopState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(GenerateDraftNode.class);

    private final ChatClient chatClient;

    public GenerateDraftNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(HumanLoopState state) {
        String topic = state.topic();
        log.info("📝 Generating draft for topic: {}", topic);

        String draft = chatClient.prompt()
                .user("Write a short blog post draft (3-4 sentences) about: " + topic)
                .call()
                .content();

        log.info("📝 Draft generated, awaiting human review");
        return Map.of("generatedContent", draft);
    }
}

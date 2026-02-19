package com.example.langgraph.multiagent.nodes;

import com.example.langgraph.multiagent.MultiAgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Writer Agent: Takes research and writes polished content.
 */
@Component
public class WriterAgentNode implements Function<MultiAgentState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(WriterAgentNode.class);

    private final ChatClient chatClient;

    public WriterAgentNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(MultiAgentState state) {
        String task = state.task();
        String research = state.researchData();
        log.info("✍️ Writer: Creating content based on research");

        String content = chatClient.prompt()
                .user("You are a professional writer. Using the research below, write a short, "
                      + "engaging article (3-4 paragraphs) about: " + task + "\n\n"
                      + "Research:\n" + research)
                .call()
                .content();

        log.info("✍️ Writer: Done ({} chars)", content != null ? content.length() : 0);

        return Map.of(
                "writtenContent", content != null ? content : "",
                "finalOutput", content != null ? content : "",
                "agentLog", List.of("writer: wrote " + (content != null ? content.length() : 0) + " chars")
        );
    }
}

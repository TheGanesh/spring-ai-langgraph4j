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
 * Researcher Agent: Gathers information and facts about a topic.
 */
@Component
public class ResearcherAgentNode implements Function<MultiAgentState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ResearcherAgentNode.class);

    private final ChatClient chatClient;

    public ResearcherAgentNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(MultiAgentState state) {
        String task = state.task();
        log.info("🔍 Researcher: Gathering information about '{}'", task);

        String research = chatClient.prompt()
                .user("You are a research agent. Research the following topic and provide "
                      + "key facts, statistics, and interesting points (5-7 bullet points):\n\n"
                      + "Topic: " + task)
                .call()
                .content();

        log.info("🔍 Researcher: Done ({} chars)", research != null ? research.length() : 0);

        return Map.of(
                "researchData", research != null ? research : "",
                "agentLog", List.of("researcher: gathered " + (research != null ? research.length() : 0) + " chars of research")
        );
    }
}

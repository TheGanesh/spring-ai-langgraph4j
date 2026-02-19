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
 * Supervisor Agent: Routes work to the right agent, decides when done.
 */
@Component
public class SupervisorAgentNode implements Function<MultiAgentState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(SupervisorAgentNode.class);
    private static final int MAX_ITERATIONS = 5;

    private final ChatClient chatClient;

    public SupervisorAgentNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(MultiAgentState state) {
        String task = state.task();
        String researchData = state.researchData();
        String writtenContent = state.writtenContent();
        int iterations = state.iterations();

        log.info("🎯 Supervisor (iteration {}): Deciding next step...", iterations);

        // Safety: prevent infinite loops
        if (iterations >= MAX_ITERATIONS) {
            log.warn("Max iterations reached, finishing");
            return Map.of(
                    "nextAgent", "FINISH",
                    "agentLog", List.of("supervisor: Max iterations reached, finishing")
            );
        }

        String prompt = """
            You are a supervisor managing a team of agents for this task: %s
            
            Current state:
            - Research data: %s
            - Written content: %s
            
            Available agents:
              researcher — gathers information and facts
              writer — writes polished content from research
            
            Decide the NEXT step. Respond with EXACTLY one word:
              'researcher' — if more research is needed
              'writer' — if we have enough research and need content written
              'FINISH' — if the written content is complete and satisfactory
            
            Rules:
            - If no research exists yet, choose 'researcher'
            - If research exists but no written content, choose 'writer'
            - If both exist, choose 'FINISH'
            """.formatted(
            task,
            researchData.isEmpty() ? "(none yet)" : researchData.substring(0, Math.min(200, researchData.length())),
            writtenContent.isEmpty() ? "(none yet)" : writtenContent.substring(0, Math.min(200, writtenContent.length()))
        );

        String decision = chatClient.prompt().user(prompt).call().content();
        String nextAgent = "FINISH"; // default safe
        if (decision != null) {
            decision = decision.trim().toLowerCase();
            if (decision.contains("researcher")) nextAgent = "researcher";
            else if (decision.contains("writer")) nextAgent = "writer";
            else nextAgent = "FINISH";
        }

        log.info("🎯 Supervisor decided: {}", nextAgent);

        return Map.of(
                "nextAgent", nextAgent,
                "iterations", iterations + 1,
                "agentLog", List.of("supervisor: decided → " + nextAgent)
        );
    }
}

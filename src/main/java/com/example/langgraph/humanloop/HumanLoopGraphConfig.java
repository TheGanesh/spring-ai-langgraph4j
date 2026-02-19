package com.example.langgraph.humanloop;

import com.example.langgraph.humanloop.nodes.FinalResponseNode;
import com.example.langgraph.humanloop.nodes.GenerateDraftNode;
import com.example.langgraph.humanloop.nodes.HumanReviewNode;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * CONCEPT 2: HUMAN-IN-THE-LOOP
 *
 * This graph pauses execution for human approval before finalizing.
 *
 * The Pattern:
 *   1. LLM generates draft content
 *   2. Graph INTERRUPTS (pauses) before the "human_review" node
 *   3. Human reviews and approves/rejects via API
 *   4. Graph resumes with the human's decision
 *   5. Final response is generated based on approval
 *
 * Graph:
 *   START → generate_draft → [INTERRUPT] → human_review → final_response → END
 *
 * Key API:
 *   - CompileConfig.builder().interruptBefore("human_review") → pauses before that node
 *   - MemorySaver → required to persist state while paused
 *   - RunnableConfig with threadId → identifies the paused conversation
 */
@Configuration
public class HumanLoopGraphConfig {

    private static final Logger log = LoggerFactory.getLogger(HumanLoopGraphConfig.class);

    private final GenerateDraftNode generateDraftNode;
    private final HumanReviewNode humanReviewNode;
    private final FinalResponseNode finalResponseNode;

    public HumanLoopGraphConfig(GenerateDraftNode generateDraftNode,
                                 HumanReviewNode humanReviewNode,
                                 FinalResponseNode finalResponseNode) {
        this.generateDraftNode = generateDraftNode;
        this.humanReviewNode = humanReviewNode;
        this.finalResponseNode = finalResponseNode;
    }

    // ───── Shared MemorySaver (needed for interrupt) ─────
    @Bean
    public MemorySaver humanLoopMemorySaver() {
        return new MemorySaver();
    }

    // ───── GRAPH WIRING ─────
    @Bean
    public CompiledGraph<HumanLoopState> humanLoopGraph(MemorySaver humanLoopMemorySaver) throws Exception {
        log.info("Building Human-in-the-Loop Graph...");

        StateGraph<HumanLoopState> workflow = new StateGraph<>(HumanLoopState::new)

                .addNode("generate_draft", node_async(generateDraftNode::apply))
                .addNode("human_review", node_async(humanReviewNode::apply))
                .addNode("final_response", node_async(finalResponseNode::apply))

                .addEdge(START, "generate_draft")
                .addEdge("generate_draft", "human_review")
                .addEdge("human_review", "final_response")
                .addEdge("final_response", END);

        // Compile WITH interrupt — pauses BEFORE "human_review" node
        return workflow.compile(
                CompileConfig.builder()
                        .checkpointSaver(humanLoopMemorySaver)
                        .interruptBefore("human_review")
                        .build()
        );
    }
}

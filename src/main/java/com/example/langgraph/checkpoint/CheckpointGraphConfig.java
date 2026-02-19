package com.example.langgraph.checkpoint;

import com.example.langgraph.checkpoint.nodes.BuildContextNode;
import com.example.langgraph.checkpoint.nodes.RespondNode;
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
 * CONCEPT 3: PERSISTENCE / CHECKPOINTING
 *
 * This graph demonstrates saving and resuming conversation state.
 *
 * The Pattern:
 *   - Each API call includes a threadId
 *   - MemorySaver persists state between calls
 *   - The AI remembers previous turns in the same thread
 *   - Different threadIds = different conversations
 *
 * How it works:
 *   Turn 1: User says "My name is Ganesh"  → AI responds, state saved
 *   Turn 2: User says "What's my name?"    → AI remembers "Ganesh" from history
 *
 * Graph:
 *   START → build_context → respond → END
 */
@Configuration
public class CheckpointGraphConfig {

    private static final Logger log = LoggerFactory.getLogger(CheckpointGraphConfig.class);

    private final BuildContextNode buildContextNode;
    private final RespondNode respondNode;

    public CheckpointGraphConfig(BuildContextNode buildContextNode, RespondNode respondNode) {
        this.buildContextNode = buildContextNode;
        this.respondNode = respondNode;
    }

    // ───── Shared MemorySaver ─────
    @Bean
    public MemorySaver checkpointMemorySaver() {
        return new MemorySaver();
    }

    // ───── GRAPH WIRING ─────
    @Bean
    public CompiledGraph<CheckpointState> checkpointGraph(MemorySaver checkpointMemorySaver) throws Exception {
        log.info("Building Checkpoint Graph...");

        StateGraph<CheckpointState> workflow = new StateGraph<>(
                CheckpointState.SCHEMA, CheckpointState::new)

                .addNode("build_context", node_async(buildContextNode::apply))
                .addNode("respond", node_async(respondNode::apply))

                .addEdge(START, "build_context")
                .addEdge("build_context", "respond")
                .addEdge("respond", END);

        // Compile WITH checkpointing — state persists between invocations
        return workflow.compile(
                CompileConfig.builder()
                        .checkpointSaver(checkpointMemorySaver)
                        .build()
        );
    }
}

package com.example.langgraph.streaming;

import com.example.langgraph.streaming.nodes.CreateOutlineNode;
import com.example.langgraph.streaming.nodes.PolishNode;
import com.example.langgraph.streaming.nodes.WriteContentNode;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * CONCEPT 5: STREAMING
 *
 * Demonstrates streaming graph execution via Server-Sent Events (SSE).
 *
 * The Pattern:
 *   - The graph has multiple nodes that run sequentially
 *   - As each node completes, its output is streamed to the client in real-time
 *   - The client sees progress as: "outline ready" → "content ready" → "polished ready"
 *   - Uses Spring MVC's SseEmitter (no WebFlux needed)
 *
 * Graph (3-step content pipeline):
 *   START → create_outline → write_content → polish → END
 *
 * Each node's completion is streamed as an SSE event to the client.
 */
@Configuration
public class StreamingGraphConfig {

    private static final Logger log = LoggerFactory.getLogger(StreamingGraphConfig.class);

    private final CreateOutlineNode createOutlineNode;
    private final WriteContentNode writeContentNode;
    private final PolishNode polishNode;

    public StreamingGraphConfig(CreateOutlineNode createOutlineNode,
                                 WriteContentNode writeContentNode,
                                 PolishNode polishNode) {
        this.createOutlineNode = createOutlineNode;
        this.writeContentNode = writeContentNode;
        this.polishNode = polishNode;
    }

    // ───── GRAPH WIRING ─────
    @Bean
    public CompiledGraph<StreamingState> streamingGraph() throws Exception {
        log.info("Building Streaming Graph...");

        return new StateGraph<>(StreamingState.SCHEMA, StreamingState::new)
                .addNode("create_outline", node_async(createOutlineNode::apply))
                .addNode("write_content", node_async(writeContentNode::apply))
                .addNode("polish", node_async(polishNode::apply))
                .addEdge(START, "create_outline")
                .addEdge("create_outline", "write_content")
                .addEdge("write_content", "polish")
                .addEdge("polish", END)
                .compile();
    }
}

package com.example.langgraph.subgraph;

import com.example.langgraph.subgraph.nodes.ClassifyDocumentNode;
import com.example.langgraph.subgraph.nodes.ExtractKeyPointsNode;
import com.example.langgraph.subgraph.nodes.FormatOutputNode;
import com.example.langgraph.subgraph.nodes.GenerateSummaryNode;
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
 * CONCEPT 4: SUBGRAPHS
 *
 * A subgraph is a complete graph nested INSIDE another graph as a single node.
 *
 * Why use subgraphs?
 *   - Encapsulate complex logic into reusable components
 *   - Keep the outer graph clean and readable
 *   - Each subgraph can be developed and tested independently
 *
 * Architecture:
 *
 *   OUTER GRAPH:
 *     START → classify → [SUBGRAPH: summarize] → format → END
 *
 *   INNER SUBGRAPH (summarize):
 *     START → extract_key_points → generate_summary → END
 *
 * The inner subgraph runs as a single "node" in the outer graph.
 * State flows through both graphs seamlessly.
 */
@Configuration
public class SubGraphConfig {

    private static final Logger log = LoggerFactory.getLogger(SubGraphConfig.class);

    private final ExtractKeyPointsNode extractKeyPointsNode;
    private final GenerateSummaryNode generateSummaryNode;
    private final ClassifyDocumentNode classifyDocumentNode;
    private final FormatOutputNode formatOutputNode;

    public SubGraphConfig(ExtractKeyPointsNode extractKeyPointsNode,
                          GenerateSummaryNode generateSummaryNode,
                          ClassifyDocumentNode classifyDocumentNode,
                          FormatOutputNode formatOutputNode) {
        this.extractKeyPointsNode = extractKeyPointsNode;
        this.generateSummaryNode = generateSummaryNode;
        this.classifyDocumentNode = classifyDocumentNode;
        this.formatOutputNode = formatOutputNode;
    }

    // ═══════════════════════════════
    //  BUILD INNER SUBGRAPH
    // ═══════════════════════════════
    private StateGraph<SubGraphState> buildSummarizationSubgraph() throws Exception {
        return new StateGraph<>(SubGraphState::new)
                .addNode("extract_key_points", node_async(extractKeyPointsNode::apply))
                .addNode("generate_summary", node_async(generateSummaryNode::apply))
                .addEdge(START, "extract_key_points")
                .addEdge("extract_key_points", "generate_summary")
                .addEdge("generate_summary", END);
    }

    // ═══════════════════════════════
    //  BUILD OUTER GRAPH (with subgraph)
    // ═══════════════════════════════
    @Bean
    public CompiledGraph<SubGraphState> subGraphPipeline() throws Exception {
        log.info("Building Subgraph Pipeline...");

        StateGraph<SubGraphState> summarizationSubgraph = buildSummarizationSubgraph();

        StateGraph<SubGraphState> outerWorkflow = new StateGraph<>(SubGraphState::new)
                .addNode("classify", node_async(classifyDocumentNode::apply))
                // Add the inner graph as a subgraph node
                .addSubgraph("summarize", summarizationSubgraph)
                .addNode("format", node_async(formatOutputNode::apply))

                .addEdge(START, "classify")
                .addEdge("classify", "summarize")
                .addEdge("summarize", "format")
                .addEdge("format", END);

        return outerWorkflow.compile();
    }
}

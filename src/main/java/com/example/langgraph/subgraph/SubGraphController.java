package com.example.langgraph.subgraph;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for the Subgraph demo.
 *
 * POST /subgraph/analyze  { "document": "..." }
 * → Classifies the document, runs summarization subgraph, formats output
 */
@RestController
@RequestMapping("/subgraph")
public class SubGraphController {

    private static final Logger log = LoggerFactory.getLogger(SubGraphController.class);

    private final CompiledGraph<SubGraphState> subGraphPipeline;

    public SubGraphController(CompiledGraph<SubGraphState> subGraphPipeline) {
        this.subGraphPipeline = subGraphPipeline;
    }

    public record AnalyzeRequest(String document) {}
    public record AnalyzeResponse(String category, String keyPoints, String summary, String finalOutput) {}

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(@RequestBody AnalyzeRequest request) throws Exception {
        log.info("Subgraph: Analyzing document ({} chars)", request.document().length());

        Map<String, Object> input = Map.of("document", request.document());

        SubGraphState finalState = null;
        for (NodeOutput<SubGraphState> output : subGraphPipeline.stream(input)) {
            String nodeName = output.node();
            boolean isSub = output.isSubGraph();
            log.info("  {} Node '{}' completed", isSub ? "↳ [subgraph]" : "✓", nodeName);
            finalState = output.state();
        }

        if (finalState == null) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(new AnalyzeResponse(
                finalState.category(),
                finalState.keyPoints(),
                finalState.summary(),
                finalState.finalOutput()
        ));
    }
}

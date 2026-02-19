package com.example.langgraph.toolcalling;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller for the Tool Calling demo.
 *
 * POST /tools/ask  { "query": "What's the weather in Tokyo?" }
 * POST /tools/ask  { "query": "What is 42 * 17?" }
 * POST /tools/ask  { "query": "Tell me a joke" }  ← no tool needed
 */
@RestController
@RequestMapping("/tools")
public class ToolCallingController {

    private static final Logger log = LoggerFactory.getLogger(ToolCallingController.class);

    private final CompiledGraph<ToolCallingState> toolCallingGraph;

    public ToolCallingController(CompiledGraph<ToolCallingState> toolCallingGraph) {
        this.toolCallingGraph = toolCallingGraph;
    }

    public record AskRequest(String query) {}
    public record AskResponse(String query, String answer, java.util.List<String> messages) {}

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@RequestBody AskRequest request) throws Exception {
        log.info("Tool Calling request: {}", request.query());

        Map<String, Object> input = Map.of("userQuery", request.query());

        ToolCallingState finalState = null;
        for (NodeOutput<ToolCallingState> output : toolCallingGraph.stream(input)) {
            log.info("  ✓ Node '{}' completed", output.node());
            finalState = output.state();
        }

        if (finalState == null) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(new AskResponse(
                request.query(),
                finalState.finalAnswer(),
                finalState.messages()
        ));
    }
}

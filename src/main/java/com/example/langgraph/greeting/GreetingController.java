package com.example.langgraph.greeting;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST Controller — exposes the graph as an HTTP API.
 *
 * POST /greet  { "name": "Ganesh" }
 *
 * Internally:
 *   1. Creates initial state with userName
 *   2. Invokes the compiled graph
 *   3. Streams through all node outputs (logging each step)
 *   4. Returns the final state as a GreetResponse
 */
@RestController
public class GreetingController {

    private static final Logger log = LoggerFactory.getLogger(GreetingController.class);

    private final CompiledGraph<GreetingState> greetingGraph;

    public GreetingController(CompiledGraph<GreetingState> greetingGraph) {
        this.greetingGraph = greetingGraph;
    }

    @PostMapping("/greet")
    public ResponseEntity<GreetResponse> greet(@RequestBody GreetRequest request) throws Exception {
        log.info("Received greeting request for: {}", request.name());

        // 1. Create the initial state with the user's name
        Map<String, Object> initialState = Map.of("userName", request.name());

        // 2. Stream through the graph — each iteration is one node completing
        GreetingState finalState = null;
        for (NodeOutput<GreetingState> output : greetingGraph.stream(initialState)) {
            log.info("  ✓ Node '{}' completed", output.node());
            finalState = output.state();
        }

        // 3. Build and return the response from the final state
        if (finalState == null) {
            return ResponseEntity.internalServerError().build();
        }

        GreetResponse response = new GreetResponse(
                finalState.userName(),
                finalState.greeting(),
                finalState.sentiment(),
                finalState.response()
        );

        log.info("Graph execution complete. Sentiment: {}", response.sentiment());
        return ResponseEntity.ok(response);
    }
}

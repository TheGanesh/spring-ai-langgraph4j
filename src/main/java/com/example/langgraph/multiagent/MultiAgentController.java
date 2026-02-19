package com.example.langgraph.multiagent;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for the Multi-Agent demo.
 *
 * POST /multi-agent/run  { "task": "Write about the future of renewable energy" }
 *
 * The supervisor orchestrates researcher + writer agents to complete the task.
 * Response includes the agent execution log showing the collaboration flow.
 */
@RestController
@RequestMapping("/multi-agent")
public class MultiAgentController {

    private static final Logger log = LoggerFactory.getLogger(MultiAgentController.class);

    private final CompiledGraph<MultiAgentState> multiAgentGraph;

    public MultiAgentController(CompiledGraph<MultiAgentState> multiAgentGraph) {
        this.multiAgentGraph = multiAgentGraph;
    }

    public record TaskRequest(String task) {}
    public record TaskResponse(String task, String finalOutput, List<String> agentLog, int iterations) {}

    @PostMapping("/run")
    public ResponseEntity<TaskResponse> run(@RequestBody TaskRequest request) throws Exception {
        log.info("Multi-Agent: Starting task '{}'", request.task());

        Map<String, Object> input = Map.of("task", request.task());

        MultiAgentState finalState = null;
        for (NodeOutput<MultiAgentState> output : multiAgentGraph.stream(input)) {
            log.info("  ✓ Agent '{}' completed", output.node());
            finalState = output.state();
        }

        if (finalState == null) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(new TaskResponse(
                request.task(),
                finalState.finalOutput(),
                finalState.agentLog(),
                finalState.iterations()
        ));
    }
}

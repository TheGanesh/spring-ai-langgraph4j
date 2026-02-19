package com.example.langgraph.checkpoint;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for the Checkpointing demo.
 *
 * Shows multi-turn conversation with persistent memory.
 *
 * Turn 1: POST /checkpoint/chat  { "threadId": "abc", "message": "My name is Ganesh" }
 *         → AI responds, state saved under thread "abc"
 *
 * Turn 2: POST /checkpoint/chat  { "threadId": "abc", "message": "What's my name?" }
 *         → AI remembers from thread "abc" and responds "Your name is Ganesh"
 *
 * Turn 3: POST /checkpoint/chat  { "threadId": "xyz", "message": "What's my name?" }
 *         → Different thread — AI does NOT know the name
 */
@RestController
@RequestMapping("/checkpoint")
public class CheckpointController {

    private static final Logger log = LoggerFactory.getLogger(CheckpointController.class);

    private final CompiledGraph<CheckpointState> checkpointGraph;

    public CheckpointController(CompiledGraph<CheckpointState> checkpointGraph) {
        this.checkpointGraph = checkpointGraph;
    }

    public record ChatRequest(String threadId, String message) {}
    public record ChatResponse(String threadId, String response, List<String> history, int turnCount) {}

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) throws Exception {
        log.info("Checkpoint chat — threadId={}, message='{}'", request.threadId(), request.message());

        // Config with threadId — this is how the saver knows which conversation to load/save
        RunnableConfig config = RunnableConfig.builder()
                .threadId(request.threadId())
                .build();

        Map<String, Object> input = Map.of("currentInput", request.message());

        CheckpointState finalState = null;
        for (NodeOutput<CheckpointState> output : checkpointGraph.stream(input, config)) {
            log.info("  ✓ Node '{}' completed", output.node());
            finalState = output.state();
        }

        if (finalState == null) {
            return ResponseEntity.internalServerError().build();
        }

        List<String> history = finalState.history();
        return ResponseEntity.ok(new ChatResponse(
                request.threadId(),
                finalState.currentOutput(),
                history,
                history.size() / 2  // each turn = 1 user + 1 assistant
        ));
    }
}

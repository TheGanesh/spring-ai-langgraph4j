package com.example.langgraph.humanloop;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.state.StateSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for the Human-in-the-Loop demo.
 *
 * Step 1: POST /human-loop/submit   { "topic": "AI in healthcare" }
 *         → Returns threadId + draft content (graph pauses before human_review)
 *
 * Step 2: POST /human-loop/review   { "threadId": "...", "approved": true, "feedback": "Looks good!" }
 *         → Resumes the graph with human decision, returns final result
 */
@RestController
@RequestMapping("/human-loop")
public class HumanLoopController {

    private static final Logger log = LoggerFactory.getLogger(HumanLoopController.class);

    private final CompiledGraph<HumanLoopState> humanLoopGraph;

    public HumanLoopController(CompiledGraph<HumanLoopState> humanLoopGraph) {
        this.humanLoopGraph = humanLoopGraph;
    }

    // ─── Request/Response DTOs ───
    public record SubmitRequest(String topic) {}
    public record SubmitResponse(String threadId, String draft, String status) {}

    public record ReviewRequest(String threadId, boolean approved, String feedback) {}
    public record ReviewResponse(String finalResponse, String status) {}

    // ─── Step 1: Submit topic → graph runs and PAUSES ───
    @PostMapping("/submit")
    public ResponseEntity<SubmitResponse> submit(@RequestBody SubmitRequest request) throws Exception {
        String threadId = UUID.randomUUID().toString();
        log.info("Human-Loop: Starting graph for topic='{}', threadId={}", request.topic(), threadId);

        // Create config with thread ID (required for checkpointing)
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();

        Map<String, Object> input = Map.of("topic", request.topic());

        // Stream the graph — it will PAUSE before "human_review" node
        HumanLoopState pausedState = null;
        for (NodeOutput<HumanLoopState> output : humanLoopGraph.stream(input, config)) {
            log.info("  ✓ Node '{}' completed", output.node());
            pausedState = output.state();
        }

        if (pausedState == null) {
            return ResponseEntity.internalServerError().build();
        }

        log.info("Graph PAUSED — awaiting human review for threadId={}", threadId);

        return ResponseEntity.ok(new SubmitResponse(
                threadId,
                pausedState.generatedContent(),
                "AWAITING_REVIEW"
        ));
    }

    // ─── Step 2: Human reviews → graph RESUMES ───
    @PostMapping("/review")
    public ResponseEntity<ReviewResponse> review(@RequestBody ReviewRequest request) throws Exception {
        log.info("Human-Loop: Resuming threadId={}, approved={}", request.threadId(), request.approved());

        // Config to resume the paused thread
        RunnableConfig config = RunnableConfig.builder()
                .threadId(request.threadId())
                .build();

        // Get the current snapshot to find the paused state
        StateSnapshot<HumanLoopState> snapshot = humanLoopGraph.getState(config);
        log.info("  Snapshot next node: {}", snapshot.next());

        // Inject the human's decision into the state and resume
        Map<String, Object> humanDecision = Map.of(
                "approved", request.approved(),
                "feedback", request.feedback() != null ? request.feedback() : ""
        );

        // Update the state with human input
        humanLoopGraph.updateState(config, humanDecision);

        // Resume execution — pass null input to continue from checkpoint
        HumanLoopState finalState = null;
        for (NodeOutput<HumanLoopState> output : humanLoopGraph.stream(null, config)) {
            log.info("  ✓ Node '{}' completed (resumed)", output.node());
            finalState = output.state();
        }

        if (finalState == null) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(new ReviewResponse(
                finalState.finalResponse(),
                request.approved() ? "PUBLISHED" : "REJECTED"
        ));
    }
}

package com.example.langgraph.streaming;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.NodeOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * REST Controller for the Streaming demo.
 *
 * GET /streaming/write?topic=Artificial+Intelligence
 *
 * Returns Server-Sent Events (SSE) — each event is a node completing:
 *   event: create_outline   data: { outline text }
 *   event: write_content    data: { content text }
 *   event: polish           data: { final polished text }
 *
 * The client sees progress in real-time as each step completes.
 */
@RestController
@RequestMapping("/streaming")
public class StreamingController {

    private static final Logger log = LoggerFactory.getLogger(StreamingController.class);

    private final CompiledGraph<StreamingState> streamingGraph;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public StreamingController(CompiledGraph<StreamingState> streamingGraph) {
        this.streamingGraph = streamingGraph;
    }

    /**
     * SSE endpoint — streams graph progress as events.
     *
     * Each event contains the node name and the latest state field for that step.
     * The client receives real-time updates as each node completes.
     */
    @GetMapping(value = "/write", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamWrite(@RequestParam String topic) {
        SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout

        executor.execute(() -> {
            try {
                log.info("Streaming: Starting content pipeline for '{}'", topic);
                Map<String, Object> input = Map.of("topic", topic);

                for (NodeOutput<StreamingState> output : streamingGraph.stream(input)) {
                    String nodeName = output.node();

                    // Skip START node
                    if (output.isSTART()) continue;

                    StreamingState state = output.state();

                    // Determine what data to send based on which node completed
                    String data = switch (nodeName) {
                        case "create_outline" -> state.outline();
                        case "write_content" -> state.content();
                        case "polish" -> state.polished();
                        default -> "";
                    };

                    log.info("  Streaming event: {} ({} chars)", nodeName, data.length());

                    // Send SSE event with node name as event type
                    emitter.send(SseEmitter.event()
                            .name(nodeName)
                            .data(Map.of(
                                    "node", nodeName,
                                    "content", data,
                                    "stepsCompleted", state.steps()
                            )));
                }

                // Send completion event
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(Map.of("status", "done")));

                emitter.complete();
                log.info("Streaming: Pipeline complete");

            } catch (Exception e) {
                log.error("Streaming error", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}

package com.example.langgraph.humanloop.nodes;

import com.example.langgraph.humanloop.HumanLoopState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * NODE: Human Review
 *
 * This node reads the human's decision from state.
 * The actual approval is injected via updateState() when resuming.
 */
@Component
public class HumanReviewNode implements Function<HumanLoopState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(HumanReviewNode.class);

    @Override
    public Map<String, Object> apply(HumanLoopState state) {
        boolean approved = state.approved();
        String feedback = state.feedback();
        log.info("👤 Human review — approved: {}, feedback: {}", approved, feedback);

        // Just pass through — the decision is already in state
        return Map.of();
    }
}

package com.example.langgraph.checkpoint.nodes;

import com.example.langgraph.checkpoint.CheckpointState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * NODE: Build Context
 *
 * Adds current input to history.
 */
@Component
public class BuildContextNode implements Function<CheckpointState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(BuildContextNode.class);

    @Override
    public Map<String, Object> apply(CheckpointState state) {
        String input = state.currentInput();
        log.debug("Building context — current input: {}", input);

        // Append user message to history (appender channel handles accumulation)
        return Map.of("history", List.of("User: " + input));
    }
}

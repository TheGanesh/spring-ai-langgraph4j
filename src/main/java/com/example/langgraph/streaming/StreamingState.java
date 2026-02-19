package com.example.langgraph.streaming;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * State for the Streaming graph.
 *
 * Fields:
 *   - topic:      user's input topic
 *   - outline:    generated outline (step 1)
 *   - content:    generated content (step 2)
 *   - polished:   final polished version (step 3)
 *   - steps:      log of completed steps (appender channel)
 */
public class StreamingState extends AgentState {

    public static Map<String, Channel<?>> SCHEMA = Map.of(
            "steps", Channels.<String>appender(ArrayList::new)
    );

    public StreamingState(Map<String, Object> initData) {
        super(initData);
    }

    public String topic() {
        return this.<String>value("topic").orElse("");
    }

    public String outline() {
        return this.<String>value("outline").orElse("");
    }

    public String content() {
        return this.<String>value("content").orElse("");
    }

    public String polished() {
        return this.<String>value("polished").orElse("");
    }

    public List<String> steps() {
        return this.<List<String>>value("steps").orElse(List.of());
    }
}

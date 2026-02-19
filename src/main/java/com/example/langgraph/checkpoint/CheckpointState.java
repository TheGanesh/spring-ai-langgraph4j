package com.example.langgraph.checkpoint;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * State for the Checkpointing demo.
 *
 * Uses an appender channel for "history" so conversation history accumulates
 * across multiple API calls with the same threadId.
 *
 * Fields:
 *   - history:       conversation history (appender channel — persists across calls)
 *   - currentInput:  the latest user message
 *   - currentOutput: the latest AI response
 */
public class CheckpointState extends AgentState {

    public static Map<String, Channel<?>> SCHEMA = Map.of(
            "history", Channels.<String>appender(ArrayList::new)
    );

    public CheckpointState(Map<String, Object> initData) {
        super(initData);
    }

    public String currentInput() {
        return this.<String>value("currentInput").orElse("");
    }

    public String currentOutput() {
        return this.<String>value("currentOutput").orElse("");
    }

    @SuppressWarnings("unchecked")
    public List<String> history() {
        return this.<List<String>>value("history").orElse(List.of());
    }
}

package com.example.langgraph.toolcalling;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * State for the Tool Calling graph.
 *
 * Uses an appender channel for "messages" — this means when a node returns
 * Map.of("messages", List.of(newMsg)), it APPENDS to the existing list
 * rather than replacing it. This is critical for maintaining conversation history.
 *
 * Fields:
 *   - messages:    conversation history (appender — grows over time)
 *   - userQuery:   the original user question
 *   - finalAnswer: the LLM's final response after tool use
 */
public class ToolCallingState extends AgentState {

    public static Map<String, Channel<?>> SCHEMA = Map.of(
            "messages", Channels.<String>appender(ArrayList::new)
    );

    public ToolCallingState(Map<String, Object> initData) {
        super(initData);
    }

    public String userQuery() {
        return this.<String>value("userQuery").orElse("");
    }

    @SuppressWarnings("unchecked")
    public List<String> messages() {
        return this.<List<String>>value("messages").orElse(List.of());
    }

    public String finalAnswer() {
        return this.<String>value("finalAnswer").orElse("");
    }

    public boolean needsTool() {
        return this.<Boolean>value("needsTool").orElse(false);
    }

    public String toolName() {
        return this.<String>value("toolName").orElse("");
    }

    public String toolInput() {
        return this.<String>value("toolInput").orElse("");
    }

    public String toolResult() {
        return this.<String>value("toolResult").orElse("");
    }
}

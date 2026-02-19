package com.example.langgraph.subgraph;

import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

/**
 * Shared state for both the outer graph and inner subgraph.
 *
 * Both graphs must use the SAME state type when using addSubgraph().
 *
 * Fields:
 *   - document:     the input document text
 *   - category:     document category (set by outer graph's classify node)
 *   - keyPoints:    extracted key points (set by inner subgraph)
 *   - summary:      generated summary (set by inner subgraph)
 *   - finalOutput:  formatted final result (set by outer graph's format node)
 */
public class SubGraphState extends AgentState {

    public SubGraphState(Map<String, Object> initData) {
        super(initData);
    }

    public String document() {
        return this.<String>value("document").orElse("");
    }

    public String category() {
        return this.<String>value("category").orElse("");
    }

    public String keyPoints() {
        return this.<String>value("keyPoints").orElse("");
    }

    public String summary() {
        return this.<String>value("summary").orElse("");
    }

    public String finalOutput() {
        return this.<String>value("finalOutput").orElse("");
    }
}

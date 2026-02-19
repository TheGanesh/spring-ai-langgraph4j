package com.example.langgraph.humanloop;

import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

/**
 * State for the Human-in-the-Loop graph.
 *
 * Fields:
 *   - topic:            user's requested topic
 *   - generatedContent: LLM-generated draft content
 *   - approved:         whether human approved (set after interrupt)
 *   - feedback:         optional human feedback
 *   - finalResponse:    final output after human decision
 */
public class HumanLoopState extends AgentState {

    public HumanLoopState(Map<String, Object> initData) {
        super(initData);
    }

    public String topic() {
        return this.<String>value("topic").orElse("");
    }

    public String generatedContent() {
        return this.<String>value("generatedContent").orElse("");
    }

    public boolean approved() {
        return this.<Boolean>value("approved").orElse(false);
    }

    public String feedback() {
        return this.<String>value("feedback").orElse("");
    }

    public String finalResponse() {
        return this.<String>value("finalResponse").orElse("");
    }
}

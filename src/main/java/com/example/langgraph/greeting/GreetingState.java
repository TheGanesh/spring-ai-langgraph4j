package com.example.langgraph.greeting;

import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

/**
 * The State object that flows through the entire graph.
 *
 * Think of this as a "clipboard" — each node reads from it, does work, and writes updates back.
 *
 * Fields:
 *   - userName:  the user's name (input)
 *   - greeting:  the LLM-generated greeting (set by GreetingNode)
 *   - sentiment: "positive" or "neutral" (set by SentimentNode)
 *   - response:  the final formatted response (set by ResponseNode)
 */
public class GreetingState extends AgentState {

    public GreetingState(Map<String, Object> initData) {
        super(initData);
    }

    public String userName() {
        return this.<String>value("userName").orElse("");
    }

    public String greeting() {
        return this.<String>value("greeting").orElse("");
    }

    public String sentiment() {
        return this.<String>value("sentiment").orElse("neutral");
    }

    public String response() {
        return this.<String>value("response").orElse("");
    }
}

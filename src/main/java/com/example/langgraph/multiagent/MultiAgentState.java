package com.example.langgraph.multiagent;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * State for the Multi-Agent graph.
 *
 * Fields:
 *   - task:           the user's original request
 *   - nextAgent:      which agent should run next (set by supervisor)
 *   - researchData:   data gathered by the researcher agent
 *   - writtenContent: content produced by the writer agent
 *   - finalOutput:    final combined output
 *   - agentLog:       log of agent actions (appender channel)
 *   - iterations:     how many supervisor loops so far
 */
public class MultiAgentState extends AgentState {

    public static Map<String, Channel<?>> SCHEMA = Map.of(
            "agentLog", Channels.<String>appender(ArrayList::new)
    );

    public MultiAgentState(Map<String, Object> initData) {
        super(initData);
    }

    public String task() {
        return this.<String>value("task").orElse("");
    }

    public String nextAgent() {
        return this.<String>value("nextAgent").orElse("FINISH");
    }

    public String researchData() {
        return this.<String>value("researchData").orElse("");
    }

    public String writtenContent() {
        return this.<String>value("writtenContent").orElse("");
    }

    public String finalOutput() {
        return this.<String>value("finalOutput").orElse("");
    }

    public List<String> agentLog() {
        return this.<List<String>>value("agentLog").orElse(List.of());
    }

    public int iterations() {
        return this.<Integer>value("iterations").orElse(0);
    }
}

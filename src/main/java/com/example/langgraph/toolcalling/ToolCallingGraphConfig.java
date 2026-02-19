package com.example.langgraph.toolcalling;

import com.example.langgraph.toolcalling.nodes.AgentNode;
import com.example.langgraph.toolcalling.nodes.ToolExecutorNode;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * CONCEPT 1: TOOL CALLING
 *
 * This graph demonstrates how the LLM can call external tools/functions.
 *
 * The Pattern (ReAct loop):
 *   1. Agent node asks the LLM: "should I call a tool?"
 *   2. If yes → tool executor node runs the tool → loops back to agent
 *   3. If no  → agent produces final answer → END
 *
 * Graph:
 *   START → agent → (needs_tool?) ─Yes─→ tool_executor → agent (loop)
 *                                 └─No──→ END
 */
@Configuration
public class ToolCallingGraphConfig {

    private static final Logger log = LoggerFactory.getLogger(ToolCallingGraphConfig.class);

    private final AgentNode agentNode;
    private final ToolExecutorNode toolExecutorNode;

    public ToolCallingGraphConfig(AgentNode agentNode, ToolExecutorNode toolExecutorNode) {
        this.agentNode = agentNode;
        this.toolExecutorNode = toolExecutorNode;
    }

    // ───── GRAPH WIRING ─────
    @Bean
    public CompiledGraph<ToolCallingState> toolCallingGraph() throws Exception {
        log.info("Building Tool Calling Graph...");

        StateGraph<ToolCallingState> workflow = new StateGraph<>(
                ToolCallingState.SCHEMA, ToolCallingState::new)

                .addNode("agent", node_async(agentNode::apply))
                .addNode("tool_executor", node_async(toolExecutorNode::apply))

                .addEdge(START, "agent")

                // Conditional: does the agent need a tool?
                .addConditionalEdges("agent",
                        edge_async(state -> state.needsTool() ? "use_tool" : "done"),
                        Map.of(
                                "use_tool", "tool_executor",
                                "done", END
                        ))

                // After tool execution, loop back to agent
                .addEdge("tool_executor", "agent");

        return workflow.compile();
    }
}

package com.example.langgraph.multiagent;

import com.example.langgraph.multiagent.nodes.ResearcherAgentNode;
import com.example.langgraph.multiagent.nodes.SupervisorAgentNode;
import com.example.langgraph.multiagent.nodes.WriterAgentNode;
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
 * CONCEPT 6: MULTI-AGENT
 *
 * Multiple AI agents collaborating in a graph, orchestrated by a supervisor.
 *
 * The Supervisor Pattern:
 *   1. Supervisor agent decides which worker agent should act next
 *   2. Worker agent does its job and returns to supervisor
 *   3. Supervisor decides: another agent, or FINISH?
 *   4. When supervisor says FINISH → END
 *
 * Agents:
 *   - Supervisor:  Routes work to the right agent, decides when done
 *   - Researcher:  Gathers information and facts about a topic
 *   - Writer:      Takes research and writes polished content
 *
 * Graph:
 *   START → supervisor → (researcher | writer | FINISH)
 *                ↑              |            |
 *                └──────────────┴────────────┘
 *
 * The supervisor loops until it decides the task is complete.
 */
@Configuration
public class MultiAgentGraphConfig {

    private static final Logger log = LoggerFactory.getLogger(MultiAgentGraphConfig.class);

    private final SupervisorAgentNode supervisorAgentNode;
    private final ResearcherAgentNode researcherAgentNode;
    private final WriterAgentNode writerAgentNode;

    public MultiAgentGraphConfig(SupervisorAgentNode supervisorAgentNode,
                                  ResearcherAgentNode researcherAgentNode,
                                  WriterAgentNode writerAgentNode) {
        this.supervisorAgentNode = supervisorAgentNode;
        this.researcherAgentNode = researcherAgentNode;
        this.writerAgentNode = writerAgentNode;
    }

    // ═══════════════════════════════
    //  GRAPH WIRING
    // ═══════════════════════════════
    @Bean
    public CompiledGraph<MultiAgentState> multiAgentGraph() throws Exception {
        log.info("Building Multi-Agent Graph...");

        return new StateGraph<>(MultiAgentState.SCHEMA, MultiAgentState::new)

                .addNode("supervisor", node_async(supervisorAgentNode::apply))
                .addNode("researcher", node_async(researcherAgentNode::apply))
                .addNode("writer", node_async(writerAgentNode::apply))

                // Start with supervisor
                .addEdge(START, "supervisor")

                // Supervisor routes to the chosen agent or END
                .addConditionalEdges("supervisor",
                        edge_async(state -> state.nextAgent()),
                        Map.of(
                                "researcher", "researcher",
                                "writer", "writer",
                                "FINISH", END
                        ))

                // Both worker agents return to supervisor for next decision
                .addEdge("researcher", "supervisor")
                .addEdge("writer", "supervisor")

                .compile();
    }
}

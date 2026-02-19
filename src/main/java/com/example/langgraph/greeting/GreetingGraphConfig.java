package com.example.langgraph.greeting;

import com.example.langgraph.greeting.nodes.GreetingNode;
import com.example.langgraph.greeting.nodes.ResponseNode;
import com.example.langgraph.greeting.nodes.SentimentNode;
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
 * GRAPH CONFIGURATION
 *
 * This is where all nodes and edges are wired together into a runnable graph.
 *
 * The graph looks like this:
 *
 *   START
 *     │
 *     ▼
 *   greeting       ← calls LLM to generate a personalized greeting
 *     │
 *     ▼
 *   sentiment      ← calls LLM to classify sentiment ("positive" or "neutral")
 *     │
 *     ▼
 *   (conditional)  ← routes based on the sentiment value
 *    ╱          ╲
 *   ▼            ▼
 * celebrate    encourage    ← format final response differently
 *   │            │
 *   ▼            ▼
 *  END          END
 */
@Configuration
public class GreetingGraphConfig {

    private static final Logger log = LoggerFactory.getLogger(GreetingGraphConfig.class);

    @Bean
    public CompiledGraph<GreetingState> greetingGraph(
            GreetingNode greetingNode,
            SentimentNode sentimentNode,
            ResponseNode responseNode) throws Exception {

        log.info("Building the Greeting Graph...");

        // 1. Create a new StateGraph with our custom state type
        StateGraph<GreetingState> workflow = new StateGraph<>(GreetingState::new)

                // 2. Add nodes — each node is a named function
                .addNode("greeting",  node_async(greetingNode::process))
                .addNode("sentiment", node_async(sentimentNode::process))
                .addNode("celebrate", node_async(responseNode::celebrateResponse))
                .addNode("encourage", node_async(responseNode::encourageResponse))

                // 3. Add edges — define the flow between nodes

                // Normal edge: START → greeting (always runs first)
                .addEdge(START, "greeting")

                // Normal edge: greeting → sentiment (always runs after greeting)
                .addEdge("greeting", "sentiment")

                // Conditional edge: sentiment → (celebrate OR encourage)
                // The routing function returns "positive" or "neutral",
                // and the map translates that to a node name.
                .addConditionalEdges(
                        "sentiment",
                        edge_async(state -> {
                            String sentiment = state.sentiment();
                            log.debug("Router: sentiment='{}', routing to '{}'",
                                    sentiment,
                                    sentiment.equals("positive") ? "celebrate" : "encourage");
                            return sentiment.equals("positive") ? "positive" : "neutral";
                        }),
                        Map.of(
                                "positive", "celebrate",   // if routing fn returns "positive" → go to celebrate node
                                "neutral",  "encourage"    // if routing fn returns "neutral"  → go to encourage node
                        )
                )

                // Normal edges: both response nodes lead to END
                .addEdge("celebrate", END)
                .addEdge("encourage", END);

        // 4. Compile the graph — validates all connections and returns a runnable graph
        CompiledGraph<GreetingState> compiled = workflow.compile();
        log.info("Greeting Graph compiled successfully!");

        return compiled;
    }
}

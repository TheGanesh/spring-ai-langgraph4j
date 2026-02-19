package com.example.langgraph.studio;

import com.example.langgraph.greeting.GreetingState;
import com.example.langgraph.greeting.nodes.GreetingNode;
import com.example.langgraph.greeting.nodes.ResponseNode;
import com.example.langgraph.greeting.nodes.SentimentNode;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.studio.springboot.AbstractLangGraphStudioConfig;
import org.bsc.langgraph4j.studio.springboot.LangGraphFlow;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * LangGraph4j Studio Configuration
 *
 * Enables the web UI for visualizing and testing graphs.
 * Access at: http://localhost:8080/studio
 *
 * Enable with: langgraph.studio.enabled=true in application.properties
 */
@Configuration
@ConditionalOnProperty(name = "langgraph.studio.enabled", havingValue = "true", matchIfMissing = false)
public class LangGraphStudioConfig extends AbstractLangGraphStudioConfig {

    private final GreetingNode greetingNode;
    private final SentimentNode sentimentNode;
    private final ResponseNode responseNode;

    public LangGraphStudioConfig(GreetingNode greetingNode,
                                  SentimentNode sentimentNode,
                                  ResponseNode responseNode) {
        this.greetingNode = greetingNode;
        this.sentimentNode = sentimentNode;
        this.responseNode = responseNode;
    }

    @Override
    public LangGraphFlow getFlow() {
        try {
            // Configure the Greeting graph for Studio
            StateGraph<GreetingState> greetingGraph = new StateGraph<>(GreetingState::new)
                    .addNode("greeting", node_async(greetingNode::process))
                    .addNode("sentiment", node_async(sentimentNode::process))
                    .addNode("celebrate", node_async(responseNode::celebrateResponse))
                    .addNode("encourage", node_async(responseNode::encourageResponse))
                    .addEdge(START, "greeting")
                    .addEdge("greeting", "sentiment")
                    .addConditionalEdges(
                            "sentiment",
                            edge_async(state -> state.sentiment().equals("positive") ? "positive" : "neutral"),
                            Map.of(
                                    "positive", "celebrate",
                                    "neutral", "encourage"
                            ))
                    .addEdge("celebrate", END)
                    .addEdge("encourage", END);

            return LangGraphFlow.builder()
                    .title("Greeting Graph")
                    .addInputStringArg("userName", true)
                    .stateGraph(greetingGraph)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create LangGraph flow", e);
        }
    }
}

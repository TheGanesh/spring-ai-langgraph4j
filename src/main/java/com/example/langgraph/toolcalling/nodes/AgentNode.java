package com.example.langgraph.toolcalling.nodes;

import com.example.langgraph.toolcalling.ToolCallingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * NODE: Agent
 *
 * Calls LLM and decides whether to use a tool or give a final answer.
 */
@Component
public class AgentNode implements Function<ToolCallingState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(AgentNode.class);

    private final ChatClient chatClient;

    public AgentNode(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Map<String, Object> apply(ToolCallingState state) {
        String query = state.userQuery();
        String toolResult = state.toolResult();

        String prompt;
        if (!toolResult.isEmpty()) {
            // We have a tool result — ask LLM to form final answer
            prompt = String.format(
                "User asked: %s\n\nTool result: %s\n\nProvide a helpful final answer using the tool result.",
                query, toolResult);
        } else {
            // First call — ask LLM if it needs a tool
            prompt = String.format(
                "User asked: %s\n\n" +
                "You have access to these tools:\n" +
                "  1. getWeather(city) — returns current weather for a city\n" +
                "  2. calculate(expression) — evaluates a math expression\n\n" +
                "If you need a tool, respond EXACTLY in this format:\n" +
                "TOOL: toolName(argument)\n\n" +
                "If you can answer directly without a tool, just provide the answer.\n" +
                "Do NOT use a tool if the question doesn't require one.",
                query);
        }

        String response = chatClient.prompt().user(prompt).call().content();
        log.debug("Agent response: {}", response);

        Map<String, Object> updates = new HashMap<>();
        updates.put("messages", List.of("agent: " + response));

        // Parse tool call from response
        if (response != null && response.contains("TOOL:") && toolResult.isEmpty()) {
            String toolCall = response.substring(response.indexOf("TOOL:") + 5).trim();
            String toolName = toolCall.substring(0, toolCall.indexOf("(")).trim();
            String toolInput = toolCall.substring(toolCall.indexOf("(") + 1, toolCall.lastIndexOf(")")).trim();

            updates.put("needsTool", true);
            updates.put("toolName", toolName);
            updates.put("toolInput", toolInput);
        } else {
            updates.put("needsTool", false);
            updates.put("finalAnswer", response);
        }

        return updates;
    }
}

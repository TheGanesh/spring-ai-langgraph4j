package com.example.langgraph.humanloop.nodes;

import com.example.langgraph.humanloop.HumanLoopState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * NODE: Final Response
 *
 * Generates final output based on human's decision.
 */
@Component
public class FinalResponseNode implements Function<HumanLoopState, Map<String, Object>> {

    @Override
    public Map<String, Object> apply(HumanLoopState state) {
        if (state.approved()) {
            String response = "✅ APPROVED — Content published:\n\n" + state.generatedContent();
            if (!state.feedback().isEmpty()) {
                response += "\n\n📝 Human feedback: " + state.feedback();
            }
            return Map.of("finalResponse", response);
        } else {
            String reason = state.feedback().isEmpty() ? "No reason given" : state.feedback();
            return Map.of("finalResponse",
                    "❌ REJECTED — Content was not published.\nReason: " + reason +
                    "\n\nOriginal draft:\n" + state.generatedContent());
        }
    }
}

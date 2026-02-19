package com.example.langgraph.subgraph.nodes;

import com.example.langgraph.subgraph.SubGraphState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Outer Graph Node 3: Format the final output.
 */
@Component
public class FormatOutputNode implements Function<SubGraphState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(FormatOutputNode.class);

    @Override
    public Map<String, Object> apply(SubGraphState state) {
        log.info("[Outer] Formatting final output...");

        String output = String.format(
                "📄 DOCUMENT ANALYSIS\n" +
                "═══════════════════\n" +
                "Category: %s\n\n" +
                "🔑 Key Points:\n%s\n\n" +
                "📝 Summary:\n%s",
                state.category(),
                state.keyPoints(),
                state.summary()
        );

        return Map.of("finalOutput", output);
    }
}

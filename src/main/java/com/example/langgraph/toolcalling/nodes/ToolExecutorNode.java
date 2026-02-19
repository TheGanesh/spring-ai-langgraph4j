package com.example.langgraph.toolcalling.nodes;

import com.example.langgraph.toolcalling.ToolCallingState;
import com.example.langgraph.toolcalling.tools.CalculatorTool;
import com.example.langgraph.toolcalling.tools.WeatherTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * NODE: Tool Executor
 *
 * Runs the requested tool and stores the result.
 */
@Component
public class ToolExecutorNode implements Function<ToolCallingState, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutorNode.class);

    private final WeatherTool weatherTool;
    private final CalculatorTool calculatorTool;

    public ToolExecutorNode(WeatherTool weatherTool, CalculatorTool calculatorTool) {
        this.weatherTool = weatherTool;
        this.calculatorTool = calculatorTool;
    }

    @Override
    public Map<String, Object> apply(ToolCallingState state) {
        String toolName = state.toolName();
        String toolInput = state.toolInput();
        log.info("🔧 Executing tool: {}({})", toolName, toolInput);

        String result;
        switch (toolName.toLowerCase()) {
            case "getweather" -> {
                var weatherResult = weatherTool.apply(new WeatherTool.Request(toolInput));
                result = "Weather in " + weatherResult.city() + ": " + weatherResult.forecast();
            }
            case "calculate" -> {
                var calcResult = calculatorTool.apply(new CalculatorTool.Request(toolInput));
                result = calcResult.expression() + " = " + calcResult.result();
            }
            default -> result = "Unknown tool: " + toolName;
        }

        log.info("🔧 Tool result: {}", result);
        return Map.of(
                "toolResult", result,
                "needsTool", false,
                "messages", List.of("tool(" + toolName + "): " + result)
        );
    }
}

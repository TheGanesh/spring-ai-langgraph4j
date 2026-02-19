package com.example.langgraph.toolcalling.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.function.Function;

/**
 * TOOL: Calculator
 *
 * A simple calculator that evaluates math expressions.
 * The LLM calls this when the user asks a math question.
 */
@Component("calculate")
public class CalculatorTool implements Function<CalculatorTool.Request, CalculatorTool.Response> {

    private static final Logger log = LoggerFactory.getLogger(CalculatorTool.class);

    @Override
    public Response apply(Request request) {
        String expression = request.expression();
        log.info("🧮 CalculatorTool called with expression: {}", expression);

        try {
            // Simple evaluation using JavaScript engine
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
            if (engine != null) {
                Object result = engine.eval(expression);
                return new Response(expression, result.toString());
            }
            // Fallback: parse simple arithmetic manually
            double result = evaluateSimple(expression);
            return new Response(expression, String.valueOf(result));
        } catch (Exception e) {
            log.error("Calculator error: {}", e.getMessage());
            return new Response(expression, "Error: could not evaluate expression");
        }
    }

    private double evaluateSimple(String expr) {
        // Very basic: handle single operation (a + b, a * b, etc.)
        expr = expr.trim();
        if (expr.contains("+")) {
            String[] parts = expr.split("\\+");
            return Double.parseDouble(parts[0].trim()) + Double.parseDouble(parts[1].trim());
        } else if (expr.contains("*")) {
            String[] parts = expr.split("\\*");
            return Double.parseDouble(parts[0].trim()) * Double.parseDouble(parts[1].trim());
        } else if (expr.contains("-")) {
            String[] parts = expr.split("-");
            return Double.parseDouble(parts[0].trim()) - Double.parseDouble(parts[1].trim());
        } else if (expr.contains("/")) {
            String[] parts = expr.split("/");
            return Double.parseDouble(parts[0].trim()) / Double.parseDouble(parts[1].trim());
        }
        return Double.parseDouble(expr);
    }

    public record Request(String expression) {}
    public record Response(String expression, String result) {}
}

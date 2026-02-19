package com.example.langgraph.toolcalling.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * TOOL: Weather Lookup (Mock)
 *
 * A simulated weather API that the LLM can call.
 * In production, this would hit a real weather API like OpenWeatherMap.
 *
 * The LLM decides WHEN to call this tool based on the user's question.
 */
@Component("getWeather")
public class WeatherTool implements Function<WeatherTool.Request, WeatherTool.Response> {

    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);

    // Mock weather data
    private static final Map<String, String> WEATHER_DATA = Map.of(
            "new york", "72°F, Partly Cloudy",
            "london", "58°F, Rainy",
            "tokyo", "80°F, Sunny",
            "paris", "65°F, Overcast",
            "sydney", "85°F, Clear Skies"
    );

    @Override
    public Response apply(Request request) {
        String city = request.city().toLowerCase().trim();
        log.info("🌤️ WeatherTool called for city: {}", city);

        String weather = WEATHER_DATA.getOrDefault(city, "70°F, Clear (default forecast)");
        return new Response(city, weather);
    }

    public record Request(String city) {}
    public record Response(String city, String forecast) {}
}

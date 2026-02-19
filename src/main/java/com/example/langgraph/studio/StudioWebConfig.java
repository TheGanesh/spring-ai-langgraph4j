package com.example.langgraph.studio;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for LangGraph4j Studio.
 * 
 * Serves the static web UI from the langgraph4j-studio-springboot JAR.
 */
@Configuration
@ConditionalOnProperty(name = "langgraph.studio.enabled", havingValue = "true", matchIfMissing = false)
public class StudioWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from the studio JAR
        registry.addResourceHandler("/studio/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect /studio to /studio/index.html
        registry.addRedirectViewController("/studio", "/studio/index.html");
    }
}

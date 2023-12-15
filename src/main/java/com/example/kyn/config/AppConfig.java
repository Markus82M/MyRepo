package com.example.kyn.config;

import com.example.kyn.intercept.LoggerInterceptor;
import com.example.kyn.intercept.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class AppConfig implements WebMvcConfigurer {

    private RequestInterceptor requestInterceptor;
    private LoggerInterceptor loggerInterceptor;

    public AppConfig(RequestInterceptor requestInterceptor, LoggerInterceptor loggerInterceptor) {
        this.requestInterceptor = requestInterceptor;
        this.loggerInterceptor = loggerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loggerInterceptor)
                .addPathPatterns("/api/**");

        registry.addInterceptor(requestInterceptor)
                .addPathPatterns("/api/**");
    }
}
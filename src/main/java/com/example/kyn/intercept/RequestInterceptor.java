package com.example.kyn.intercept;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class RequestInterceptor implements HandlerInterceptor {

    private static Logger log = LoggerFactory.getLogger(RequestInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String apiKey = request.getHeader("X-api-key");
        log.info("[preHandle_RequestInterceptor_headersChecks]");
        if (apiKey == null || apiKey.isEmpty()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing Header: X-api-key");
            return false;
        }
        return true;
    }

}

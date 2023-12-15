package com.example.kyn.intercept;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

@Configuration
public class LoggerInterceptor implements HandlerInterceptor {
    private static Logger log = LoggerFactory.getLogger(LoggerInterceptor.class);

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        log.debug("Request Http Method:" + request.getMethod());
        log.debug("Request URI:" + request.getRequestURI());
        log.debug("Request Parameters:" + getParameters(request));
        log.debug("Request Headers:" + getHttpHeaders(request));

        return true;
    }


    private String getParameters(HttpServletRequest request) {
        StringBuffer posted = new StringBuffer();
        Enumeration<?> e = request.getParameterNames();
        if (e != null) {
            posted.append("?");
        }
        while (e.hasMoreElements()) {
            if (posted.length() > 1) {
                posted.append("&");
            }
            String curr = (String) e.nextElement();
            posted.append(curr + "=");
            if (curr.contains("password")
                    || curr.contains("pass")
                    || curr.contains("pwd")) {
                posted.append("*****");
            } else {
                posted.append(request.getParameter(curr));
            }
        }
        String ip = request.getHeader("X-FORWARDED-FOR");
        String ipAddr = (ip == null) ? request.getRemoteAddr() : ip;
        if (ipAddr != null && !ipAddr.equals("")) {
            posted.append("&_psip=" + ipAddr);
        }
        return posted.toString();
    }

    private String getHttpHeaders(HttpServletRequest request) {
        StringBuffer headers = new StringBuffer();
        Enumeration<?> e = request.getHeaderNames();

        while (e.hasMoreElements()) {
            if (headers.length() > 1) {
                headers.append(" | ");
            }
            String headerName = (String) e.nextElement();
            headers.append(headerName + "=");
            if (headerName.contains("password")
                    || headerName.contains("pass")
                    || headerName.contains("pwd")
                    || headerName.contains("key")
                    || headerName.contains("Key")) {
                headers.append("*****");
            } else {
                headers.append(request.getHeader(headerName));
            }
        }

        return headers.toString();
    }

}
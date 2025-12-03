package com.twojz.y_kit.global.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

public class InvalidMethodFilter implements Filter {

    private static final Set<String> VALID_METHODS =
            Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String method = httpReq.getMethod();

        if (!VALID_METHODS.contains(method)) {
            return;
        }

        chain.doFilter(request, response);
    }
}

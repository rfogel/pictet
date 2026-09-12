package com.pictet.common.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MDCFilter extends OncePerRequestFilter {

    public static final String TRANSACTIONAL_ID = "transactionId";

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MDC.put(TRANSACTIONAL_ID, " [" + UUID.randomUUID() + "]");
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRANSACTIONAL_ID);
        }
    }
}
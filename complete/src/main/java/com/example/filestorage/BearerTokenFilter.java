package com.example.filestorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {

  private final String token;

  public BearerTokenFilter(@Value("${files.auth-token:}") String token) {
    this.token = token == null ? "" : token;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return token.isBlank() || request.getRequestURI().equals("/health");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String supplied = request.getHeader(HttpHeaders.AUTHORIZATION);
    String expected = "Bearer " + token;
    boolean matches = supplied != null && MessageDigest.isEqual(
        supplied.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    if (!matches) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Unauthorized\"}");
      return;
    }
    filterChain.doFilter(request, response);
  }
}

package com.saludrednorte.ms_notificaciones.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Filtro JWT: valida tokens firmados por ms-auth o tokens legacy Base64 del Gateway.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);

            if (jwtUtil.isTokenValid(jwt)) {
                authenticateFromJwt(request, jwt);
            } else {
                authenticateFromLegacyToken(request, jwt);
            }
        }

        chain.doFilter(request, response);
    }

    private void authenticateFromJwt(HttpServletRequest request, String jwt) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        String username = jwtUtil.extractUsername(jwt);
        String role = jwtUtil.extractRole(jwt);
        if (username == null) {
            return;
        }
        String springRole = role != null ? role.replace("ROLE_", "") : "USER";
        UserDetails userDetails = User.builder()
                .username(username)
                .password("")
                .roles(springRole)
                .build();
        setAuthentication(request, userDetails);
    }

    private void authenticateFromLegacyToken(HttpServletRequest request, String jwt) {
        try {
            String decoded = new String(Base64.getDecoder().decode(jwt), StandardCharsets.UTF_8);
            String[] partes = decoded.split(":", 2);
            if (partes.length != 2 || partes[0].isEmpty()) {
                return;
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(partes[0]);
            if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                setAuthentication(request, userDetails);
            }
        } catch (Exception ignored) {
            // Token no reconocido
        }
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}

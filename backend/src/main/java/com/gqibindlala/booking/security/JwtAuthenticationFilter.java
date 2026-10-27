package com.gqibindlala.booking.security;

import com.gqibindlala.booking.model.AppRole;
import com.gqibindlala.booking.model.AppUser;
import com.gqibindlala.booking.repository.AppUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AppUserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, AppUserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                AppUser user = userRepository.findByLoginIdentifier(claims.getSubject()).orElse(null);
                if (user != null && user.isActive()
                        && user.getId().equals(claims.get("userId", Long.class))
                        && (user.getDepartment() == null || user.getDepartment().isActive())) {
                    AuthPrincipal principal = new AuthPrincipal(user.getId(), user.getLoginIdentifier(),
                        user.getPersonalEmail(), user.getFullName(), user.getPhone(), user.getRole(),
                        user.getDepartment() == null ? null : user.getDepartment().getId(),
                        user.getDepartment() == null ? null : user.getDepartment().getName());
                    var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
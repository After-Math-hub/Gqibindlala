package com.gqibindlala.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.gqibindlala.booking.security.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

/**
 * Public catalogue access with customer, department, payments, and admin roles.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/equipment/admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/equipment/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/departments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/departments/admin").hasRole("ADMIN")
                .requestMatchers("/api/departments/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/bookings/mine").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/bookings/*/payment-reference").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.GET, "/api/bookings/payment-review/**").hasAnyRole("PAYMENTS", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/bookings/*/payment-review/**").hasAnyRole("PAYMENTS", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings/department/**").hasAnyRole("DEPARTMENT", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/bookings/items/*/fulfillment").hasAnyRole("DEPARTMENT", "ADMIN")
                .requestMatchers("/api/staff/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/bookings/*/cancel").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/equipment/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/equipment/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/equipment/**")
                    .hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

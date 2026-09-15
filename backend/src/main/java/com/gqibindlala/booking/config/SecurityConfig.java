package com.gqibindlala.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Starter security config: public browsing + booking submission stay open,
 * everything that manages equipment or confirms/cancels bookings requires auth.
 *
 * NOTE: this is a starting point. Wire in the JWT filter once the admin
 * login endpoint is built (see jjwt dependency in pom.xml).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/equipment/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}); // swap for JWT filter once auth endpoint exists

        return http.build();
    }
}

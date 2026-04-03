package com.falcon.booking.security.config;

import com.falcon.booking.security.jwt.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Autowired
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        //AUTH & DOCS
                        .requestMatchers("/v1/auth/register-admin").hasRole("ADMIN")
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // FLIGHTS GENERATIONS
                        .requestMatchers("/v1/flights/generations/**").hasRole("ADMIN")

                        // FLIGHTS
                        .requestMatchers(HttpMethod.POST, "/v1/flights/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/v1/flights/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/flights/**").permitAll()

                        //RESERVATIONS
                        .requestMatchers("/v1/reservations/**").permitAll()

                        //COUNTRIES
                        .requestMatchers(HttpMethod.GET, "/v1/countries/**").permitAll()

                        //ADMIN ONLY
                        .requestMatchers(
                                "/v1/airplane-types/**",
                                "/v1/passengers/**",
                                "/v1/airports/**",
                                "/v1/routes/**"
                        ).hasRole("ADMIN")

                        //ROUTES
                        .requestMatchers(HttpMethod.GET, "/v1/routes/**").permitAll()

                        .anyRequest().hasRole("ADMIN")

                ).addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

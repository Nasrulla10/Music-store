package com.music.musicstore.configs;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow CORS preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/music", "/api/music/**").permitAll() // Allow public access to all music endpoints
                .requestMatchers("/api/music/browse/**", "/api/music/preview/**").permitAll()
                .requestMatchers("/api/reviews/music/**").permitAll()
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/uploads/**").permitAll() // Allow public access to uploaded music files
                .requestMatchers("/").permitAll()

                // Customer endpoints (override specific music endpoints that need authentication)
                .requestMatchers("/api/cart/**", "/api/orders/**", "/api/playlists/**").hasRole("CUSTOMER")
                .requestMatchers("/api/music/purchase/**", "/api/music/download/**").hasRole("CUSTOMER")
                .requestMatchers("/api/tickets/create").hasAnyRole("CUSTOMER", "ARTIST")
                .requestMatchers("/api/reviews/create").hasRole("CUSTOMER")

                // Artist endpoints (override music upload/manage)
                .requestMatchers("/api/music/upload").hasRole("ARTIST")
                .requestMatchers("/api/music/manage/**").hasRole("ARTIST")
                .requestMatchers("/api/reviews/artist/**").hasRole("ARTIST")
                .requestMatchers("/api/analytics/artist/**").hasRole("ARTIST")

                // Staff endpoints
                .requestMatchers("/api/tickets/manage/**", "/api/tickets/reply/**").hasRole("STAFF")
                .requestMatchers("/api/analytics/website/**", "/api/reports/sales/**").hasRole("STAFF")

                // Admin endpoints - Fixed to match controller paths
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")  // Added this for your controller
                .requestMatchers("/api/users/manage/**").hasRole("ADMIN")
                .requestMatchers("/api/analytics/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
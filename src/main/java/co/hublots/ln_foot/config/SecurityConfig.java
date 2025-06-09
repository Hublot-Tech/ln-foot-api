package co.hublots.ln_foot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/webhooks/notchpay") // ❌ No CSRF protection for this endpoint
                )
                .authorizeHttpRequests(auth -> auth
                        // Existing public webhook
                        .requestMatchers(HttpMethod.POST, "/webhooks/notchpay").permitAll()

                        // Secure POST, PUT, DELETE for /api/v1/**
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").authenticated()

                        // Existing authenticated GET for orders
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").authenticated()

                        // General GET permit all (catches all other GET requests not specified above)
                        // This makes all GET endpoints under /api/v1/ public by default,
                        // unless a more specific GET rule above makes them authenticated.
                        // (Currently, no /api/v1/ GET endpoints are marked as authenticated above this line)
                        .requestMatchers(HttpMethod.GET).permitAll()

                        // Default for any other request not matched above
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }
}

package com.eilco.messagerie.config;

import com.eilco.messagerie.security.jwt.AuthTokenFilter;
import com.eilco.messagerie.security.jwt.JwtAuthenticationEntryPoint;
import com.eilco.messagerie.services.interfaces.IUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

      private final IUserService userService;
      private final PasswordEncoder passwordEncoder;
      private final AuthTokenFilter authTokenFilter;
      private final JwtAuthenticationEntryPoint unauthorizedHandler;

      public SecurityConfig(IUserService userService, PasswordEncoder passwordEncoder, AuthTokenFilter authTokenFilter,
                  JwtAuthenticationEntryPoint unauthorizedHandler) {
            this.userService = userService;
            this.passwordEncoder = passwordEncoder;
            this.authTokenFilter = authTokenFilter;
            this.unauthorizedHandler = unauthorizedHandler;
      }

      @Bean
      public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
            return authConfig.getAuthenticationManager();
      }

      @Bean
      public DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userService);
            authProvider.setPasswordEncoder(passwordEncoder);
            return authProvider;
      }

      @Bean
      public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                        .csrf(AbstractHttpConfigurer::disable)
                        .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                    .requestMatchers("/api/auth/**", "/h2-console/**", "/ws/**", "/ws-raw/**")
                                    .permitAll()
                                    .anyRequest().authenticated());

            http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
      }
}

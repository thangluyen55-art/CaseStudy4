package com.yourname.digishop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/", "/cards", "/products/**", "/login", "/register", "/error",
                                "/card-images/**", "/images/**", "/css/**", "/js/**", "/uploads/**").permitAll()
                        .requestMatchers("/admin/dashboard", "/admin/products/**", "/admin/coupons/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/cart/**", "/checkout/**", "/orders/**", "/wishlist/**", "/decks/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .formLogin(f -> f
                        .loginPage("/login")
                        .defaultSuccessUrl("/cards", false)   // quay lại trang đang định vào nếu có
                        .permitAll())
                .logout(l -> l.logoutSuccessUrl("/cards?logout"));
        return http.build();
    }
}
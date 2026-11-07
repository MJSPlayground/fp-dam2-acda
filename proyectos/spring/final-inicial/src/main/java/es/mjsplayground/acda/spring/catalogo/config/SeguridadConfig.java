package es.mjsplayground.acda.spring.catalogo.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SeguridadConfig {
  @Bean
  @Profile("!seguro")
  SecurityFilterChain local(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(a -> a.anyRequest().permitAll())
        .csrf(Customizer.withDefaults())
        .build();
  }

  @Bean
  @Profile("seguro")
  SecurityFilterChain segura(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            a ->
                a.requestMatchers("/estilo.css", "/favicon.png", "/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/**")
                    .hasRole("GESTOR")
                    .anyRequest()
                    .authenticated())
        .formLogin(Customizer.withDefaults())
        .logout(l -> l.permitAll())
        .csrf(Customizer.withDefaults())
        .build();
  }

  @Bean
  @Profile("seguro")
  UserDetailsService usuarios() {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    return new InMemoryUserDetailsManager(
        User.withUsername("lector")
            .password(
                "{bcrypt}"
                    + encoder.encode(
                        System.getenv().getOrDefault("CATALOGO_LECTOR_PASSWORD", "lector_local")))
            .roles("LECTOR")
            .build(),
        User.withUsername("gestor")
            .password(
                "{bcrypt}"
                    + encoder.encode(
                        System.getenv().getOrDefault("CATALOGO_GESTOR_PASSWORD", "gestor_local")))
            .roles("GESTOR")
            .build());
  }
}

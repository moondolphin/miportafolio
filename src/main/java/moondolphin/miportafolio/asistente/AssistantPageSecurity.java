package moondolphin.miportafolio.asistente;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

@Configuration(proxyBeanMethods = false)
class AssistantPageSecurity {
    @Bean
    @Order(1)
    SecurityFilterChain assistantPageFilterChain(HttpSecurity http) throws Exception {
        return http.securityMatcher(new OrRequestMatcher(
                        new AntPathRequestMatcher("/asistente/index.html", "GET"),
                        new AntPathRequestMatcher("/asistente/asistente.css", "GET"),
                        new AntPathRequestMatcher("/asistente/asistente.js", "GET")))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}

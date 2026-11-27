package uk.gov.mockserver;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * Spring Boot's authorization server security, except that the login form is {@link LoginPageController}.
 * Defining any chain switches off Boot's, so both are declared here as Boot 4.0 declares them.
 */
@Configuration
public class SecurityConfig {

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
    OAuth2AuthorizationServerConfigurer authorizationServer = new OAuth2AuthorizationServerConfigurer();
    http.securityMatcher(authorizationServer.getEndpointsMatcher())
        .with(authorizationServer, withDefaults())
        .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());
    http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(withDefaults());
    http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(withDefaults()));
    http.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
        new LoginUrlAuthenticationEntryPoint("/login"), htmlRequests()));
    return http.build();
  }

  @Bean
  @Order(Ordered.LOWEST_PRECEDENCE - 5)
  SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
        .formLogin(form -> form.loginPage("/login").permitAll());
    return http.build();
  }

  private static MediaTypeRequestMatcher htmlRequests() {
    MediaTypeRequestMatcher matcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
    matcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
    return matcher;
  }
}

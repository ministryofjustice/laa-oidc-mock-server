
package uk.gov.mockserver;

import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class IdTokenCustomizerConfig {

  @Bean
  public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
    return context -> {
      if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
        OidcUserInfo userInfo = createUser(context.getPrincipal().getName());
        context.getClaims().claims(claims ->
            claims.putAll(userInfo.getClaims()));
      }
    };
  }


  /**
   * Examples
   *
   * @param username user name
   * @return OidcUserInfo
   */
  private OidcUserInfo createUser(String username) {
    return OidcUserInfo.builder()
        .subject(username)
        .name("PENNY WALL")
        .givenName("PENNY")
        .familyName("WALL")
        .preferredUsername(username)
        .picture("https://govuk.com/" + username + ".jpg")
        .email("PENNY.WALL@SWITALSKIS.COM")
        .emailVerified(true)
        .birthdate("1970-01-01")
        .phoneNumber("+1 (604) 555-1234;ext=5678")
        .phoneNumberVerified(false)
        .claim("address", Collections.singletonMap("formatted",
            "Champ de Mars\n5 Av. Anatole France\n75007 Paris\nFrance"))
        .updatedAt("2025-01-01T00:00:00Z")
        .build();
  }
}
package uk.gov.mockserver;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class IdTokenCustomizerConfig {

  private final Map<String, MockUsers.User> usersByUsername;
  private final List<String> defaultLaaAccounts;
  private final String defaultFirmCode;
  private final String defaultFirmName;

  public IdTokenCustomizerConfig(
      MockUsers mockUsers,
      @Value("${LAA_ACCOUNTS:1,123,0P322F}") List<String> laaAccounts,
      @Value("${FIRM_CODE:1234}") String firmCode,
      @Value("${FIRM_NAME:TEST FIRM NAME}") String firmName) {
    this.usersByUsername =
        mockUsers.users().stream()
            .collect(java.util.stream.Collectors.toMap(MockUsers.User::username, Function.identity()));
    this.defaultLaaAccounts = List.copyOf(laaAccounts);
    this.defaultFirmCode = firmCode;
    this.defaultFirmName = firmName;
  }

  @Bean
  public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
    return context -> {
      if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
        OidcUserInfo userInfo = createUser(context.getPrincipal().getName());
        context.getClaims().claims(claims -> claims.putAll(userInfo.getClaims()));
      }
    };
  }

  OidcUserInfo createUser(String username) {
    MockUsers.User user = usersByUsername.get(username);
    String email = user == null ? username : user.emailOrUsername();
    return OidcUserInfo.builder()
        .subject(username)
        .name(valueOr(user == null ? null : user.name(), "PROVIDER USER"))
        .givenName(valueOr(user == null ? null : user.givenName(), "PROVIDER"))
        .familyName(valueOr(user == null ? null : user.familyName(), "USER"))
        .preferredUsername(username)
        .picture("https://govuk.com/" + username + ".jpg")
        .email(email)
        .emailVerified(true)
        .birthdate("1970-01-01")
        .phoneNumber("+1 (604) 555-1234;ext=5678")
        .phoneNumberVerified(false)
        .claim("address", Collections.singletonMap("formatted",
            "Champ de Mars\n5 Av. Anatole France\n75007 Paris\nFrance"))
        .claim("LAA_ACCOUNTS",
            user == null || user.laaAccounts() == null || user.laaAccounts().isEmpty()
                ? defaultLaaAccounts
                : List.copyOf(user.laaAccounts()))
        .claim("FIRM_NAME", valueOr(user == null ? null : user.firmName(), defaultFirmName))
        .claim("FIRM_CODE", valueOr(user == null ? null : user.firmCode(), defaultFirmCode))
        .updatedAt("2025-01-01T00:00:00Z")
        .build();
  }

  private static String valueOr(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }
}

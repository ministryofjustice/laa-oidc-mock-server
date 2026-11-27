package uk.gov.mockserver;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The users this mock will sign in, and the claims each one's token carries.
 *
 * <p>More than one is needed because the services under test sign in as several people: the claims
 * have to differ per user, or every sign-in resolves to the same person downstream.
 */
@ConfigurationProperties("mock")
public record MockUsers(List<User> users) {

  /** A single sign-in. Everything but the username and password falls back to a default. */
  public record User(
      String username,
      String password,
      String email,
      String name,
      String givenName,
      String familyName,
      String firmCode,
      String firmName,
      List<String> laaAccounts) {

    /**
     * The address the token carries. Defaults to the username, because the application resolves an
     * internal user by email and so needs a different one per user.
     */
    public String emailOrUsername() {
      return email == null || email.isBlank() ? username : email;
    }
  }
}

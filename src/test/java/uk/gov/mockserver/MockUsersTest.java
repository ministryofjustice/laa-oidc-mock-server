package uk.gov.mockserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
    properties = {
      "mock.users[0].username=provider.user@provider.com",
      "mock.users[0].password=password",
      "mock.users[1].username=multi.firm.user@provider.com",
      "mock.users[1].password=other-password",
      "mock.users[1].firm-code=9999",
      "mock.users[1].firm-name=OTHER FIRM",
      "mock.users[1].laa-accounts=7,8"
    })
@DisplayName("Mock users test")
class MockUsersTest {

  private static final Pattern CSRF = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");

  @Autowired private WebApplicationContext context;
  @Autowired private IdTokenCustomizerConfig idTokenCustomizer;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("Signs in every configured user, not only the first")
  void signsInEveryConfiguredUser() throws Exception {
    assertSignsIn("provider.user@provider.com", "password");
    assertSignsIn("multi.firm.user@provider.com", "other-password");
  }

  @Test
  @DisplayName("Refuses a password that belongs to another user")
  void refusesTheWrongPassword() throws Exception {
    MockHttpSession session = new MockHttpSession();
    mockMvc
        .perform(
            post("/login")
                .session(session)
                .param("_csrf", csrfToken(session))
                .param("username", "multi.firm.user@provider.com")
                .param("password", "password"))
        .andExpect(redirectedUrl("/login?error"));
  }

  @Test
  @DisplayName("Gives each user their own email, so they resolve to different people downstream")
  void emailFollowsTheUser() {
    assertThat(idTokenCustomizer.createUser("provider.user@provider.com").getEmail())
        .isEqualTo("provider.user@provider.com");
    assertThat(idTokenCustomizer.createUser("multi.firm.user@provider.com").getEmail())
        .isEqualTo("multi.firm.user@provider.com");
  }

  @Test
  @DisplayName("Gives each user their own firm and accounts, falling back to the defaults")
  void firmFollowsTheUser() {
    OidcUserInfo first = idTokenCustomizer.createUser("provider.user@provider.com");
    OidcUserInfo second = idTokenCustomizer.createUser("multi.firm.user@provider.com");

    assertThat(first.getClaimAsString("FIRM_CODE")).isEqualTo("1234");
    assertThat(first.getClaimAsString("FIRM_NAME")).isEqualTo("TEST FIRM NAME");
    assertThat(second.getClaimAsString("FIRM_CODE")).isEqualTo("9999");
    assertThat(second.getClaimAsString("FIRM_NAME")).isEqualTo("OTHER FIRM");
    assertThat(second.getClaimAsStringList("LAA_ACCOUNTS")).containsExactly("7", "8");
  }

  private void assertSignsIn(String username, String password) throws Exception {
    MockHttpSession session = new MockHttpSession();
    mockMvc
        .perform(
            post("/login")
                .session(session)
                .param("_csrf", csrfToken(session))
                .param("username", username)
                .param("password", password))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
  }

  private String csrfToken(MockHttpSession session) throws Exception {
    String body =
        mockMvc
            .perform(get("/login").session(session))
            .andReturn()
            .getResponse()
            .getContentAsString();
    Matcher matcher = CSRF.matcher(body);
    assertThat(matcher.find()).isTrue();
    return matcher.group(1);
  }
}

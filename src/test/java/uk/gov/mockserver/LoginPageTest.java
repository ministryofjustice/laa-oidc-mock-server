package uk.gov.mockserver;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class LoginPageTest {

  private static final Pattern CSRF = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void loginPageCarriesTheCsrfTokenAndTheSingleSubmitGuard() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(content().string(containsString("name=\"_csrf\" value=\"")))
        .andExpect(content().string(containsString("this.dataset.submitted")));
  }

  @Test
  void loginPageShowsTheErrorMessage() throws Exception {
    mockMvc.perform(get("/login").param("error", ""))
        .andExpect(content().string(containsString("Invalid username or password.")));
  }

  @Test
  void signInSucceedsOnceAndARepeatedSubmitIsRejected() throws Exception {
    MockHttpSession session = new MockHttpSession();
    String token = csrfToken(session);

    mockMvc.perform(post("/login").session(session).param("_csrf", token)
            .param("username", "provider.user@provider.com").param("password", "password"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));

    mockMvc.perform(post("/login").session(session).param("_csrf", token)
            .param("username", "provider.user@provider.com").param("password", "password"))
        .andExpect(status().isForbidden());
  }

  @Test
  void wrongPasswordReturnsToTheLoginPage() throws Exception {
    MockHttpSession session = new MockHttpSession();
    mockMvc.perform(post("/login").session(session).param("_csrf", csrfToken(session))
            .param("username", "provider.user@provider.com").param("password", "wrong"))
        .andExpect(redirectedUrl("/login?error"));
  }

  @Test
  void browserAuthorizeRequestIsSentToTheLoginPage() throws Exception {
    mockMvc.perform(get("/oauth2/authorize?response_type=code&client_id=bulk-claim-client"
            + "&scope=openid&redirect_uri=http://127.0.0.1:8082/login/oauth2/code/silas-identity"
            + "&code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&code_challenge_method=S256")
            .accept(MediaType.TEXT_HTML))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", endsWith("/login")));
  }

  @Test
  void oidcDiscoveryIsStillPublished() throws Exception {
    mockMvc.perform(get("/.well-known/openid-configuration"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("\"userinfo_endpoint\"")));
  }

  private String csrfToken(MockHttpSession session) throws Exception {
    String page = mockMvc.perform(get("/login").session(session))
        .andReturn().getResponse().getContentAsString();
    Matcher matcher = CSRF.matcher(page);
    if (!matcher.find()) {
      throw new AssertionError("No CSRF token on the login page");
    }
    return matcher.group(1);
  }
}

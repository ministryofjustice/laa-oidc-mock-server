package uk.gov.mockserver;

import static org.springframework.web.util.HtmlUtils.htmlEscape;

import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login form that ignores a second submit. Sign-in rotates the CSRF token, so a repeated POST
 * (double click, or a password manager submitting as well) fails with 403 and replaces the
 * redirect back to the client that the first POST started.
 */
@RestController
public class LoginPageController {

  private static final String PAGE = """
      <!DOCTYPE html>
      <html lang="en">
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Please sign in</title>
        <style>
          body { font-family: arial, sans-serif; margin: 0; padding: 2rem 1rem; }
          main { max-width: 24rem; margin: 0 auto; }
          label { display: block; margin-top: 1rem; font-weight: bold; }
          input { box-sizing: border-box; width: 100%%; padding: .5rem; font-size: 1rem; }
          button { margin-top: 1.5rem; padding: .5rem 1rem; font-size: 1rem; }
          button:disabled { opacity: .6; }
          .message { padding: .75rem; margin-bottom: 1rem; border-left: 4px solid; }
        </style>
      </head>
      <body>
      <main>
        <h1>Please sign in</h1>
        %s
        <form id="login" method="post" action="/login">
          <label for="username">Username</label>
          <input type="text" id="username" name="username" autocomplete="username" required autofocus>
          <label for="password">Password</label>
          <input type="password" id="password" name="password" autocomplete="current-password" required>
          <input type="hidden" name="%s" value="%s">
          <button type="submit">Sign in</button>
        </form>
      </main>
      <script>
        document.getElementById('login').addEventListener('submit', function (event) {
          if (this.dataset.submitted) {
            event.preventDefault();
            return;
          }
          this.dataset.submitted = 'true';
          this.querySelector('button').disabled = true;
        });
      </script>
      </body>
      </html>
      """;

  @GetMapping(path = "/login", produces = MediaType.TEXT_HTML_VALUE)
  public String login(CsrfToken csrfToken,
                      @RequestParam(required = false) String error,
                      @RequestParam(required = false) String logout) {
    String message = "";
    if (error != null) {
      message = "<p class=\"message\" role=\"alert\">Invalid username or password.</p>";
    } else if (logout != null) {
      message = "<p class=\"message\" role=\"status\">You have been signed out.</p>";
    }
    return PAGE.formatted(message, htmlEscape(csrfToken.getParameterName()),
        htmlEscape(csrfToken.getToken()));
  }
}

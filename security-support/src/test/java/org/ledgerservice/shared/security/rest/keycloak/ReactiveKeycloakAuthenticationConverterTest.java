package org.ledgerservice.shared.security.rest.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ledgerservice.shared.security.TestContainerImages;
import org.ledgerservice.shared.security.rest.keycloak.ReactiveKeycloakAuthenticationConverterTest.ReactiveKeycloakAuthenticationConverterTestConfiguration;
import org.ledgerservice.shared.security.rest.keycloak.ReactiveKeycloakAuthenticationConverterTest.ReactiveKeycloakAuthenticationConverterTestInitializer;
import org.ledgerservice.spring.support.LocalTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ContextConfiguration(classes = ReactiveKeycloakAuthenticationConverterTestConfiguration.class,
  initializers = ReactiveKeycloakAuthenticationConverterTestInitializer.class)
@Slf4j
public class ReactiveKeycloakAuthenticationConverterTest {

  private static final String CLIENT_ID = "scan-request-client";
  private static final String CLIENT_SECRET = "12345678";

  @Container
  private static final GenericContainer<?> KEYCLOAK_CONTAINER
    = new GenericContainer<>(TestContainerImages.KEYCLOAK_NAME)
    .withClasspathResourceMapping("keycloak/dataplatform-external.json",
      "/opt/keycloak/data/import/dataplatform-external.json",
      BindMode.READ_ONLY)
    .withClasspathResourceMapping("keycloak/dataplatform-internal.json",
      "/opt/keycloak/data/import/dataplatform-internal.json",
      BindMode.READ_ONLY)
    .withExposedPorts(8080)
    .withCommand("start-dev --import-realm")
    .waitingFor(Wait.forHttp("/realms/dataplatform-internal/protocol/openid-connect/certs")
      .forPort(8080)
      .withStartupTimeout(Duration.of(240, ChronoUnit.SECONDS)));

  @Autowired
  private ReactiveJwtKeycloakAuthenticationConverter keycloakAuthenticationConverter;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Validator validator;

  @Autowired
  private JwtDecoder jwtDecoder;

  private Jwt jwt;

  @BeforeEach
  void obtainAccessToken() throws Exception {
    final OkHttpClient httpClient = new OkHttpClient();

    final Request request = new Request.Builder()
      .url(String.format(
        "http://%s:%d/realms/dataplatform-internal/protocol/openid-connect/token",
        KEYCLOAK_CONTAINER.getHost(),
        KEYCLOAK_CONTAINER.getMappedPort(8080)))
      .post(new FormBody.Builder()
        .add("grant_type", "client_credentials")
        .add("client_id", CLIENT_ID)
        .add("client_secret", CLIENT_SECRET)
        .add("scope", "openid")
        .build())
      .cacheControl(new CacheControl.Builder()
        .noCache()
        .build())
      .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful()
        && StringUtils.isNotBlank(HttpHeaders.CONTENT_TYPE)
        && MediaType.parseMediaType(response.header(HttpHeaders.CONTENT_TYPE))
        .isCompatibleWith(MediaType.APPLICATION_JSON)) {
        try (ResponseBody body = response.body()) {
          final TokenResponse tokenResponse = objectMapper.readerFor(TokenResponse.class)
            .readValue(body.byteStream());
          final Set<ConstraintViolation<TokenResponse>> violations = validator.validate(tokenResponse);

          if (violations.isEmpty()) {
            log.info("Retrieved token response {}", tokenResponse);

            jwt = jwtDecoder.decode(tokenResponse.getAccessToken());
          } else {
            throw new IllegalStateException("Cannot load token response, validations failed: " + violations);
          }
        }
      } else {
        throw new IllegalStateException("Cannot load token response: " + response.code());
      }
    }
  }

  @Test
  void shouldExtractGrantedAuthorities() {
    StepVerifier.create(keycloakAuthenticationConverter.convert(jwt)
        .map(grantedAuthority -> grantedAuthority.getAuthority())
        .collect(Collectors.toSet()))
      .expectNextMatches(grantedAuthorities -> {
        try {
          Assertions.assertThat(grantedAuthorities)
            .containsExactlyInAnyOrder(
              "REALM_ROLE_UMA_AUTHORIZATION",
              "REALM_ROLE_OFFLINE_ACCESS",
              "REALM_ROLE_DEFAULT_ROLES_DATAPLATFORM_INTERNAL",
              "REALM_ROLE_SCAN_REQUEST");
          return true;
        } catch (Throwable throwable) {
          log.error("Assertion failed", throwable);

          return false;
        }
      })
      .verifyComplete();
  }

  @LocalTestConfiguration
  @EnableAutoConfiguration
  public static class ReactiveKeycloakAuthenticationConverterTestConfiguration {

    @Bean
    public ReactiveJwtKeycloakAuthenticationConverter kerycloakAuthenticationConverter() {
      return new ReactiveJwtKeycloakAuthenticationConverter();
    }

    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper()
        .registerModule(new JavaTimeModule());
    }

    @Bean
    public Validator validator() {
      return Validation
        .buildDefaultValidatorFactory()
        .getValidator();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
      final NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
        .withJwkSetUri(String.format("http://%s:%d/realms/dataplatform-internal/protocol/openid-connect/certs",
          KEYCLOAK_CONTAINER.getHost(),
          KEYCLOAK_CONTAINER.getMappedPort(8080)))
        .build();

      jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>());

      return jwtDecoder;
    }
  }

  public static class ReactiveKeycloakAuthenticationConverterTestInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      TestPropertyValues.of(
        String.format(
          "spring.security.oauth2.resourceserver.jwt.jwk-set-uri: http://%s:%d/realms/dataplatform-internal/protocol/openid-connect/certs",
          KEYCLOAK_CONTAINER.getHost(),
          KEYCLOAK_CONTAINER.getMappedPort(8080))
      ).applyTo(applicationContext);
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TokenResponse {

    @JsonProperty("access_token")
    @NotNull
    @NotEmpty
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("token_type")
    @NotNull
    @NotEmpty
    private String tokenType;

    @JsonProperty("expires_in")
    @Min(1)
    private int expiresIn;

    @JsonProperty("refresh_expires_in")
    @Min(0)
    private int refreshExpiresIn;

    @JsonProperty("scope")
    @NotNull
    @NotEmpty
    private String scope;

    @JsonProperty("session_state")
    private String sessionState;

    @JsonProperty("not-before-policy")
    @Min(0)
    private int notBeforePolicy;
  }
}

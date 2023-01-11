package org.ledgerservice.shared.security.rest.common;

import static org.ledgerservice.shared.security.rest.common.RestSecuritySupportCommonConfigurationSingleTenantTest.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
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
import org.ledgerservice.shared.security.rest.keycloak.ReactiveJwtKeycloakAuthenticationConverter;
import org.ledgerservice.shared.security.rest.keycloak.ReactiveKeycloakAuthenticationConverterTest.TokenResponse;
import org.ledgerservice.spring.support.LocalTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = RestSecuritySupportCommonConfigurationTestConfiguration.class,
  initializers = RestSecuritySupportCommonConfigurationTestInitializer.class)
@Testcontainers(disabledWithoutDocker = true)
@Slf4j
public class RestSecuritySupportCommonConfigurationSingleTenantTest {

  private static final String CLIENT_ID = "scan-request-client";
  private static final String CLIENT_SECRET = "12345678";
  private static final String DATAPLATFORM_PATH = "/dataplatform/check-avail";
  private static final String OTHER_PATH = "/other/prohibited";

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

  @LocalServerPort
  private int serverPort;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Validator validator;

  @Autowired
  @Qualifier("internalJwtDecoder")
  private JwtDecoder internalJwtDecoder;

  @Autowired
  @Qualifier("externalJwtDecoder")
  private JwtDecoder externalJwtDecoder;

  @Autowired
  private WebClient webClient;

  private Jwt internalJwt;

  private Jwt externalJwt;

  private URI baseUri;

  @BeforeEach
  void obtainInternalAccessToken() throws Exception {
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

            internalJwt = internalJwtDecoder.decode(tokenResponse.getAccessToken());

            log.info("Using internal token value: {}", internalJwt.getTokenValue());
          } else {
            throw new IllegalStateException("Cannot load token response, validations failed: " + violations);
          }
        }
      } else {
        throw new IllegalStateException("Cannot load token response: " + response.code());
      }
    }
  }

  @BeforeEach
  void obtainExternalAccessToken() throws Exception {
    final OkHttpClient httpClient = new OkHttpClient();

    final Request request = new Request.Builder()
      .url(String.format(
        "http://%s:%d/realms/dataplatform-external/protocol/openid-connect/token",
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

            externalJwt = externalJwtDecoder.decode(tokenResponse.getAccessToken());

            log.info("Using external token value: {}", externalJwt.getTokenValue());
          } else {
            throw new IllegalStateException("Cannot load token response, validations failed: " + violations);
          }
        }
      } else {
        throw new IllegalStateException("Cannot load token response: " + response.code());
      }
    }
  }

  @BeforeEach
  void assignBaseUri() {
    baseUri = UriComponentsBuilder.newInstance()
      .scheme("http")
      .host("localhost")
      .port(serverPort)
      .build()
      .toUri();
  }

  @Test
  public void shouldNotReachOtherEndpoint() {
    StepVerifier.create(webClient.get()
        .uri(UriComponentsBuilder.fromUri(baseUri)
          .path(OTHER_PATH)
          .build()
          .toUri())
        .retrieve()
        .toBodilessEntity())
      .expectError(WebClientResponseException.Unauthorized.class)
      .verify();
  }

  @Test
  public void shouldNotReachDataplatformEndpointWithoutJwt() {
    StepVerifier.create(webClient.get()
        .uri(UriComponentsBuilder.fromUri(baseUri)
          .path(DATAPLATFORM_PATH)
          .build()
          .toUri())
        .retrieve()
        .toBodilessEntity())
      .expectError(WebClientResponseException.Unauthorized.class)
      .verify();

  }

  @Test
  public void shouldReachDataplatformEndpointWithInternalJwt() {
    StepVerifier.create(webClient.get()
        .uri(UriComponentsBuilder.fromUri(baseUri)
          .path(DATAPLATFORM_PATH)
          .build()
          .toUri())
        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", internalJwt.getTokenValue()))
        .retrieve()
        .toBodilessEntity())
      .expectNextMatches(response -> {
        try {
          Assertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.OK);
          return true;
        } catch (Throwable throwable) {
          log.error("Assertion failed", throwable);

          return false;
        }
      })
      .verifyComplete();
  }

  @Test
  public void shouldNotReachDataplatformEndpointWithExternalJwt() {
    StepVerifier.create(webClient.get()
        .uri(UriComponentsBuilder.fromUri(baseUri)
          .path(DATAPLATFORM_PATH)
          .build()
          .toUri())
        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", externalJwt.getTokenValue()))
        .retrieve()
        .toBodilessEntity())
      .expectError(WebClientResponseException.Unauthorized.class)
      .verify();
  }

  @LocalTestConfiguration
  @EnableAutoConfiguration
  @Import(RestSecuritySupportCommonConfiguration.class)
  static class RestSecuritySupportCommonConfigurationTestConfiguration {

    @Bean
    public ReactiveJwtKeycloakAuthenticationConverter keycloakAuthenticationConverter() {
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
    public RouterFunction<ServerResponse> endpointRoutes() {
      return RouterFunctions.route()
        .GET(DATAPLATFORM_PATH,
          request -> ServerResponse
            .ok()
            .build())
        .GET(OTHER_PATH,
          request -> ServerResponse
            .ok()
            .build())
        .build();
    }

    @Bean("internalJwtDecoder")
    public JwtDecoder internalJwtDecoder() {
      final NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
        .withJwkSetUri(String.format("http://%s:%d/realms/dataplatform-internal/protocol/openid-connect/certs",
          KEYCLOAK_CONTAINER.getHost(),
          KEYCLOAK_CONTAINER.getMappedPort(8080)))
        .build();

      jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>());

      return jwtDecoder;
    }

    @Bean("externalJwtDecoder")
    public JwtDecoder externalJwtDecoder() {
      final NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
        .withJwkSetUri(String.format("http://%s:%d/realms/dataplatform-external/protocol/openid-connect/certs",
          KEYCLOAK_CONTAINER.getHost(),
          KEYCLOAK_CONTAINER.getMappedPort(8080)))
        .build();

      jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<Jwt>());

      return jwtDecoder;
    }

    @Bean
    public WebClient webClient() {
      return WebClient.builder().build();
    }
  }

  static class RestSecuritySupportCommonConfigurationTestInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      TestPropertyValues.of(
        "app.rest.security.common.required-role=scan-request",
        String.format("app.rest.security.common.api-path=%s", DATAPLATFORM_PATH),
        String.format(
          "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://%s:%d/realms/dataplatform-internal",
          KEYCLOAK_CONTAINER.getHost(),
          KEYCLOAK_CONTAINER.getMappedPort(8080))
      ).applyTo(applicationContext);
    }
  }
}

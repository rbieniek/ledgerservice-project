package org.ledgerservice.shared.security.rest.keycloak;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClaimConverterBaseTest {
  private TestClaimConverter converter;

  @BeforeEach
  public void before() {
    converter = new TestClaimConverter();
  }

  @Test
  public void shouldReturnEmptySetOnNullObject() {
    assertThat(converter.execute(null, "any"))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptySetOnStringObject() {
    assertThat(converter.execute("test", "any"))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnEmptyList() {
    assertThat(converter.execute(emptyList(), "any"))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnEmptyMap() {
    assertThat(converter.execute(emptyMap(), "any"))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnMapWithString() {
    assertThat(converter.execute(singletonMap("any", "value"), "any"))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnMapWithEmptyCollection() {
    assertThat(converter.execute(singletonMap("any", emptyList()), "any"))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnMapWithPopulatedCollection() {
    assertThat(converter.execute(singletonMap("any", singletonList("foo")), "any"))
        .isNotNull()
        .containsOnly("foo");
  }

  @Test
  public void shouldReturnEmptyOnMapWithEmptyListAndEmptyPath() {
    assertThat(converter.execute(singletonMap("any", singletonMap("path", emptyList())), "any"))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnMapWithEmptyMap() {
    assertThat(converter.execute(singletonMap("any", singletonMap("path", emptyMap())), "any.path"))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnMapWithMapWithString() {
    assertThat(converter.execute(singletonMap("any", singletonMap("path", "value")), "any.path"))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnMapWithMapWithPopulatedCollection() {
    assertThat(converter.execute(singletonMap("any", singletonMap("path", singletonList("foo"))), "any.path"))
        .isNotNull()
        .containsOnly("foo");
  }

  @Test
  public void shouldReturnEmptySetOnNullObjectWithEmptyPath() {
    assertThat(converter.execute(null, ""))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptySetOnStringObjectWithEmptyPath() {
    assertThat(converter.execute("test", ""))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnEmptyListWithEmptyPath() {
    assertThat(converter.execute(emptyList(), ""))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnEmptyOnEmptyMapWithEmptyPath() {
    assertThat(converter.execute(emptyMap(), ""))
        .isNotNull()
        .isEmpty();
  }

  @Test
  public void shouldReturnPopulaterOnPopulatedListWithEmptyPath() {
    assertThat(converter.execute(singletonList("value"), ""))
        .isNotNull()
        .containsOnly("value");
  }

  private static class TestClaimConverter extends ClaimConverterBase {
    public Set<String> execute(final Object source, final String pathName) {
      return super.deconstructStructuredClaim(source, pathName);
    }

    @Override
    public String getTargetClaimName() {
      return null;
    }
  }
}

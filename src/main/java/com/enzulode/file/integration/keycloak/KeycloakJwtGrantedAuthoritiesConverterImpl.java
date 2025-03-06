package com.enzulode.file.integration.keycloak;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public class KeycloakJwtGrantedAuthoritiesConverterImpl implements Converter<Jwt, Collection<? extends GrantedAuthority>> {

  private static final Configuration JSONPATH_CONFIG_NO_EXCEPTIONS =
      Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);

  @Override
  public Collection<? extends GrantedAuthority> convert(Jwt jwt) {
    @SuppressWarnings("unchecked")
    Function<String, ? extends Stream<String>> claimMapper = (claimPath) -> {
      Object claim = JsonPath.using(JSONPATH_CONFIG_NO_EXCEPTIONS)
          .parse(jwt.getClaims())
          .read(claimPath);

      // if nothing found then return an empty stream
      if (claim == null) {
        return Stream.empty();
      }

      // if got claim of String type return a string stream
      if (claim instanceof String claimStr) {
        return Stream.of(claimStr.split(","));
      }

      // if got claim of String[] type return a stream of String[]
      if (claim instanceof String[] claimArr) {
        return Stream.of(claimArr);
      }

      if (Collection.class.isAssignableFrom(claim.getClass())) {
        final var collection = (Collection<?>) claim;
        final var iter = collection.iterator();

        // if the collection is empty return empty stream
        if (!iter.hasNext()) {
          return Stream.empty();
        }

        final var firstItem = iter.next();
        // if the collection items have String type return them as String stream
        if (firstItem instanceof String) {
          return (Stream<String>) collection.stream();
        }

        // if the collection items have Collection type try to cast them to Collection type, stream them and flatmap
        if (Collection.class.isAssignableFrom(firstItem.getClass())) {
          return (Stream<String>) collection.stream()
              .flatMap(claimItemCollection -> ((Collection<?>) claimItemCollection).stream())
              .map(String.class::cast);
        }
      }

      return Stream.empty();
    };

    return Stream.of("$.realm_access.roles", "$.resource_access.*.roles")
        .flatMap(claimMapper)
        .map(String::toUpperCase)
        .map(r -> "ROLE_" + r)
        .map(SimpleGrantedAuthority::new)
        .map(GrantedAuthority.class::cast)
        .toList();
  }
}

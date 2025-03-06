package com.enzulode.file.integration.keycloak;

import com.jayway.jsonpath.JsonPath;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class KeycloakAuthenticationConverterImpl implements Converter<Jwt, AbstractAuthenticationToken> {

  private final Converter<Jwt, Collection<? extends GrantedAuthority>> jwtToGAConverter;

  public KeycloakAuthenticationConverterImpl() {
    this.jwtToGAConverter = new KeycloakJwtGrantedAuthoritiesConverterImpl();
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    final var authorities = jwtToGAConverter.convert(jwt);
    final String username = JsonPath.read(jwt.getClaims(), "preferred_username");
    return new JwtAuthenticationToken(jwt, authorities, username);
  }
}

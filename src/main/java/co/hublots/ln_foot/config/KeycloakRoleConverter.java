package co.hublots.ln_foot.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {

        Object realmAccessClaim = jwt.getClaims().get("realm_access");

        if (!(realmAccessClaim instanceof Map<?, ?>)) {
            return List.of();
        }

        Map<?, ?> realmAccessMap = (Map<?, ?>) realmAccessClaim;
        Object rolesObject = realmAccessMap.get("roles");

        if (!(rolesObject instanceof List<?>)) {
            return List.of();
        }

        List<?> roles = (List<?>) rolesObject;

        return roles.stream()
                .map(role -> {
                    if (role instanceof String) {
                        return "ROLE_" + ((String) role).toUpperCase();
                    } else {
                        throw new IllegalArgumentException("Role is not a string: " + role);
                    }
                })
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

package co.hublots.ln_foot.resolvers;



import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import co.hublots.ln_foot.annotations.KeycloakUserId;

@Component
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(KeycloakUserId.class) &&
               parameter.getParameterType().equals(String.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, 
                                  ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, 
                                  WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof KeycloakAuthenticationToken) {
            KeycloakAuthenticationToken keycloakToken = (KeycloakAuthenticationToken) authentication;
            KeycloakPrincipal<?> principal = (KeycloakPrincipal<?>) keycloakToken.getPrincipal();
            AccessToken accessToken = principal.getKeycloakSecurityContext().getToken();
            String userId = accessToken.getSubject();
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("UserId not found in token");
            }
            return userId;
        }
        throw new IllegalStateException("User is not authenticated with Keycloak");
    }
}

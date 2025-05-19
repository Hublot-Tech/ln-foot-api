package co.hublots.ln_foot.resolvers;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import co.hublots.ln_foot.annotations.KeycloakUserId;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
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
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication: {}", authentication);

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String userId = jwtAuth.getToken().getSubject(); // JWT 'sub' claim
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("User ID (sub) not found in JWT token");
            }
            return userId;
        }

        throw new IllegalStateException("Unsupported authentication type: " + authentication.getClass().getName());
    }
}

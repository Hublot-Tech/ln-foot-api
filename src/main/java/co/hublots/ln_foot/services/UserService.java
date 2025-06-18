package co.hublots.ln_foot.services;

import java.util.List;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;

import co.hublots.ln_foot.dto.UserDto;

public interface UserService {
    List<UserDto> listUsers(String role);
    Optional<UserDto> findUserById(String id);
    Optional<UserDto> findUserByEmail(String email);
    UserDto updateUserRole(String userId, String newRole);
    void deleteUser(String id);
    Optional<UserDto> getCurrentUser();
    Optional<UserDto> findOrCreateUserFromJwt(Jwt jwt);
}

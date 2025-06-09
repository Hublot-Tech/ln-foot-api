package co.hublots.ln_foot.services;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> listUsers(String role);
    Optional<UserDto> findUserById(String id);
    // Optional<UserDto> findUserByEmail(String email); // Might be useful
    UserDto updateUserRole(String id, UpdateUserRoleDto updateUserRoleDto);
    void deleteUser(String id);
    // Optional<UserDto> getCurrentUser(); // For fetching profile of logged-in user
}

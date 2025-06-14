package co.hublots.ln_foot.services.impl;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.models.User;
import co.hublots.ln_foot.models.User.ValidRolesEnum;
import co.hublots.ln_foot.repositories.UserRepository;
import co.hublots.ln_foot.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private UserDto mapToDto(User entity) {
        if (entity == null) {
            return null;
        }
        String name = (entity.getFirstName() != null ? entity.getFirstName() : "") +
                (entity.getLastName() != null ? " " + entity.getLastName() : "");
        name = name.trim();

        return UserDto.builder()
                .id(entity.getId())
                .keycloakId(entity.getKeycloakId())
                .email(entity.getEmail())
                .name(name.isEmpty() ? null : name)
                .avatarUrl(entity.getAvatarUrl())
                .role(entity.getRole())
                .permissions(Collections.emptyList())
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> listUsers(String role) {
        List<User> users;
        if (role != null && !role.isEmpty()) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findUserById(String id) {
        // Also consider findByKeycloakId if 'id' can be keycloakId
        return userRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public UserDto updateUserRole(String userId, String newRole) { // Signature updated
        if (newRole == null || newRole.isBlank()) {
            throw new IllegalArgumentException("Role cannot be null or blank.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));

        if (!ValidRolesEnum.isValidRole(newRole)) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }

        user.setRole(newRole);

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + id + " not found"));

        // Check if the user is an admin
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            long adminCount = userRepository.countByRole("ADMIN");
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last admin user.");
            }
        }

        userRepository.save(user);
    }

    @Override
    public Optional<UserDto> findUserByEmail(String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findUserByEmail'");
    }

    @Override
    public Optional<UserDto> getCurrentUser() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentUser'");
    }
}

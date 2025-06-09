package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.models.User;
import co.hublots.ln_foot.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    private User createMockUser(String id, String role, String firstName, String lastName) {
        return User.builder()
                .id(id)
                .keycloakId(UUID.randomUUID().toString())
                .username(firstName.toLowerCase() + "." + lastName.toLowerCase())
                .email(firstName.toLowerCase() + "@example.com")
                .firstName(firstName)
                .lastName(lastName)
                .avatarUrl("http://avatar.url/" + firstName)
                .role(role)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void listUsers_noRole_returnsAllUserDtos() {
        // Arrange
        User user1 = createMockUser(UUID.randomUUID().toString(), "ADMIN", "Admin", "User");
        User user2 = createMockUser(UUID.randomUUID().toString(), "USER", "Normal", "Joe");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<UserDto> result = userService.listUsers(null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Admin User")));
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Normal Joe")));
        verify(userRepository).findAll();
        verify(userRepository, never()).findByRole(anyString());
    }

    @Test
    void listUsers_withRole_returnsFilteredUserDtos() {
        // Arrange
        String roleToFilter = "ADMIN";
        User adminUser = createMockUser(UUID.randomUUID().toString(), roleToFilter, "Super", "Admin");
        // User otherUser = createMockUser(UUID.randomUUID().toString(), "USER", "Basic", "User"); // Not returned by mock
        when(userRepository.findByRole(roleToFilter)).thenReturn(List.of(adminUser));

        // Act
        List<UserDto> result = userService.listUsers(roleToFilter);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Super Admin", result.get(0).getName());
        assertEquals(roleToFilter, result.get(0).getRole());
        verify(userRepository).findByRole(roleToFilter);
        verify(userRepository, never()).findAll();
    }

    @Test
    void listUsers_withRole_returnsEmptyListIfNoneMatch() {
        // Arrange
        String roleToFilter = "EDITOR";
        when(userRepository.findByRole(roleToFilter)).thenReturn(Collections.emptyList());

        // Act
        List<UserDto> result = userService.listUsers(roleToFilter);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findByRole(roleToFilter);
    }


    @Test
    void findUserById_whenFound_returnsOptionalDto() {
        // Arrange
        String id = UUID.randomUUID().toString();
        User mockUser = createMockUser(id, "USER", "Test", "Person");
        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));

        // Act
        Optional<UserDto> result = userService.findUserById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUser.getFirstName() + " " + mockUser.getLastName(), result.get().getName());
        assertEquals(mockUser.getRole(), result.get().getRole());
        verify(userRepository).findById(id);
    }

    @Test
    void findUserById_whenNotFound_returnsEmptyOptional() {
        // Arrange
        String id = "nonexistent-id";
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<UserDto> result = userService.findUserById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(id);
    }

    @Test
    void updateUserRole_whenUserFound_updatesAndReturnsDto() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User existingUser = createMockUser(userId, "USER", "Initial", "Role");
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().role("ADMIN").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // Simulate @UpdateTimestamp behavior
            userToSave.setUpdatedAt(LocalDateTime.now());
            return userToSave;
        });

        // Act
        UserDto resultDto = userService.updateUserRole(userId, updateDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals(userId, resultDto.getId());
        assertEquals("ADMIN", resultDto.getRole());
        // Check that updatedAt is recent (or different from original if original was far back)
        assertTrue(resultDto.getUpdatedAt().isAfter(existingUser.getUpdatedAt().atOffset(ZoneOffset.UTC).minusSeconds(1)));


        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("ADMIN", userCaptor.getValue().getRole());
        verify(userRepository).findById(userId);
    }

    @Test
    void updateUserRole_whenUserNotFound_throwsEntityNotFoundException() {
        // Arrange
        String userId = "nonexistent-id";
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().role("ADMIN").build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.updateUserRole(userId, updateDto));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_whenDtoRoleIsNull_doesNotChangeRole() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User existingUser = createMockUser(userId, "USER", "Keep", "Role");
        // DTO role is null
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().permissions(List.of("test_perm")).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser); // Return the same user

        // Act
        UserDto resultDto = userService.updateUserRole(userId, updateDto);

        // Assert
        assertNotNull(resultDto);
        assertEquals("USER", resultDto.getRole()); // Role should not have changed
        verify(userRepository).save(existingUser);
    }


    @Test
    void deleteUser_whenUserFound_deletesUser() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // Assert
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_whenUserNotFound_throwsEntityNotFoundException() {
        // Arrange
        String userId = "nonexistent-id";
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(anyString());
    }
}

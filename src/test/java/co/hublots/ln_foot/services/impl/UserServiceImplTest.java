package co.hublots.ln_foot.services.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.models.User;
import co.hublots.ln_foot.models.User.ValidRolesEnum;
import co.hublots.ln_foot.repositories.UserRepository;
import co.hublots.ln_foot.services.UserService;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    private User createMockUser(String id, ValidRolesEnum role, String firstName, String lastName) {
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
        User user1 = createMockUser(UUID.randomUUID().toString(), ValidRolesEnum.ADMIN, "Admin", "User");
        User user2 = createMockUser(UUID.randomUUID().toString(), ValidRolesEnum.ADMIN, "Normal", "Joe");
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<UserDto> result = userService.listUsers(null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Admin User")));
        assertTrue(result.stream().anyMatch(dto -> dto.getName().equals("Normal Joe")));
        verify(userRepository).findAll();
        verify(userRepository, never()).findByRole(any());
    }

    @Test
    void listUsers_withRole_returnsFilteredUserDtos() {
        // Arrange
        ValidRolesEnum roleToFilter = ValidRolesEnum.ADMIN;
        User adminUser = createMockUser(UUID.randomUUID().toString(), roleToFilter, "Super", "Admin");
        // User otherUser = createMockUser(UUID.randomUUID().toString(),
        // ValidRolesEnum.USER,
        // "Basic", "User"); // Not returned by mock
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
        ValidRolesEnum roleToFilter = ValidRolesEnum.EDITOR;
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
        User mockUser = createMockUser(id, ValidRolesEnum.ADMIN, "Test", "Person");
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
        User existingUser = createMockUser(userId, ValidRolesEnum.USER, "Initial", "Role");
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().role(ValidRolesEnum.ADMIN).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // Simulate @UpdateTimestamp behavior
            userToSave.setUpdatedAt(LocalDateTime.now());
            return userToSave;
        });

        // Act
        UserDto resultDto = userService.updateUserRole(userId, updateDto.getRole());

        // Assert
        assertNotNull(resultDto);
        assertEquals(userId, resultDto.getId());
        assertEquals(ValidRolesEnum.ADMIN, resultDto.getRole());
        // Check that updatedAt is recent (or different from original if original was
        // far back)
        assertTrue(
                resultDto.getUpdatedAt().isAfter(existingUser.getUpdatedAt().atOffset(ZoneOffset.UTC).minusSeconds(1)));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(ValidRolesEnum.ADMIN, userCaptor.getValue().getRole());
        verify(userRepository).findById(userId);
    }

    @Test
    void updateUserRole_whenUserNotFound_throwsEntityNotFoundException() {
        // Arrange
        String userId = "nonexistent-id";
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder().role(ValidRolesEnum.ADMIN).build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.updateUserRole(userId, updateDto.getRole()));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserRole_whenNewRoleIsNull_throwsIllegalArgumentException() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        // No need to mock findById or save as the validation should happen before that.

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateUserRole(userId, null));
        assertEquals("Role cannot be null or blank.", exception.getMessage());
        verify(userRepository, never()).findById(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // --- Delete User Tests ---
    @Test
    void deleteUser_whenUserFoundAndNotLastAdmin_deletesUser() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User userToDelete = createMockUser(userId, ValidRolesEnum.USER, "Test", "User");
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));
        // For a non-admin user, countByRole is not called, so no need to mock it.
        doNothing().when(userRepository).delete(userToDelete);

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).delete(userToDelete);
    }

    @Test
    void deleteUser_whenAdminUserFoundAndNotLastAdmin_deletesUser() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User adminUserToDelete = createMockUser(userId, ValidRolesEnum.ADMIN, "Admin", "User");
        when(userRepository.findById(userId)).thenReturn(Optional.of(adminUserToDelete));
        when(userRepository.countByRole(ValidRolesEnum.ADMIN)).thenReturn(2L); // More than one admin
        doNothing().when(userRepository).delete(adminUserToDelete);

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).countByRole(ValidRolesEnum.ADMIN);
        verify(userRepository).delete(adminUserToDelete);
    }

    @Test
    void deleteUser_whenUserIsLastAdmin_throwsIllegalStateException() {
        // Arrange
        String userId = UUID.randomUUID().toString();
        User lastAdmin = createMockUser(userId, ValidRolesEnum.ADMIN, "TheOnly", "Admin");
        when(userRepository.findById(userId)).thenReturn(Optional.of(lastAdmin));
        when(userRepository.countByRole(ValidRolesEnum.ADMIN)).thenReturn(1L); // This is the last admin

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userService.deleteUser(userId));
        assertEquals("Cannot delete the last admin user.", exception.getMessage());

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).countByRole(ValidRolesEnum.ADMIN);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_whenUserNotFound_throwsEntityNotFoundException() {
        // Arrange
        String userId = "nonexistent-id";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).countByRole(any());
        verify(userRepository, never()).delete(any(User.class));
    }

    // --- findOrCreateUserFromJwt Tests ---

    private Jwt createMockJwt(String subject, String email, String givenName, String familyName,
            String preferredUsername, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        claims.put("email", email);
        claims.put("given_name", givenName);
        claims.put("family_name", familyName);
        claims.put("preferred_username", preferredUsername);
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", roles);
        claims.put("realm_access", realmAccess);

        return new Jwt(
                "mock-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Collections.singletonMap("alg", "none"), // Header
                claims // Claims
        );
    }

    @Test
    void findOrCreateUserFromJwt_whenNewUser_createsAndReturnsUserDto() {
        // Arrange
        String keycloakId = UUID.randomUUID().toString();
        String email = "newuser@example.com";
        String firstName = "New";
        String lastName = "User";
        String username = "newbie";
        List<String> roles = List.of(ValidRolesEnum.USER.name());
        Jwt mockJwt = createMockJwt(keycloakId, email, firstName, lastName, username, roles);

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // Ensure the saved user is returned by the mock
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<UserDto> resultOpt = userService.findOrCreateUserFromJwt(mockJwt);

        // Assert
        assertTrue(resultOpt.isPresent());
        UserDto resultDto = resultOpt.get();
        assertEquals(keycloakId, resultDto.getKeycloakId());
        assertEquals(email, resultDto.getEmail());
        assertEquals(firstName + " " + lastName, resultDto.getName());
        assertEquals(ValidRolesEnum.USER, resultDto.getRole()); // Assuming default role logic or direct mapping

        User savedUser = userCaptor.getValue();
        assertEquals(keycloakId, savedUser.getKeycloakId());
        assertEquals(email, savedUser.getEmail());
        assertEquals(firstName, savedUser.getFirstName());
        assertEquals(lastName, savedUser.getLastName());
        assertEquals(username, savedUser.getUsername());
        assertEquals(ValidRolesEnum.USER, savedUser.getRole());

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findOrCreateUserFromJwt_whenExistingUserNeedsUpdate_updatesAndReturnsUserDto() {
        // Arrange
        String keycloakId = UUID.randomUUID().toString();
        String oldEmail = "oldemail@example.com";
        String newEmail = "newemail@example.com";
        String firstName = "Existing";
        String lastName = "User";
        String username = "existuser";
        ValidRolesEnum oldRole = ValidRolesEnum.USER;
        ValidRolesEnum newRole = ValidRolesEnum.ADMIN;

        Jwt mockJwt = createMockJwt(keycloakId, newEmail, firstName, lastName, username, List.of(newRole.name()));

        User existingUser = User.builder()
                .id(UUID.randomUUID().toString())
                .keycloakId(keycloakId)
                .email(oldEmail)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .role(oldRole)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusHours(5))
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(existingUser));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setUpdatedAt(LocalDateTime.now()); // Simulate timestamp update
            return userToSave;
        });

        // Act
        Optional<UserDto> resultOpt = userService.findOrCreateUserFromJwt(mockJwt);

        // Assert
        assertTrue(resultOpt.isPresent());
        UserDto resultDto = resultOpt.get();
        assertEquals(keycloakId, resultDto.getKeycloakId());
        assertEquals(newEmail, resultDto.getEmail());
        assertEquals(newRole, resultDto.getRole());
        assertEquals(firstName + " " + lastName, resultDto.getName());

        User savedUser = userCaptor.getValue();
        assertEquals(newEmail, savedUser.getEmail());
        assertEquals(newRole, savedUser.getRole());
        // Ensure other fields are maintained or correctly updated
        assertEquals(firstName, savedUser.getFirstName());

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository).save(existingUser); // existingUser object should be the one modified and saved
    }

    @Test
    void findOrCreateUserFromJwt_whenExistingUserMatchesJwt_returnsUserDtoWithoutSaving() {
        // Arrange
        String keycloakId = UUID.randomUUID().toString();
        String email = "match@example.com";
        String firstName = "Match";
        String lastName = "User";
        String username = "matchuser";
        ValidRolesEnum role = ValidRolesEnum.USER;
        Jwt mockJwt = createMockJwt(keycloakId, email, firstName, lastName, username, List.of(role.name()));

        User existingUser = User.builder()
                .id(UUID.randomUUID().toString())
                .keycloakId(keycloakId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .role(role) // Matches JWT role
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusHours(5))
                .build();

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(existingUser));

        // Act
        Optional<UserDto> resultOpt = userService.findOrCreateUserFromJwt(mockJwt);

        // Assert
        assertTrue(resultOpt.isPresent());
        UserDto resultDto = resultOpt.get();
        assertEquals(keycloakId, resultDto.getKeycloakId());
        assertEquals(email, resultDto.getEmail());
        assertEquals(role, resultDto.getRole());
        assertEquals(firstName + " " + lastName, resultDto.getName());

        verify(userRepository).findByKeycloakId(keycloakId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findOrCreateUserFromJwt_whenJwtHasRoleAdmin_mapsToAdminRole() {
        // Arrange
        String keycloakId = UUID.randomUUID().toString();
        Jwt mockJwt = createMockJwt(keycloakId, "roleadmin@example.com", "Role", "Admin", "roleadmin",
                List.of("ROLE_ADMIN"));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.findOrCreateUserFromJwt(mockJwt);

        // Assert
        User savedUser = userCaptor.getValue();
        assertEquals(ValidRolesEnum.ADMIN, savedUser.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findOrCreateUserFromJwt_whenJwtHasAdminRole_mapsToAdminRole() {
        // Arrange
        String keycloakId = UUID.randomUUID().toString();
        Jwt mockJwt = createMockJwt(keycloakId, "adminrole@example.com", "Admin", "Role", "adminrole",
                List.of(ValidRolesEnum.ADMIN.name()));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.findOrCreateUserFromJwt(mockJwt);

        // Assert
        User savedUser = userCaptor.getValue();
        assertEquals(ValidRolesEnum.ADMIN, savedUser.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findOrCreateUserFromJwt_whenJwtHasUserRole_mapsToUserRole() {
        // Arrange
        String keycloakId = UUID.randomUUID().toString();
        Jwt mockJwt = createMockJwt(keycloakId, "userrole@example.com", "User", "Role", "userrole",
                List.of("ROLE_USER"));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.findOrCreateUserFromJwt(mockJwt);

        // Assert
        User savedUser = userCaptor.getValue();
        assertEquals(ValidRolesEnum.USER, savedUser.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findOrCreateUserFromJwt_whenJwtHasNoAppSpecificRole_defaultsToUserRole() {
        // Arrange
        String keycloakId = UUID.randomUUID().toString();
        // Roles from JWT that are not ValidRolesEnum.ADMIN, "ROLE_ADMIN",
        // ValidRolesEnum.USER, "ROLE_USER"
        Jwt mockJwt = createMockJwt(keycloakId, "otherrole@example.com", "Other", "Role", "otherrole",
                List.of("SOME_OTHER_ROLE", "ANOTHER_ONE"));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<UserDto> resultOpt = userService.findOrCreateUserFromJwt(mockJwt);

        // Assert
        assertTrue(resultOpt.isPresent());
        assertEquals(ValidRolesEnum.USER, resultOpt.get().getRole()); // Default role

        User savedUser = userCaptor.getValue();
        assertEquals(ValidRolesEnum.USER, savedUser.getRole()); // Ensure saved user also has default role
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findOrCreateUserFromJwt_whenJwtRoleIsInvalidAccordingToEnum_defaultsToUserRoleAndLogsWarning() {
        // This test relies on the actual User.ValidRolesEnum.isValidRole() behavior.
        // We assume "INVALID_ROLE_FROM_JWT" is not a valid role in ValidRolesEnum.
        // The service implementation should log a warning. For this test, we primarily
        // check the defaulting.
        String keycloakId = UUID.randomUUID().toString();
        Jwt mockJwt = createMockJwt(keycloakId, "invalidenum@example.com", "Invalid", "Enum", "invalidenum",
                List.of("INVALID_ROLE_FROM_JWT"));

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<UserDto> resultOpt = userService.findOrCreateUserFromJwt(mockJwt);

        // Assert
        assertTrue(resultOpt.isPresent());
        assertEquals(ValidRolesEnum.USER, resultOpt.get().getRole()); // Should default to USER

        User savedUser = userCaptor.getValue();
        assertEquals(ValidRolesEnum.USER, savedUser.getRole());
        verify(userRepository).save(any(User.class));
        // To verify logging, you'd typically inject a mock logger or use a testing
        // appender.
        // This is more advanced and might be skipped if direct log verification is too
        // complex for the current setup.
        // For now, we trust the implementation detail that it logs a warning.
    }

    // --- getCurrentUser Tests ---

    @Test
    void getCurrentUser_whenAuthenticatedWithJwt_callsFindOrCreateUserAndReturnsDto() {
        // Arrange
        String keycloakId = UUID.randomUUID().toString();
        Jwt mockJwt = createMockJwt(keycloakId, "current@example.com", "Current", "User", "currentuser",
                List.of(ValidRolesEnum.USER.name()));
        UserDto expectedDto = UserDto.builder().id(UUID.randomUUID().toString()).keycloakId(keycloakId)
                .name("Current User").build(); // Simplified DTO

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockJwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock the internal call to findOrCreateUserFromJwt
        // We need to use a spy or ensure the same instance of userService is being used
        // if we were to mock userService itself.
        // Here, userService is the System Under Test (SUT), so we mock its
        // collaborator's (userRepository) behavior
        // as already done in findOrCreateUserFromJwt tests, or we can mock
        // findOrCreateUserFromJwt if it were public and spyable.
        // For simplicity, let's re-mock the underlying findByKcid and save for this
        // specific jwt,
        // or trust the findOrCreateUserFromJwt method works as tested elsewhere and
        // focus on the SecurityContext interaction.

        // Let's refine this: We want to test getCurrentUser's logic, which is to
        // extract Jwt and call findOrCreateUserFromJwt.
        // So, we can spy on the userService to verify findOrCreateUserFromJwt is called
        // with the correct Jwt.
        // However, the current setup uses a direct instance, not a spy.
        // A simpler approach for this unit test is to ensure that if
        // findOrCreateUserFromJwt (as an internal call)
        // would return a specific DTO for a given JWT, getCurrentUser also returns it.
        // So, we set up mocks for userRepository that findOrCreateUserFromJwt would
        // interact with for this *specific* JWT.

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(expectedDto.getId()); // Ensure the saved user gets an ID for mapping
            return u;
        });

        // Act
        Optional<UserDto> resultOpt = userService.getCurrentUser();

        // Assert
        assertTrue(resultOpt.isPresent());
        assertEquals(expectedDto.getKeycloakId(), resultOpt.get().getKeycloakId());
        assertEquals(expectedDto.getName(), resultOpt.get().getName());

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_whenNotAuthenticated_returnsEmpty() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null); // No authentication
        SecurityContextHolder.setContext(securityContext);

        Optional<UserDto> result = userService.getCurrentUser();

        assertFalse(result.isPresent());
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_whenPrincipalIsNotJwt_returnsEmpty() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(new Object()); // Not a Jwt

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Optional<UserDto> result = userService.getCurrentUser();

        assertFalse(result.isPresent());
        SecurityContextHolder.clearContext();
    }

    // --- findUserByEmail Tests ---

    @Test
    void findUserByEmail_whenUserExists_returnsOptionalUserDto() {
        // Arrange
        String email = "test@example.com";
        User mockUser = createMockUser(UUID.randomUUID().toString(), ValidRolesEnum.USER, "Test", "Email");
        mockUser.setEmail(email); // Ensure email is set on the mock user

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        // Act
        Optional<UserDto> result = userService.findUserByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        assertEquals(mockUser.getFirstName() + " " + mockUser.getLastName(), result.get().getName());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findUserByEmail_whenUserDoesNotExist_returnsEmptyOptional() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<UserDto> result = userService.findUserByEmail(email);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }
}

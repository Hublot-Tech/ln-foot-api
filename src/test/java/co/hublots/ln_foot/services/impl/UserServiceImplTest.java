package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
    }

    @Test
    void listUsers_returnsEmptyList() {
        List<UserDto> result = userService.listUsers("ADMIN");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findUserById_returnsEmptyOptional() {
        Optional<UserDto> result = userService.findUserById("user1");
        assertNotNull(result);
        // As with TeamServiceImplTest, this tests the general mock behavior.
        // The current UserServiceImpl mock doesn't have a specific ID that returns data.
        assertFalse(result.isPresent());
    }

    @Test
    void updateUserRole_returnsMockDtoWithUpdatedRole() {
        String userId = "userToUpdate";
        UpdateUserRoleDto updateDto = UpdateUserRoleDto.builder()
                .role("SUPER_ADMIN")
                .permissions(Collections.singletonList("ALL"))
                .build();
        UserDto result = userService.updateUserRole(userId, updateDto);
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("SUPER_ADMIN", result.getRole());
        assertFalse(result.getPermissions().isEmpty());
        assertEquals("ALL", result.getPermissions().get(0));
        assertNotNull(result.getUpdatedAt());
        // Check that createdAt is some time in the past (mocked original creation)
        assertTrue(result.getCreatedAt().isBefore(result.getUpdatedAt()));
    }

    @Test
    void deleteUser_completesWithoutError() {
        assertDoesNotThrow(() -> userService.deleteUser("userToDelete"));
    }
}

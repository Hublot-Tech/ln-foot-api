package co.hublots.ln_foot.controllers;

import co.hublots.ln_foot.dto.UpdateUserRoleDto;
import co.hublots.ln_foot.dto.UserDto;
import co.hublots.ln_foot.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> listUsers(@RequestParam(required = false) String role) {
        return userService.listUsers(role);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> findUserById(@PathVariable String id) {
        // For /me endpoint, a separate method would be better e.g. getCurrentUser()
        // if ("me".equalsIgnoreCase(id)) {
        //     return userService.getCurrentUser()
        //            .map(ResponseEntity::ok)
        //            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        // }
        return userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable String id, @RequestBody UpdateUserRoleDto updateUserRoleDto) {
        UserDto updatedUser = userService.updateUserRole(id, updateUserRoleDto);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        // Add checks: e.g., admin cannot delete self unless specific conditions
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Example for a /me endpoint if needed:
    /*
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Should be accessible by any logged-in user
    public ResponseEntity<UserDto> getCurrentUser() {
        return userService.getCurrentUser()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()); // Or NotFound if user somehow not in DB
    }
    */
}

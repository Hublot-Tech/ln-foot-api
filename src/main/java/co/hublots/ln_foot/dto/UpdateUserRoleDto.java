package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleDto {
    // userId is typically passed as a path parameter, e.g., /users/{userId}/role
    // So, it might not be needed in the DTO body.
    // private String userId;
    private String role; // The new role to assign
    private List<String> permissions; // Optional: if permissions are updated alongside role
}

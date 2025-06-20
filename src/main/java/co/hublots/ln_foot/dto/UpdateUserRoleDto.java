package co.hublots.ln_foot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

import co.hublots.ln_foot.annotations.ValidEnum;
import co.hublots.ln_foot.models.User.ValidRolesEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleDto {
    @NotBlank(message = "Role must be provided.")
    @ValidEnum(enumClass = ValidRolesEnum.class, message = "Invalid role provided.")
    private ValidRolesEnum role;

    private List<String> permissions; // Optional: if permissions are updated alongside role. Could add @Size if needed.
}

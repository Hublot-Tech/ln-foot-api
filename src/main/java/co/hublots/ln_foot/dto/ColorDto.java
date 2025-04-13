package co.hublots.ln_foot.dto;

import jakarta.validation.constraints.NotBlank;
import co.hublots.ln_foot.models.Color;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorDto {
    private Long id;

    @NotBlank(message = "Color name is required")
    private String name;

    public static ColorDto from(Color color) {
        return ColorDto.builder()
                .id(color.getId())
                .name(color.getName())
                .build();
    }
}
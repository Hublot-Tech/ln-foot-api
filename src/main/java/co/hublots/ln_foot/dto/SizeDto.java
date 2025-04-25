package co.hublots.ln_foot.dto;



import co.hublots.ln_foot.models.Size;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SizeDto {
    private String id;

    @NotBlank(message = "Size name is required")
    private String name;

    public static SizeDto fromEntity(Size size) {
        return SizeDto.builder()
                .id(size.getId())
                .name(size.getName())
                .build();
    }

    public Size toEntity() {
        return Size.builder()
                .id(id)
                .name(name)
                .build();
    }
} 
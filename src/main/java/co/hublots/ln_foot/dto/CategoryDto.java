package co.hublots.ln_foot.dto;

import java.util.UUID;

import co.hublots.ln_foot.models.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private UUID id;

    @NotBlank(message = "Category name is required")
    private String name;

    public static CategoryDto fromEntity(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public Category toEntity() {
        return Category.builder()
                .id(id)
                .name(name)
                .build();
    }
} 
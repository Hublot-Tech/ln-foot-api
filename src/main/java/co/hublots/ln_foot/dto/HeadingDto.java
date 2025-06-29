package co.hublots.ln_foot.dto;

import co.hublots.ln_foot.models.Heading;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HeadingDto {
    private String id;

    @NotBlank(message = "Title is required")
    private String title;

    private String imageUrl;

    public static HeadingDto fromEntity(Heading heading) {
        return HeadingDto.builder()
                .id(heading.getId())
                .title(heading.getTitle())
                .imageUrl(heading.getImageUrl())
                .build();
    }

    public Heading toEntity() {
        return Heading.builder()
                .id(id)
                .title(title)
                .imageUrl(imageUrl)
                .build();
    }
}

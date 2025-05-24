package co.hublots.ln_foot.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BulkProductVariantDto {
    @Valid
    @NotNull(message = "Variants list is required")
    @NotEmpty(message = "Variants list cannot be empty")
    private List<ProductVariantDto> variants;
}

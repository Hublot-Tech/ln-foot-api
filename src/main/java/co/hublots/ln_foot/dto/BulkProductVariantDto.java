package co.hublots.ln_foot.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BulkProductVariantDto {
    @Size(min = 1)
    @Valid
    List<ProductVariantDto> variants;
}

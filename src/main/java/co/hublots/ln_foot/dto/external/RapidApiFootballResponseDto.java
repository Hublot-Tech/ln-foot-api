package co.hublots.ln_foot.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RapidApiFootballResponseDto<T> {
    private String get; // The endpoint path that was called
    private Map<String, String> parameters;
    private List<Object> errors; // Can be List<String> or a more specific ErrorDto if structure is known
    private int results;
    private PagingDto paging;
    private List<T> response;
}

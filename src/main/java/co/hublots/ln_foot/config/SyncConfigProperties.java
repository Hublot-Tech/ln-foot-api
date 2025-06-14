package co.hublots.ln_foot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;
// Map is not needed for this structure, List of Objects is fine.
// import java.util.Map;

@Component
@ConfigurationProperties(prefix = "application.sync")
@Data
public class SyncConfigProperties {

    private List<InterestedLeague> interestedLeagues;

    @Data
    public static class InterestedLeague {
        private String name;
        private String country;
        // Optional: Add apiLeagueId if you want to pre-map them in config
        // private String apiLeagueId;
    }
}

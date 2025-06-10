package co.hublots.ln_foot.models.enums;

import java.util.Set;
import java.util.stream.Stream;

public enum FixtureStatus {
    TIME_TO_BE_DEFINED("TBD", "Time To Be Defined", false),
    NOT_STARTED("NS", "Not Started", false),
    FIRST_HALF("1H", "First Half", true),
    HALF_TIME("HT", "Halftime", true),
    SECOND_HALF("2H", "Second Half", true),
    EXTRA_TIME("ET", "Extra Time", true),
    PENALTY_SHOOTOUT("P", "Penalty In Progress", true),
    MATCH_FINISHED("FT", "Match Finished", false),
    MATCH_FINISHED_AFTER_EXTRA_TIME("AET", "Match Finished After Extra Time", false),
    MATCH_FINISHED_AFTER_PENALTY_SHOOTOUT("PEN", "Match Finished After Penalty", false),
    BREAK_TIME("BT", "Break Time (Extra Time)", true),
    SUSPENDED("SUSP", "Match Suspended", true), // isLive can be true if it might resume
    INTERRUPTED("INT", "Match Interrupted", true), // Same as SUSP
    POSTPONED("PST", "Match Postponed", false),
    CANCELLED("CANC", "Match Cancelled", false),
    ABANDONED("ABD", "Match Abandoned", false),
    TECHNICAL_LOSS("AWD", "Technical Loss", false),
    WALKOVER("WO", "WalkOver", false),
    LIVE("LIVE", "In Progress", true), // Generic LIVE status often provided by APIs
    UNKNOWN("UNKNOWN", "Unknown Status", false);

    private final String shortCode;
    private final String description;
    private final boolean isLive;

    FixtureStatus(String shortCode, String description, boolean isLive) {
        this.shortCode = shortCode;
        this.description = description;
        this.isLive = isLive;
    }

    public String getShortCode() { return shortCode; }
    public String getDescription() { return description; }
    public boolean isLive() { return isLive; }

    private static final Set<String> LIVE_STATUS_SHORT_CODES = Set.of(
            "1H", "HT", "2H", "ET", "P", "BT", "SUSP", "INT", "LIVE"
    );

    // More robust isLive check based on a set of codes, can be used if enum instance's isLive field isn't sufficient
    public static boolean isStatusLive(String shortCode) {
        if (shortCode == null) return false;
        return LIVE_STATUS_SHORT_CODES.contains(shortCode.toUpperCase());
    }

    public static FixtureStatus fromShortCode(String shortCode) {
        if (shortCode == null || shortCode.isBlank()) {
            return UNKNOWN;
        }
        String upperCode = shortCode.toUpperCase();
        return Stream.of(values())
                .filter(status -> status.shortCode.equals(upperCode))
                .findFirst()
                .orElseGet(() -> {
                    // Handle some common variations if necessary, though API should be consistent
                    if ("FT_PEN".equals(upperCode)) return PEN;
                    return UNKNOWN;
                });
    }
}

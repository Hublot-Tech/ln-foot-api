package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Color;
import java.util.List;
import java.util.UUID;

public interface ColorService {
    List<Color> getAllColors();

    Color getColorById(UUID id);

    Color createColor(Color color);

    Color updateColor(UUID id, Color color);

    void deleteColor(UUID id);
}
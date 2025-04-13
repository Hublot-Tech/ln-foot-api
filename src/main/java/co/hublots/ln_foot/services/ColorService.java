package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Color;
import java.util.List;

public interface ColorService {
    List<Color> getAllColors();

    Color getColorById(Long id);

    Color createColor(Color color);

    Color updateColor(Long id, Color color);

    void deleteColor(Long id);
}
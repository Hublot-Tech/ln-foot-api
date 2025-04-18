package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Color;
import java.util.List;


public interface ColorService {
    List<Color> getAllColors();

    Color getColorById(String id);

    Color createColor(Color color);

    Color updateColor(String id, Color color);

    void deleteColor(String id);
}
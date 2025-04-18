package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Size;
import java.util.List;


public interface SizeService {
    List<Size> getAllSizes();

    Size getSizeById(String id);

    Size createSize(Size size);

    Size updateSize(String id, Size size);

    void deleteSize(String id);
}
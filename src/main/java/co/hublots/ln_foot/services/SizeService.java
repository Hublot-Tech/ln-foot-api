package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Size;
import java.util.List;

public interface SizeService {
    List<Size> getAllSizes();

    Size getSizeById(Long id);

    Size createSize(Size size);

    Size updateSize(Long id, Size size);

    void deleteSize(Long id);
}
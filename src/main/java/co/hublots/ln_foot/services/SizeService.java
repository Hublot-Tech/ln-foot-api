package co.hublots.ln_foot.services;

import co.hublots.ln_foot.models.Size;
import java.util.List;
import java.util.UUID;

public interface SizeService {
    List<Size> getAllSizes();

    Size getSizeById(UUID id);

    Size createSize(Size size);

    Size updateSize(UUID id, Size size);

    void deleteSize(UUID id);
}
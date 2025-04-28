package co.hublots.ln_foot.services.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.hublots.ln_foot.models.Size;
import co.hublots.ln_foot.repositories.SizeRepository;
import co.hublots.ln_foot.services.SizeService;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    @Autowired
    private final SizeRepository sizeRepository;


    @Override
    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }

    @Override
    public Size getSizeById(String id) {
        return sizeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Size not found with id: " + id));
    }

    @Override
    public Size createSize(Size size) {
        return sizeRepository.save(size);
    }

    @Override
    public Size updateSize(String id, Size size) {
        Size existingSize = sizeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Size not found with id: " + id));
        Optional.of(size.getName()).ifPresent(existingSize::setName);
        return sizeRepository.save(existingSize);
    }

    @Override
    public void deleteSize(String id) {
        sizeRepository.deleteById(id);
    }
}
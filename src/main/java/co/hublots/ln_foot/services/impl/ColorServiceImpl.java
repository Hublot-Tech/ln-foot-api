package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.models.Color;
import co.hublots.ln_foot.repositories.ColorRepository;
import co.hublots.ln_foot.services.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ColorServiceImpl implements ColorService {

    private final ColorRepository colorRepository;

    @Autowired
    public ColorServiceImpl(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @Override
    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    @Override
    public Color getColorById(Long id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
    }

    @Override
    public Color createColor(Color color) {
        return colorRepository.save(color);
    }

    @Override
    public Color updateColor(Long id, Color color) {
        Color existingColor = colorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Color not found with id: " + id));
        existingColor.setName(color.getName());
        return colorRepository.save(existingColor);
    }

    @Override
    public void deleteColor(Long id) {
        colorRepository.deleteById(id);
    }
} 
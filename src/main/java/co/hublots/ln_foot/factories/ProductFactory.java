package co.hublots.ln_foot.factories;

import co.hublots.ln_foot.models.Product;

import java.math.BigDecimal;
import java.util.Random;

public class ProductFactory {

    private static final Random random = new Random();

    public static Product createSampleProduct() {
        String[] names = {"Football", "Jersey", "Short", "Socks", "Gloves"};
        String[] descriptions = {"High quality", "Official", "Limited edition", "New season"};

        String name = names[random.nextInt(names.length)] + " " + (random.nextInt(100) + 1);
        String description = descriptions[random.nextInt(descriptions.length)];
        BigDecimal price = BigDecimal.valueOf(random.nextDouble() * 100 + 20); // $20 to $120
        int stockQuantity = random.nextInt(200);

        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .imageUrl("http://example.com/image.jpg")
                .stockQuantity(stockQuantity)
                .build();
    }
}

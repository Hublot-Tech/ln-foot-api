package co.hublots.ln_foot.models;



import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "reviews")
@Data
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @UuidGenerator
    private String id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int rating;
    private String comment;
    private String keycloakUserId;
}
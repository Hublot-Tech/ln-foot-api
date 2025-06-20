package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.List; // Example if custom queries were added now

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, String> {
    // Example:
    // List<Advertisement> findByStatusAndPriorityOrderByEndDateAsc(String status, Integer priority);
}

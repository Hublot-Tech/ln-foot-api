package co.hublots.ln_foot.repositories;

import co.hublots.ln_foot.models.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, String> {
    List<NewsArticle> findByStatusOrderByPublicationDateDesc(String status);
    List<NewsArticle> findByCategoryAndStatusOrderByPublicationDateDesc(String category, String status);
    List<NewsArticle> findByAuthorIdAndStatusOrderByPublicationDateDesc(String authorId, String status);
    List<NewsArticle> findByPublicationDateBetweenOrderByPublicationDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    // Example for searching by tags if tags were a separate entity or using a more complex query for ElementCollection
    // @Query("SELECT na FROM NewsArticle na JOIN na.tags t WHERE t = :tag")
    // List<NewsArticle> findByTag(@Param("tag") String tag);
}

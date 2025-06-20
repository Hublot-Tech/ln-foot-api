package co.hublots.ln_foot.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.hublots.ln_foot.models.NewsArticle;
import co.hublots.ln_foot.models.NewsArticle.NewsStatus;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, String> {
    List<NewsArticle> findByStatusOrderByPublicationDateDesc(NewsStatus status);

}

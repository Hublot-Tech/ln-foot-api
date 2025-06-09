package co.hublots.ln_foot.services.impl;

import co.hublots.ln_foot.dto.CreateNewsArticleDto;
import co.hublots.ln_foot.dto.NewsArticleDto;
import co.hublots.ln_foot.dto.UpdateNewsArticleDto;
import co.hublots.ln_foot.dto.UserDto; // Required for mapping
import co.hublots.ln_foot.models.NewsArticle;
import co.hublots.ln_foot.models.User;
import co.hublots.ln_foot.repositories.NewsArticleRepository;
import co.hublots.ln_foot.repositories.UserRepository;
import co.hublots.ln_foot.services.NewsArticleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort; // Added for sorting
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsArticleServiceImpl implements NewsArticleService {

    private final NewsArticleRepository newsArticleRepository;
    private final UserRepository userRepository; // Inject UserRepository

    // Helper to map User entity to UserDto (could be in a shared mapper or UserServiceImpl if public)
    private UserDto mapUserToUserDto(User userEntity) {
        if (userEntity == null) {
            return null;
        }
        String name = (userEntity.getFirstName() != null ? userEntity.getFirstName() : "") +
                      (userEntity.getLastName() != null ? " " + userEntity.getLastName() : "");
        name = name.trim();

        return UserDto.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .name(name.isEmpty() ? null : name)
                .avatarUrl(userEntity.getAvatarUrl())
                .role(userEntity.getRole())
                .createdAt(userEntity.getCreatedAt() != null ? userEntity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(userEntity.getUpdatedAt() != null ? userEntity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .permissions(Collections.emptyList()) // Assuming User entity doesn't store these directly
                .emailVerified(null) // Assuming User entity doesn't store this directly
                .build();
    }

    private NewsArticleDto mapToDto(NewsArticle entity) {
        if (entity == null) {
            return null;
        }
        return NewsArticleDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .author(mapUserToUserDto(entity.getAuthor())) // Map User entity to UserDto
                .articleUrl(entity.getSourceUrl()) // DTO 'articleUrl' maps to entity's 'sourceUrl'
                // DTO 'sourceName' (was 'source') would map from a new field on entity if added, e.g. entity.getSourcePublicationName()
                .imageUrl(entity.getImageUrl())
                .publishedAt(entity.getPublicationDate() != null ? entity.getPublicationDate().atOffset(ZoneOffset.UTC) : null)
                .status(entity.getStatus())
                .tags(Collections.emptyList()) // 'tags' not in NewsArticle entity currently
                .createdAt(entity.getCreatedAt() != null ? entity.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    private void mapToEntityForCreate(CreateNewsArticleDto dto, NewsArticle entity) {
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());

        if (dto.getAuthorId() != null && !dto.getAuthorId().isBlank()) {
            User authorUser = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author (User) with ID " + dto.getAuthorId() + " not found."));
            entity.setAuthor(authorUser);
            entity.setAuthorName(null); // Clear text authorName if linked User is set
        } else {
            entity.setAuthor(null);
            // entity.setAuthorName(dto.getAuthor()); // If CreateNewsArticleDto still had 'author' string field
        }

        entity.setSourceUrl(dto.getUrl()); // DTO 'url' maps to entity's 'sourceUrl'
        // DTO 'source' (now sourceName in NewsArticleDto) is not mapped to a direct entity field.
        // Could map dto.getSource() to a new entity.sourcePublicationName if that field existed.
        entity.setImageUrl(dto.getImageUrl());
        entity.setPublicationDate(dto.getPublishedAt() != null ? dto.getPublishedAt().toLocalDateTime() : null);
        entity.setStatus(dto.getStatus());
        // entity.setCategory(); // 'category' not in CreateNewsArticleDto
        // entity.setTags(); // 'tags' not in NewsArticle entity
    }

    private void mapToEntityForUpdate(UpdateNewsArticleDto dto, NewsArticle entity) {
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }
        if (dto.getAuthorId() != null) {
            if (dto.getAuthorId().isBlank()) { // Explicitly making author null
                entity.setAuthor(null);
                entity.setAuthorName(null);
            } else {
                User authorUser = userRepository.findById(dto.getAuthorId())
                    .orElseThrow(() -> new EntityNotFoundException("Author (User) with ID " + dto.getAuthorId() + " not found."));
                entity.setAuthor(authorUser);
                entity.setAuthorName(null);
            }
        }
        // If UpdateNewsArticleDto had 'author' string:
        // else if (dto.getAuthor() != null) {
        //     entity.setAuthorName(dto.getAuthor());
        //     entity.setAuthor(null); // Clear linked user if text name is provided
        // }

        if (dto.getUrl() != null) {
            entity.setSourceUrl(dto.getUrl());
        }
        if (dto.getImageUrl() != null) {
            entity.setImageUrl(dto.getImageUrl());
        }
        if (dto.getPublishedAt() != null) {
            entity.setPublicationDate(dto.getPublishedAt().toLocalDateTime());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsArticleDto> listNewsArticles(String status, List<String> tags) {
        List<NewsArticle> articles;
        // Tags filtering is not implemented as entity doesn't support it yet.
        if (status != null && !status.isEmpty()) {
            articles = newsArticleRepository.findByStatusOrderByPublicationDateDesc(status);
        } else {
            // Default sort if no status specified
            articles = newsArticleRepository.findAll(Sort.by(Sort.Direction.DESC, "publicationDate"));
        }
        return articles.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NewsArticleDto> findNewsArticleById(String id) {
        return newsArticleRepository.findById(id).map(this::mapToDto);
    }

    @Override
    @Transactional
    public NewsArticleDto createNewsArticle(CreateNewsArticleDto createDto) {
        NewsArticle article = new NewsArticle();
        mapToEntityForCreate(createDto, article);
        NewsArticle savedArticle = newsArticleRepository.save(article);
        return mapToDto(savedArticle);
    }

    @Override
    @Transactional
    public NewsArticleDto updateNewsArticle(String id, UpdateNewsArticleDto updateDto) {
        NewsArticle article = newsArticleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("NewsArticle with ID " + id + " not found"));
        mapToEntityForUpdate(updateDto, article);
        NewsArticle updatedArticle = newsArticleRepository.save(article);
        return mapToDto(updatedArticle);
    }

    @Override
    @Transactional
    public void deleteNewsArticle(String id) {
        if (!newsArticleRepository.existsById(id)) {
            throw new EntityNotFoundException("NewsArticle with ID " + id + " not found");
        }
        newsArticleRepository.deleteById(id);
    }
}

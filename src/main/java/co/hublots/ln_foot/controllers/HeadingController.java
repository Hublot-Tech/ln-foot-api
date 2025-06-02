package co.hublots.ln_foot.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.hublots.ln_foot.annotations.KeycloakUserId;
import co.hublots.ln_foot.dto.HeadingDto;
import co.hublots.ln_foot.models.Heading;
import co.hublots.ln_foot.repositories.HeadingRepository;
import co.hublots.ln_foot.services.MinioService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/headings")
public class HeadingController {
    private final String bucketName = "headings";
    private final MinioService minioService;
    private final HeadingRepository headingRepository;

    @GetMapping
    public ResponseEntity<List<HeadingDto>> getHeadings() {
        List<Heading> headings = headingRepository.findAll();
        return new ResponseEntity<>(headings.stream().map(HeadingDto::fromEntity).collect(Collectors.toList()),
                HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeadingDto> getOrderById(@PathVariable String id) {
        Optional<Heading> heading = headingRepository.findById(id);
        if (heading.isPresent()) {
            return new ResponseEntity<>(
                    HeadingDto.fromEntity(heading.get()),
                    HttpStatus.OK);

        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HeadingDto> createHeading(@KeycloakUserId @Parameter(hidden = true) String userId,
            @Valid @RequestBody HeadingDto headingDto) {
        MultipartFile file = headingDto.getFile();
        if (file != null && !file.isEmpty()) {
            String imageUrl = minioService.uploadFile(bucketName, file);
            headingDto.setImageUrl(imageUrl);
        }
        Heading heading = headingRepository.save(headingDto.toEntity());

        return new ResponseEntity<>(
                HeadingDto.fromEntity(heading),
                HttpStatus.CREATED);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHeading(@PathVariable String id) {
        headingRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

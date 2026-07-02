package com.brochure.cms.domain.media;

import com.brochure.cms.config.AppProperties;
import com.brochure.cms.domain.tenant.Tenant;
import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.security.TenantContext;
import com.brochure.cms.shared.util.TenantIds;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);
    private static final long MAX_BYTES = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml");

    private final MediaRepository mediaRepository;
    private final AppProperties appProperties;
    private final Tika tika = new Tika();

    public MediaService(MediaRepository mediaRepository, AppProperties appProperties) {
        this.mediaRepository = mediaRepository;
        this.appProperties = appProperties;
    }

    public MediaDto upload(MultipartFile file, UUID uploaderId, String altText) throws IOException {
        validateUpload(file);
        Tenant tenant = TenantContext.get();
        if (tenant == null) {
            throw new ValidationException("Tenant could not be resolved");
        }

        UUID fileId = UUID.randomUUID();
        String extension = extensionFor(file.getOriginalFilename());
        YearMonth ym = YearMonth.now();
        String relativePath = tenant.getSlug() + "/" + ym.getYear() + "/" + ym.getMonthValue() + "/" + fileId + extension;
        Path target = Path.of(appProperties.media().root(), relativePath);
        Files.createDirectories(target.getParent());
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        }

        MediaFile media = new MediaFile();
        media.setTenantId(TenantIds.current());
        media.setUploaderId(uploaderId);
        media.setFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : fileId + extension);
        media.setStoragePath(relativePath);
        media.setMimeType(detectMimeType(file));
        media.setSizeBytes(file.getSize());
        media.setAltText(altText);
        mediaRepository.save(media);
        log.info("Uploaded media {} for tenant {}", media.getId(), tenant.getSlug());
        return MediaDto.from(media);
    }

    @Transactional(readOnly = true)
    public List<MediaDto> list() {
        return mediaRepository.findByTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(TenantIds.current()).stream()
                .map(MediaDto::from)
                .toList();
    }

    public void delete(UUID id) {
        MediaFile media = mediaRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, TenantIds.current())
                .orElseThrow(() -> new ResourceNotFoundException("Media file not found"));
        media.softDelete();
        mediaRepository.save(media);
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is required");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ValidationException("File exceeds 10 MB limit");
        }
        String detected = detectMimeType(file);
        if (!ALLOWED_TYPES.contains(detected)) {
            throw new ValidationException("File type not allowed: " + detected);
        }
    }

    private String detectMimeType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new ValidationException("Unable to detect file type");
        }
    }

    private String extensionFor(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    public record MediaDto(UUID id, String filename, String storagePath, String mimeType, long sizeBytes, String altText) {
        static MediaDto from(MediaFile media) {
            return new MediaDto(
                    media.getId(),
                    media.getFilename(),
                    media.getStoragePath(),
                    media.getMimeType(),
                    media.getSizeBytes(),
                    media.getAltText());
        }
    }
}

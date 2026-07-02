package com.brochure.cms.domain.category;

import com.brochure.cms.shared.exception.ResourceNotFoundException;
import com.brochure.cms.shared.exception.ValidationException;
import com.brochure.cms.shared.util.TenantIds;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final PostCategoryRepository postCategoryRepository;

    public CategoryService(CategoryRepository categoryRepository, PostCategoryRepository postCategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.postCategoryRepository = postCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeDto> listTree() {
        UUID tenantId = TenantIds.current();
        List<Category> all = categoryRepository.findByTenantIdAndDeletedAtIsNullOrderByNameAsc(tenantId);
        return buildTree(all, null);
    }

    @Transactional(readOnly = true)
    public CategoryDto getBySlug(String slug) {
        UUID tenantId = TenantIds.current();
        Category category = categoryRepository
                .findByTenantIdAndSlugAndDeletedAtIsNull(tenantId, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return CategoryDto.from(category);
    }

    public CategoryDto create(CreateCategoryRequest request) {
        UUID tenantId = TenantIds.current();
        Category category = new Category();
        category.setTenantId(tenantId);
        category.setParentId(request.parentId());
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        categoryRepository.save(category);
        return CategoryDto.from(category);
    }

    public CategoryDto update(UUID id, UpdateCategoryRequest request) {
        Category category = findForTenant(id);
        if (request.name() != null) {
            category.setName(request.name());
        }
        if (request.slug() != null) {
            category.setSlug(request.slug());
        }
        if (request.description() != null) {
            category.setDescription(request.description());
        }
        if (request.parentId() != null) {
            category.setParentId(request.parentId());
        }
        categoryRepository.save(category);
        return CategoryDto.from(category);
    }

    public void delete(UUID id) {
        Category category = findForTenant(id);
        if (postCategoryRepository.countByCategoryId(category.getId()) > 0) {
            throw new ValidationException("Reassign or remove posts from this category before deleting");
        }
        category.softDelete();
        categoryRepository.save(category);
    }

    public void assignPostCategories(UUID postId, List<UUID> categoryIds) {
        postCategoryRepository.deleteByPostId(postId);
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        UUID tenantId = TenantIds.current();
        for (UUID categoryId : categoryIds) {
            findForTenant(categoryId);
            PostCategory link = new PostCategory();
            link.setPostId(postId);
            link.setCategoryId(categoryId);
            postCategoryRepository.save(link);
        }
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> categoriesForPost(UUID postId) {
        return postCategoryRepository.findByPostId(postId).stream()
                .map(pc -> categoryRepository.findById(pc.getCategoryId()))
                .filter(opt -> opt.isPresent() && opt.get().getDeletedAt() == null)
                .map(opt -> CategoryDto.from(opt.get()))
                .toList();
    }

    private Category findForTenant(UUID id) {
        return categoryRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, TenantIds.current())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private List<CategoryTreeDto> buildTree(List<Category> all, UUID parentId) {
        List<CategoryTreeDto> result = new ArrayList<>();
        for (Category c : all) {
            boolean isRoot = parentId == null && c.getParentId() == null;
            boolean isChild = parentId != null && parentId.equals(c.getParentId());
            if (!isRoot && !isChild) {
                continue;
            }
            result.add(new CategoryTreeDto(
                    c.getId(),
                    c.getParentId(),
                    c.getName(),
                    c.getSlug(),
                    c.getDescription(),
                    buildTree(all, c.getId())));
        }
        return result;
    }

    public record CategoryDto(UUID id, UUID parentId, String name, String slug, String description) {
        static CategoryDto from(Category c) {
            return new CategoryDto(c.getId(), c.getParentId(), c.getName(), c.getSlug(), c.getDescription());
        }
    }

    public record CategoryTreeDto(
            UUID id, UUID parentId, String name, String slug, String description, List<CategoryTreeDto> children) {}

    public record CreateCategoryRequest(String name, String slug, String description, UUID parentId) {}

    public record UpdateCategoryRequest(String name, String slug, String description, UUID parentId) {}
}

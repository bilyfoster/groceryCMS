package com.brochure.cms.domain.blog;

import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ScheduledPublishTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduledPublishTask.class);

    private final BlogPostRepository blogPostRepository;

    public ScheduledPublishTask(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void publishDuePosts() {
        OffsetDateTime now = OffsetDateTime.now();
        List<BlogPost> due = blogPostRepository.findByPublishedFalseAndPublishedAtLessThanEqualAndDeletedAtIsNull(now);
        if (due.isEmpty()) {
            return;
        }
        for (BlogPost post : due) {
            post.setPublished(true);
            log.info("Published scheduled blog post id={} slug={}", post.getId(), post.getSlug());
        }
        blogPostRepository.saveAll(due);
    }
}

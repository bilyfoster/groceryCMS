package com.brochure.cms.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Schema-parity test for ALIGN-1 (Flyway → Liquibase).
 *
 * <p>Boots the full Spring context against a real PostgreSQL instance managed by
 * Testcontainers. Liquibase applies the changelog, then Hibernate validates the
 * resulting schema against the JPA entities ({@code ddl-auto: validate}).
 *
 * <p>If this test passes, the Liquibase changesets produce a schema equivalent
 * to the one the application expects.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("liquibase-test")
@Testcontainers(disabledWithoutDocker = true)
class LiquibaseSchemaParityIntegrationTest {

    private static final String POSTGRES_IMAGE = "postgres:16-alpine";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("brochure_cms_test")
            .withUsername("cms_test")
            .withPassword("cms_test");

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoadsAndAllLiquibaseChangesetsAreApplied() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        List<String> appliedIds = jdbc.queryForList(
                "SELECT id FROM databasechangelog ORDER BY dateexecuted, orderexecuted", String.class);

        assertThat(appliedIds).containsExactly(
                "ALIGN-1-001-init-schema",
                "ALIGN-1-002-add-indexes",
                "ALIGN-1-003-seed-data",
                "ALIGN-1-004-wordpress-parity",
                "ALIGN-1-005-seed-block-patterns",
                "ALIGN-1-006-taxonomy",
                "ALIGN-1-007-brazen-domain");
    }

    @Test
    void demoSeedDataIsPresent() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        Map<String, Object> tenant = jdbc.queryForMap(
                "SELECT slug, name FROM tenants WHERE id = ?",
                "a0000000-0000-4000-8000-000000000001");

        assertThat(tenant).containsEntry("slug", "demo");
        assertThat(tenant).containsEntry("name", "Demo Client");
    }
}

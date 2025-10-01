package com.restaurant.demo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests that provides common test configuration
 * All integration tests should extend this class to ensure proper test database setup
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public abstract class BaseIntegrationTest {

    // This base class ensures that:
    // 1. Tests use the H2 in-memory database instead of MySQL
    // 2. Each test runs in a transaction that is rolled back after completion
    // 3. Tests are isolated from each other and don't affect the production database
}

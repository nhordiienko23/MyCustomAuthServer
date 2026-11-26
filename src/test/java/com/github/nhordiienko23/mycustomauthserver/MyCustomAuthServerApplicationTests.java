package com.github.nhordiienko23.mycustomauthserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@Transactional
@DisplayName("Application integration")
class MyCustomAuthServerApplicationTests extends AbstractPostgreSqlIntegrationTest {

    @Test
    @DisplayName("Should start the application context with PostgreSQL")
    void contextLoads() {
    }
}

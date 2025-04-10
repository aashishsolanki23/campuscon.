package com.campuscon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.campuscon.config.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
class CampusConApplicationTests {

    @Test
    void contextLoads() {
    }
}

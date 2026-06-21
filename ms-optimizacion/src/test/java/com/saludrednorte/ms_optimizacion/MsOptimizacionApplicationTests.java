package com.saludrednorte.ms_optimizacion;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestMessagingConfig.class)
class MsOptimizacionApplicationTests {

    @Test
    void contextLoads() {
    }

}

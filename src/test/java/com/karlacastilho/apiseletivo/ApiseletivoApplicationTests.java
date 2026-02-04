package com.karlacastilho.apiseletivo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("ContextLoads desabilitado: app depende de infraestrutura externa (Postgres/MinIO).")
@SpringBootTest
class ApiseletivoApplicationTests {
    @Test void contextLoads() {}
}
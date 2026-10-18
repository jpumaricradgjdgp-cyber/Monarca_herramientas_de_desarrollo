package com.Monarca.Backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:monarca_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS monarca",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=clave-exclusiva-de-pruebas-locales-no-usar-en-produccion",
    "spring.mail.host=",
    "monarca.mail.from=",
    "monarca.password-reset-url="
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}

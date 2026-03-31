package cz.cyberrange.platform.training.rest.integration;

import cz.cyberrange.platform.training.rest.integration.config.RestConfigTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RestConfigTest.class)
public class IntegrationTestApplication {}

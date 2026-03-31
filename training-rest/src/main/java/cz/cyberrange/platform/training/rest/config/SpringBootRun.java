package cz.cyberrange.platform.training.rest.config;

import cz.cyberrange.platform.training.service.config.FacadeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/** The type Spring boot run. */
@SpringBootApplication(scanBasePackages = "cz.cyberrange.platform.training.rest")
@EnableSpringDataWebSupport
@Import({WebConfigRestTraining.class, FacadeConfiguration.class})
public class SpringBootRun extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(SpringBootRun.class);
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(SpringBootRun.class, args);
  }
}

package cz.cyberrange.platform.training.rest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/** Allows intentionally public, summary-only endpoints. */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PublicEndpointSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.requestMatchers()
        .antMatchers("/public/**")
        .and()
        .authorizeRequests()
        .anyRequest()
        .permitAll()
        .and()
        .csrf()
        .disable();
  }
}

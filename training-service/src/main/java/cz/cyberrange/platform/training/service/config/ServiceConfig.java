package cz.cyberrange.platform.training.service.config;

import cz.cyberrange.platform.commons.security.config.ResourceServerSecurityConfig;
import cz.cyberrange.platform.commons.startup.config.MicroserviceRegistrationConfiguration;
import cz.cyberrange.platform.training.elasticsearch.service.config.ElasticsearchServiceConfig;
import cz.cyberrange.platform.training.persistence.config.PersistenceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The type Service config.
 */
@Configuration
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
@Import({MicroserviceRegistrationConfiguration.class,
        ElasticsearchServiceConfig.class,
        PersistenceConfig.class,
        ResourceServerSecurityConfig.class,
        WebClientConfig.class
})
@ComponentScan(basePackages = {"cz.cyberrange.platform.training.service.services", "cz.cyberrange.platform.training.service.config", "cz.cyberrange.platform.training.service.startup"})
public class ServiceConfig {
    
    private Environment env;

    /**
     * Instantiates a new Service config.
     */
    @Autowired
    public ServiceConfig(Environment env) {
        this.env = env;
    }

    /**
     * This configuration is necessary for sharing SecurityContext between worker threads (to pass SecurityContext to the @Async methods.)
     *
     * @return method invoking factory bean
     */
    @Bean
    public MethodInvokingFactoryBean methodInvokingFactoryBean() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
        methodInvokingFactoryBean.setTargetMethod("setStrategyName");
        methodInvokingFactoryBean.setArguments((Object[]) new String[]{SecurityContextHolder.MODE_INHERITABLETHREADLOCAL});
        return methodInvokingFactoryBean;
    }

}

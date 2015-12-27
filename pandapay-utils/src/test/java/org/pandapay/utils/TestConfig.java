package org.pandapay.utils;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Created by LANLI on 2015/12/18.
 */
@Profile("unit-test")
@Configuration
public class TestConfig implements EnvironmentAware {

    private Environment env;

    @Bean
    RelaxedPropertyResolver propertyResolver()
    {
        return new RelaxedPropertyResolver(env, "idManager.");
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.env = environment;
    }
}

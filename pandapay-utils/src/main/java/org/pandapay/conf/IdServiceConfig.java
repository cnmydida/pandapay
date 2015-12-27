package org.pandapay.conf;

import org.pandapay.utils.IdGeneratorFactory;
import org.pandapay.utils.impl.IncGenerator;
import org.pandapay.utils.impl.JdbcGenerator;
import org.pandapay.utils.impl.UuidGenerator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by LANLI on 2015/12/27.
 */
@Configuration
@ComponentScan(basePackages = {"org.pandapay.utils"})
public class IdServiceConfig {

    @Bean
    public FactoryBean serviceLocatorFactoryBean() {
        ServiceLocatorFactoryBean factoryBean = new ServiceLocatorFactoryBean();
        factoryBean.setServiceLocatorInterface(IdGeneratorFactory.class);
        return factoryBean;
    }

    @Bean(name = "UUID")
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public UuidGenerator uuidGenerator() {
        return new UuidGenerator();
    }

    @Bean(name = "INC")
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IncGenerator incGenerator() {
        return new IncGenerator();
    }

    @Bean(name = "JDBC")
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public JdbcGenerator jdbcGenerator() {
        return new JdbcGenerator();
    }

}

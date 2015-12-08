package org.pandapay.conf;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Created by Jerry Wang on 2015/12/6.
 */
@Configuration
@EnableTransactionManagement
public class DataBaseConfiguration implements EnvironmentAware {

    private RelaxedPropertyResolver propertyResolver;

    private static Logger log = LoggerFactory.getLogger(DataBaseConfiguration.class);

    @Override
    public void setEnvironment(Environment env) {
        this.propertyResolver = new RelaxedPropertyResolver(env, "jdbc.");
    }

    @Bean(destroyMethod = "close")
    @Primary
    public DataSource dataSource() {

        log.debug("Configuring HikariDataSource.");

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(propertyResolver.getProperty("url"));
        ds.setUsername(propertyResolver.getProperty("username"));
        ds.setPassword(propertyResolver.getProperty("password"));

        return ds;
    }

}

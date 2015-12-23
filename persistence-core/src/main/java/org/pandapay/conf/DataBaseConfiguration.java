package org.pandapay.conf;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AbstractTransactionManagementConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * Created by Jerry Wang on 2015/12/6.
 */
@Configuration
@EnableTransactionManagement
public class DataBaseConfiguration implements EnvironmentAware {

    private RelaxedPropertyResolver propertyResolver;
    private RelaxedPropertyResolver mybatisResolver;
    private static Logger log = LoggerFactory.getLogger(DataBaseConfiguration.class);
    @Autowired
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    @Override
    public void setEnvironment(Environment env) {
        this.propertyResolver = new RelaxedPropertyResolver(env, "jdbc.");
        this.mybatisResolver = new RelaxedPropertyResolver(env, "mybatis.");
    }

    @Bean(destroyMethod = "close")
    @Primary
    public DataSource dataSource() {

        log.debug("Configuring HikariDataSource.");

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(propertyResolver.getProperty("url"));
        ds.setUsername(propertyResolver.getProperty("username"));
        ds.setPassword(propertyResolver.getProperty("password"));
        ds.setDriverClassName(propertyResolver.getProperty("driverClassName"));
        ds.setAutoCommit(false);
        return ds;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource());
        if (StringUtils.hasText(this.mybatisResolver.getProperty("config"))) {
            factory.setConfigLocation(
                    this.resourceLoader.getResource(this.mybatisResolver.getProperty("config")));
        }
        return factory.getObject();
    }


    @Bean(destroyMethod = "getExecutorType")
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory,
                ExecutorType.valueOf(mybatisResolver.getProperty("executorType")));
    }

    @Bean
    @ConditionalOnMissingBean({PlatformTransactionManager.class})
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @ConditionalOnMissingBean({AbstractTransactionManagementConfiguration.class})
    @Configuration
    @EnableTransactionManagement
    protected static class TransactionManagementConfiguration {
        protected TransactionManagementConfiguration() {
        }
    }

}

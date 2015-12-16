package org.pandapay;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.pandapay.accounting.domain.City;
import org.pandapay.accounting.mapper.CityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;

/**
 * Created by LANLI on 2015/12/6.
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@MapperScan("org.pandapay.accounting.mapper")
public class AccountingApplication implements CommandLineRunner {

    @Autowired
    private CityMapper cityMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public static void main(String[] args) {
        SpringApplication.run(AccountingApplication.class, args);
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        City city = this.cityMapper.selectCityById(1);
        city.setName("ShenZhen 5");
        this.cityMapper.updateCity(city);
        System.out.println(transactionManager);

        SqlSession sqlSession = SqlSessionUtils.getSqlSession(sqlSessionTemplate.getSqlSessionFactory(), sqlSessionTemplate.getExecutorType(), sqlSessionTemplate.getPersistenceExceptionTranslator());
        PreparedStatement statement = sqlSession.getConnection().prepareStatement("update city set state = 'bc' where id = 1");
        statement.executeUpdate();


        //throw new UnsupportedOperationException("trying transaction");
    }

}

package org.pandapay;

import org.mybatis.spring.annotation.MapperScan;
import org.pandapay.accounting.mapper.CityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Created by LANLI on 2015/12/6.
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@MapperScan("org.pandapay.accounting.mapper")
public class AccountingApplication implements CommandLineRunner {

    @Autowired
    private CityMapper cityMapper;

    public static void main(String[] args) {
        SpringApplication.run(AccountingApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(this.cityMapper.selectCityById(1));
    }

}

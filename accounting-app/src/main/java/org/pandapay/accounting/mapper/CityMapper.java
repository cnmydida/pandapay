package org.pandapay.accounting.mapper;

import org.mybatis.spring.SqlSessionTemplate;
import org.pandapay.accounting.domain.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by LANLI on 2015/12/6.
 */
@Component
public class CityMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public City selectCityById(long id) {
        return this.sqlSessionTemplate.selectOne("selectCityById", id);
    }

}
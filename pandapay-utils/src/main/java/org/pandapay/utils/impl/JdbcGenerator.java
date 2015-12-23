package org.pandapay.utils.impl;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.pandapay.utils.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by LANLI on 2015/12/22.
 */
public class JdbcGenerator implements IdGenerator<Long> {


    private String idType;
    private Map<String, String> params;

    private static final String BATCH_KEY = "batch";
    private static final int DEFAULT_BATCH_SIZE = 1000;

    private static final String UPDATE_AND_GET_KEY_SQL = "update ID_STORE set CURRENT_ID = CURRENT_ID + ? where ID_TYPE = ? returning CURRENT_ID";

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Override
    @Transactional
    public Long nextID() {
        try {
            return updateAndGetNextId();
        } catch (SQLException e) {
            throw new UnsupportedOperationException("can not get next id.", e);
        }
    }

    @Override
    public void setIdType(String idType) {
        this.idType = idType;
    }

    @Override
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    private int getBatchSize()
    {
        int batchSize =DEFAULT_BATCH_SIZE;

        if (params != null)
        {
            String value = params.get(BATCH_KEY);
            if (value != null)
            {
                try {
                    batchSize = Integer.valueOf(value);
                }
                catch (NumberFormatException ex)
                {
                    //ignore
                }
            }
        }
        return batchSize;
    }
    private Long updateAndGetNextId() throws SQLException {
        SqlSession sqlSession = SqlSessionUtils.getSqlSession(sqlSessionTemplate.getSqlSessionFactory(), sqlSessionTemplate.getExecutorType(), sqlSessionTemplate.getPersistenceExceptionTranslator());
        PreparedStatement statement = sqlSession.getConnection().prepareStatement(UPDATE_AND_GET_KEY_SQL);
        statement.setInt(1, getBatchSize());
        statement.setString(2, idType);

        boolean result = statement.execute();

        if (result)
        {
            ResultSet resultSet = statement.getResultSet();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        }

        throw new UnsupportedOperationException("can not get next id from db");
    }
}

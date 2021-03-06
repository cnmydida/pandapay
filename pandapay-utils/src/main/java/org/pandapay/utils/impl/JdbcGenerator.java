package org.pandapay.utils.impl;


import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.pandapay.utils.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;


import java.sql.*;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by LANLI on 2015/12/22.
 */
public class JdbcGenerator implements IdGenerator<Long>, IdGeneratorConfig {


    private String idType;
    private int batchSize;
    private Map<String, String> params;

    Semaphore semaphore = new Semaphore(1);
    ExecutorService service = Executors.newFixedThreadPool(1);
    ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();

    private static final String BATCH_KEY = "batch";
    private static final int DEFAULT_BATCH_SIZE = 1000;

    private static final String POSTGRE_UPDATE_AND_GET_KEY_SQL = "update ID_STORE set CURRENT_ID = CURRENT_ID + ? where ID_TYPE = ? returning CURRENT_ID";
    private static final String ORACLE_UPDATE_AND_GET_KEY_SQL = "update ID_STORE set CURRENT_ID = CURRENT_ID + ? where ID_TYPE = ? returning CURRENT_ID into ?";
    private static final String GENERAL_UPDATE_SQL = "update ID_STORE set CURRENT_ID = CURRENT_ID + ? where ID_TYPE = ?";
    private static final String GENERAL_SELECT_SQL = "select CURRENT_ID from ID_STORE where ID_TYPE = ?";

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    @Override
    public Long nextID() {
        if (this.batchSize == 1) {
            return updateAndGetNextId();
        } else {
            Long result = queue.poll();
            while (result == null) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    throw new IdGenerateException("interrupted", e);
                }
                acquireNextIds();
                result = queue.poll();

            }
            return result;
        }
    }

    @Override
    public void setParams(Map<String, String> params) {
        if (params == null) return;

        this.params = params;

        this.batchSize = DEFAULT_BATCH_SIZE;

        String value = params.get(BATCH_KEY);
        if (value != null) {
            try {
                batchSize = Integer.valueOf(value);
            } catch (NumberFormatException ex) {
                ;
            }
        }
    }

    @Override
    public void setIdType(String idType) {

        this.idType = idType;

    }


    private void acquireNextIds() {
        Callable<Long> task = new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                try {
                    Long result = queue.peek();
                    if (result == null) {
                        Long currentId = updateAndGetNextId();
                        Range<Long> longRange = Range.closedOpen(currentId - batchSize, currentId);
                        queue.addAll(ContiguousSet.create(longRange, DiscreteDomain.longs()));

                        return currentId;
                    }
                    return result;
                } finally {

                    semaphore.release();
                }
            }


        };
        final Future<Long> taskResult = service.submit(task);
        try {
            Long result = taskResult.get();
            if (result == null) {
                throw new IdGenerateException("can not get next id.");
            }
        } catch (ExecutionException e) {
            throw new IdGenerateException("can not get next id", e);
        } catch (InterruptedException e) {
            throw new IdGenerateException("can not get next id", e);
        }
    }

    private String getDatabaseId() {
        String databaseId = sqlSessionTemplate.getConfiguration() == null ? "default" : sqlSessionTemplate.getConfiguration().getDatabaseId();
        return databaseId == null ? "default" : databaseId;
    }

    public Long updateAndGetNextId() {
        //System.out.println(this.getDatabaseId());//PostgreSQL
        Long result = -1L;
        SqlSession sqlSession = SqlSessionUtils.getSqlSession(sqlSessionTemplate.getSqlSessionFactory(), sqlSessionTemplate.getExecutorType(), sqlSessionTemplate.getPersistenceExceptionTranslator());
        try {
            switch (this.getDatabaseId()) {
                case "PostgreSQL":
                    result = postgreSQLNextId(sqlSession);
                    break;
                case "Oracle":
                    result = oracleSQLNextId(sqlSession);
                    break;
                default:
                    result = generalSQLNextId(sqlSession);
                    break;


            }
            sqlSession.commit(true);
            return result;
        } catch (Exception e) {
            sqlSession.rollback(true);
            throw new IdGenerateException("db exception occured.", e);
        } finally {
            sqlSession.close();
        }


    }

    private Long postgreSQLNextId(SqlSession sqlSession) throws SQLException {
        PreparedStatement statement = sqlSession.getConnection().prepareStatement(POSTGRE_UPDATE_AND_GET_KEY_SQL);
        statement.setInt(1, this.batchSize);
        statement.setString(2, this.idType);

        boolean result = statement.execute();

        if (result) {
            ResultSet resultSet = statement.getResultSet();
            resultSet.next();
            Long nextId = resultSet.getLong(1);
            return nextId;
        }

        throw new IdGenerateException("can not get id from database.");
    }

    private Long oracleSQLNextId(SqlSession sqlSession) throws SQLException {
        CallableStatement statement = sqlSession.getConnection().prepareCall("{call " + ORACLE_UPDATE_AND_GET_KEY_SQL + "}");
        statement.setInt(1, this.batchSize);
        statement.setString(2, this.idType);
        statement.registerOutParameter(3, Types.NUMERIC);

        try {
            statement.execute();
            Long nextId = statement.getLong(3);
            return nextId;
        } catch (Exception e) {
            throw new IdGenerateException("can not get id from database.", e);
        }
    }

    private Long generalSQLNextId(SqlSession sqlSession) throws SQLException {
        PreparedStatement statement = sqlSession.getConnection().prepareStatement(GENERAL_UPDATE_SQL);
        statement.setInt(1, this.batchSize);
        statement.setString(2, this.idType);

        int noOfRow = statement.executeUpdate();

        if (noOfRow == 1) {
            statement = sqlSession.getConnection().prepareStatement(GENERAL_SELECT_SQL);
            statement.setString(1, this.idType);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long nextId = resultSet.getLong(1);
                return nextId;
            }
        }

        throw new IdGenerateException("can not get id from database.");
    }


    @Override
    public String toString() {
        return "ID Type: " + idType + "-- using DB " + this.getDatabaseId();
    }
}

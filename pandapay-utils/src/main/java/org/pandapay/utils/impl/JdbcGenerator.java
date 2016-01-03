package org.pandapay.utils.impl;


import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.pandapay.utils.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private static final String UPDATE_AND_GET_KEY_SQL = "update ID_STORE set CURRENT_ID = CURRENT_ID + ? where ID_TYPE = ? returning CURRENT_ID";

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
        return sqlSessionTemplate.getConfiguration().getDatabaseId();
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
                default:
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
        PreparedStatement statement = sqlSession.getConnection().prepareStatement(UPDATE_AND_GET_KEY_SQL);
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

    @Override
    public String toString() {
        return "ID Type: " + idType + "-- using DB " + this.getDatabaseId();
    }
}

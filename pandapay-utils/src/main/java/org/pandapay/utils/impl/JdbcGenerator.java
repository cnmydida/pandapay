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
        }
        else
        {
            Long result = queue.poll();
            while (result == null)
            {
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
        if (value != null)
        {
            try {
                batchSize = Integer.valueOf(value);
            }
            catch (NumberFormatException ex)
            {
                ;
            }
        }
    }

    @Override
    public void setIdType(String idType) {

        this.idType = idType;

    }


    private void acquireNextIds()
    {
        CountDownLatch doneSignal = new CountDownLatch(1);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    if (queue.peek() == null) {
                        Long currentId = updateAndGetNextId();
                        Range<Long> longRange = Range.closedOpen(currentId - batchSize, currentId);
                        queue.addAll(ContiguousSet.create(longRange, DiscreteDomain.longs()));
                    }
                }
                finally {
                    doneSignal.countDown();
                    semaphore.release();
                }

            }
        };
        service.execute(run);
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            ;
        }
    }
    private String getDatabaseId()
    {
        return sqlSessionTemplate.getConfiguration().getDatabaseId();
    }



    public Long updateAndGetNextId() {
        System.out.println(this.getDatabaseId());

        SqlSession sqlSession = SqlSessionUtils.getSqlSession(sqlSessionTemplate.getSqlSessionFactory(), sqlSessionTemplate.getExecutorType(), sqlSessionTemplate.getPersistenceExceptionTranslator());
        //System.out.println("Database Id: " + sqlSession.getConfiguration().getDatabaseId());
        try {
            PreparedStatement statement = sqlSession.getConnection().prepareStatement(UPDATE_AND_GET_KEY_SQL);
            statement.setInt(1, this.batchSize);
            statement.setString(2, this.idType);

            boolean result = statement.execute();

            if (result) {
                ResultSet resultSet = statement.getResultSet();
                resultSet.next();
                Long nextId = resultSet.getLong(1);
                sqlSession.commit(true);
                return nextId;
            }
        }
        catch (Exception e)
        {
            sqlSession.rollback(true);
            throw new IdGenerateException("db exception occured.", e);
        }
        finally {
            sqlSession.close();
        }

        throw new IdGenerateException("can not get id from database.");
    }

    @Override
    public String toString() {
        return "ID Type: " + idType + "-- using DB " + this.getDatabaseId();
    }
}

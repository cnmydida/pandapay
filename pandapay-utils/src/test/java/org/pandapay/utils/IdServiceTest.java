package org.pandapay.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pandapay.conf.DataBaseConfiguration;
import org.pandapay.conf.IdServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertNotNull;

/**
 * Created by LANLI on 2015/12/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("unit-test")
@SpringApplicationConfiguration(classes = {IdServiceConfig.class, TestConfig.class, DataBaseConfiguration.class})
public class IdServiceTest {

    @Autowired
    RelaxedPropertyResolver propertyResolver;

    @Autowired
    IdService idService;

    @Test
    public void testPropertyResolver()
    {
        Map<String, Object> subProperties = propertyResolver.getSubProperties("");
        System.out.println(subProperties);
    }

    @Test
    public void testUuidGenerator()
    {
        //IdGenerator<String> idGenerator = idManager.getIdGenerator("account_no");
        //assertNotNull("not null", idGenerator);
        System.out.println(idService.<String>nextId("account_no"));
    }

    @Test
    public void testJdbcGenerator()
    {
//        IdGenerator<Long> idGenerator = idManager.getIdGenerator("entry_id");
//        assertNotNull("not null", idGenerator);
//        System.out.println(idGenerator.nextID());
//        ExecutorService service = Executors.newFixedThreadPool(100);
//        for (int i = 0; i < 100; i++) {
//            int index = i + 1;
//            Runnable run = new Runnable() {
//                @Override
//                public void run() {
//                    System.out.println("thread start" + index);
//                        for (int j = 0; j < 10000; j++) {
//                            Long id = idService.<Long>nextId("entry_id");
//                            System.out.print(id + ", ");
//                        }
//                    System.out.println("thread end" + index);
//                }
//            };
//            service.execute(run);
//        }

        Long id = idService.<Long>nextId("entry_id");
        System.out.println(id + ", ");

        Long id2 = idService.<Long>nextId("entry_id");
        System.out.println(id2 + ", ");
    }

    @Test
    public void testMultiThreadJdbcGenerator() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            int index = i + 1;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("thread start" + index);
                        for (int j = 0; j < 10000; j++) {
                            Long id = idService.<Long>nextId("entry_id");
                            System.out.print(id + ", ");
                        }
                        System.out.println("thread end" + index);
                    }
                    finally {
                        latch.countDown();
                    }

                }
            };
            service.execute(run);
        }
        latch.await();

    }
}

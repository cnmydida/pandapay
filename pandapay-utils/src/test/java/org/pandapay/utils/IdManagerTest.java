package org.pandapay.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pandapay.conf.DataBaseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Created by LANLI on 2015/12/21.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("unit-test")
@SpringApplicationConfiguration(classes = {TestConfig.class, DataBaseConfiguration.class})
public class IdManagerTest {

    @Autowired
    RelaxedPropertyResolver propertyResolver;

    @Autowired
    IdManager idManager;

    @Test
    public void testPropertyResolver()
    {
        Map<String, Object> subProperties = propertyResolver.getSubProperties("");
        System.out.println(subProperties);
    }

    @Test
    public void testUuidGenerator()
    {
        IdGenerator<String> idGenerator = idManager.getIdGenerator("account_no");
        assertNotNull("not null", idGenerator);
        System.out.println(idGenerator.nextID());
    }

    @Test
    public void testJdbcGenerator()
    {
        IdGenerator<Long> idGenerator = idManager.getIdGenerator("entry_id");
        assertNotNull("not null", idGenerator);
        System.out.println(idGenerator.nextID());
    }


}

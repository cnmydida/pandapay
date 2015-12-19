package org.pandapay.utils;

import com.eaio.uuid.UUID;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by LANLI on 2015/12/19.
 */
public class UuidTest {

    @Test
    public void testUuid()
    {
        UUID uuid = new UUID();
        System.out.println(uuid.toString());
        assertTrue("larger than 0", uuid.toString().length() > 0);
    }

    @Test
    public void testJavaUuid()
    {
        java.util.UUID uuid = UuidUtils.getTimeUUID();
        System.out.println(uuid.toString());
        assertTrue("larger than 0", uuid.toString().length() > 0);
    }

    @Test
    public void testManyJavaUuid()
    {
        for (int i = 0; i < 100; i++)
        {
            java.util.UUID uuid = UuidUtils.getTimeUUID();
            System.out.println(uuid.toString());
        }
    }
}

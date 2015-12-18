package org.pandapay;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * Created by LANLI on 2015/12/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("unit-test")
@SpringApplicationConfiguration(classes = TestConfig.class)
public class EmbeddedDbTest {

    @Test
    public void testOutput()
    {
        assertTrue("test running", true);
    }
}

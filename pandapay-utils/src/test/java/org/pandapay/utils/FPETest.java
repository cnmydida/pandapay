package org.pandapay.utils;

import net._95point2.fpe.FPE;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * Created by LANLI on 2015/12/20.
 */
public class FPETest {

    @Test
    public void testFPE() throws Exception {
        final byte[] key = "Here is my secret key!".getBytes();
        final byte[] tweak = "tweak".getBytes();
        final long range = 100000000000000L;
        final BigInteger modulus = BigInteger.valueOf(range);

        BigInteger plain = BigInteger.valueOf(34538293233L);
        BigInteger enc = FPE.encrypt(modulus, plain, key, tweak);
        BigInteger dec = FPE.decrypt(modulus, enc, key, tweak);

        Assert.assertTrue( dec.compareTo(plain) == 0 );
    }
}

package transform.app.server.common.utils;

import org.junit.Test;

import java.math.BigInteger;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class RandomUtilsTest {

    @Test
    public void testUUIDfromCustomString() {
        String s = "0f14d0ab-9605-4a62-a9e4-5ed26688389b";
        String s2 = "1df7ca4ebd634327a44decc868c1d67b";
        UUID uuid = new UUID(
                new BigInteger(s2.substring(0, 16), 16).longValue(),
                new BigInteger(s2.substring(16), 16).longValue());
        assertEquals(uuid.toString(), RandomUtils.fromCustomString(s2).toString());
        assertEquals(UUID.fromString(s).toString(), s);
    }
}

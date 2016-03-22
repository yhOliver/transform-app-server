package transform.app.server.common.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapUtilsTest {
    @Test
    public void testLantitudeLongitudeDist() {
        assertEquals(MapUtils.LantitudeLongitudeDist(116.401394, 39.95676, 114.499574, 36.63014), 405858.3127090019, 0.0000000000001);
    }
}

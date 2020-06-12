package algorithm;

import org.junit.Test;

import static org.junit.Assert.*;

public class SerializerTest {

    @Test
    public void testMCDFCacheStoreAndLoad(){
        double[] p = {0.1,0.2,0.7};
        MCDFCache cache = new MCDFCache(p);
        Serializer.storeMCDFCache(cache);
        MCDFCache loadedCache = Serializer.loadMCDFCache(p);
        assertEquals(cache,loadedCache);
    }
}

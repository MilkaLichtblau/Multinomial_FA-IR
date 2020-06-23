package algorithm;

import algorithm.MCDFCache;
import algorithm.MTree;
import algorithm.Serializer;
import org.junit.Test;

import java.util.Arrays;

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

    @Test
    public void testMTreeStoreAndLoad(){
        double[] p = {1.0/3.0,1.0/3.0,1.0/3.0};
        int k = 15;
        double alpha = 0.1;
        MTree mTree = new MTree(k,p,alpha,false);
        MTree loadedMTree = Serializer.loadMTree(k,p,alpha,false);
        boolean kEquals = mTree.getK() == loadedMTree.getK();
        boolean alphaEquals = mTree.getAlpha() == loadedMTree.getAlpha();
        boolean pEquals = Arrays.equals(mTree.getP(), loadedMTree.getP());
        boolean treeEquals = mTree.getTree().equals(loadedMTree.getTree());
        assertTrue(kEquals && alphaEquals && pEquals && treeEquals);
    }

    @Test
    public void testIfLoadedMTreeContainsMCDFCache(){
        double[] p = {1.0/3.0,1.0/3.0,1.0/3.0};
        int k = 15;
        double alpha = 0.1;
        @SuppressWarnings("unused")
        MTree mTree = new MTree(k,p,alpha,false);
        MTree loadedMTree = Serializer.loadMTree(k,p,alpha,false);
        assertNull(loadedMTree.getMcdfCache());
    }
}

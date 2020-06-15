package algorithm;

import java.util.Arrays;
import java.util.List;

public class MCDFCacheThread extends Thread {
    private MCDFCache cache;
    private int from;
    private int to;

    public MCDFCacheThread(MCDFCache cache, int from, int to){
        this.cache = cache;
        this.from = from;
        this.to = to;
    }

    public void run() {
        double[] p = cache.getP();
        Integer[] props = new Integer[p.length];
        props[0] = from;
        for(int i=1; i<props.length; i++){
            props[i] = Math.toIntExact(Math.round(i * p[i]));
        }
    }

}

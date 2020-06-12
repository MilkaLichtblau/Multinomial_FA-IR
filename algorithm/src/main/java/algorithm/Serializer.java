package algorithm;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Serializer {

    public static void storeMTree(MTree mTree) {
        FileOutputStream fOutputStream;
        ObjectOutputStream OOutputStream;
        String filename = createFileNameFromMTree(mTree);
        try {
            fOutputStream = new FileOutputStream(filename);
            OOutputStream = new ObjectOutputStream(fOutputStream);
            OOutputStream.writeObject(mTree);

            OOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeMCDFCache(MCDFCache cache){
        FileOutputStream fOutputStream;
        ObjectOutputStream OOutputStream;
        String filename = createMCDFCacheFileNameFromObject(cache);
        try {
            fOutputStream = new FileOutputStream(filename);
            OOutputStream = new ObjectOutputStream(fOutputStream);
            OOutputStream.writeObject(cache);

            OOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MCDFCache loadMCDFCache(double[] p){
        String filename = Serializer.createMCDFCacheFileNameFromProportions(p);
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        MCDFCache cache;
        try {
            fileInputStream = new FileInputStream(filename);
            objectInputStream = new ObjectInputStream(fileInputStream);
            cache = (MCDFCache) objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception e) {
            return null;
        }
        return cache;
    }

    public static MTree loadMTree(int k, double[] p, double alpha, boolean isAdjusted){
        String filename = Serializer.createFileNameFromParameters(k,p,alpha,isAdjusted);
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        MTree mTree;
        try {
            fileInputStream = new FileInputStream(filename);
            objectInputStream = new ObjectInputStream(fileInputStream);
            mTree = (MTree) objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception e) {
            return null;
        }
        return mTree;
    }

    public static String createMCDFCacheFileNameFromObject(MCDFCache cache){
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mcdfcache");
        stringBuilder.append(File.separator);
        for (int i = 0; i<cache.getP().length; i++) {
            stringBuilder.append(cache.getP()[i]);
            if(i+1<cache.getP().length){
                stringBuilder.append('_');
            }
        }
        stringBuilder.append(".cache");
        return stringBuilder.toString();
    }

    public static String createMCDFCacheFileNameFromProportions(double[] p){
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mcdfcache");
        stringBuilder.append(File.separator);
        for (int i = 0; i<p.length; i++) {
            stringBuilder.append(p[i]);
            if(i+1<p.length){
                stringBuilder.append('_');
            }
        }
        stringBuilder.append(".cache");
        return stringBuilder.toString();
    }

    private static String createFileNameFromParameters(int k, double[] p, double alpha, boolean isAdjusted){
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mtree");
        stringBuilder.append(File.separator);
        stringBuilder.append(k);
        stringBuilder.append('_');
        for (double pi : p) {
            stringBuilder.append(pi);
            stringBuilder.append('_');
        }
        stringBuilder.append(alpha);
        stringBuilder.append('_');
        stringBuilder.append(isAdjusted);
        stringBuilder.append(".mtree");
        return stringBuilder.toString();
    }

    private static String createFileNameFromMTree(MTree mTree) {
        Path currentRelativePath = Paths.get("");
        int k = mTree.getK();
        double[] p = mTree.getP();
        double alpha = mTree.getAlpha();
        boolean isAdjusted = mTree.isAdjusted();
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mtree");
        stringBuilder.append(File.separator);
        stringBuilder.append(k);
        stringBuilder.append('_');
        for (double pi : p) {
            stringBuilder.append(pi);
            stringBuilder.append('_');
        }
        stringBuilder.append(alpha);
        stringBuilder.append('_');
        stringBuilder.append(isAdjusted);
        stringBuilder.append(".mtree");
        return stringBuilder.toString();
    }

}

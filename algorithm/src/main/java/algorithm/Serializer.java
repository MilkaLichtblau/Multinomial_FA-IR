package algorithm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Serializer {

    public static boolean checkIfMCDFFileExists(MCDFCache cache){
        String filename = createMCDFCacheFileNameFromObject(cache);
        File file = new File(filename);
        return file.exists();
    }

    public static boolean checkIfStorageDirectoriesExist(){
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        if(Files.notExists(Paths.get(stringBuilder.toString()))){
            Scanner in = new Scanner(System.in);
            System.out.print("Directory for MCDFCache and MTreeCache not found. Create it?(\"yes\") or stop here (\"no\")");
            String decision = in.nextLine();
            if(decision.contains("yes")){
                String storageDir = stringBuilder.toString();
                new File(storageDir).mkdir();
                new File(storageDir + File.separator + "mcdfcache").mkdir();
                new File(storageDir + File.separator + "mtree").mkdir();
                in.close();
                return true;
            }else{
                in.close();
                return false;
            }
        }
        if(Files.exists(Paths.get(stringBuilder.toString()))){
            String storageDir = stringBuilder.toString();
            if(Files.notExists(Paths.get(storageDir + File.separator + "mcdfcache")) ||
                    Files.notExists(Paths.get(storageDir + File.separator + "mtree"))){
                Scanner in = new Scanner(System.in);
                System.out.print("Directory for MCDFCache or MTreeCache not found. Create it?(\"yes\") or stop here (\"no\")");
                String decision = in.nextLine();
                if(decision.contains("yes")){
                    if(Files.notExists(Paths.get(storageDir + File.separator + "mcdfcache"))){
                        new File(storageDir + File.separator + "mcdfcache").mkdir();
                    }else{
                        new File(storageDir + File.separator + "mtree").mkdir();
                    }
                    in.close();
                    return true;
                }else{
                    in.close();
                    return false;
                }
            }else{
                int mcdfDirSize = new File(stringBuilder.toString() + File.separator + "mcdfcache").listFiles().length;
                int mtreeDirSize = new File(stringBuilder.toString() + File.separator + "mtree").listFiles().length;
                if(mcdfDirSize == 0 || mtreeDirSize == 0){
                    System.out.println("WARNING: You should download the .cache and .mtree Files from the FA-IR repository. This will boost the performance" +
                            "by several orders of magnitude.");
                }
                return true;
            }

        }
        return false;
    }

    public static boolean checkIfMCDFCacheShouldBeStored(MCDFCache cache){
        if(!checkIfMCDFFileExists(cache)){
            return true;
        }
        MCDFCache storedCache = loadMCDFCache(cache.getP());
        if(storedCache.getMaxK() > cache.getMaxK()){
            return false;
        }
        if(storedCache.getMaxK()< cache.getMaxK()){
            return true;
        }
        if(storedCache.getMaxK() == cache.getMaxK()){
            return storedCache.getSize() < cache.getSize();
        }
        return true;
    }

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

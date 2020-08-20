package algorithm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Serializer {

    public static final int MAX_FILE_SIZE_IN_MB = 50;

    public static boolean checkIfMCDFFileExists(MCDFCache cache){
        String filename = createMCDFCacheFileNameFromObject(cache);
        File file = new File(filename);
        return file.exists();
    }

    public static List<File> getListOfMCDFFileParts(String fileName){
        fileName = new File(fileName).getName();
        fileName = fileName.substring(0,fileName.length()-4);
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mcdfcache");
        stringBuilder.append(File.separator);
        File dir = new File(stringBuilder.toString());
        File[] directoryListing = dir.listFiles();
        ArrayList<File> listOfFileParts = new ArrayList<>();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(child.getName().contains(fileName)){
                    listOfFileParts.add(child);
                }
            }
        }
        return listOfFileParts;
    }

    public static List<File> getListOfMTreeFileParts(String fileName){
        fileName = new File(fileName).getName();
        fileName = fileName.substring(0,fileName.length()-4);
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mtree");
        stringBuilder.append(File.separator);
        File dir = new File(stringBuilder.toString());
        File[] directoryListing = dir.listFiles();
        ArrayList<File> listOfFileParts = new ArrayList<>();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if(child.getName().contains(fileName)){
                    listOfFileParts.add(child);
                }
            }
        }
        return listOfFileParts;
    }

    public static File mergeFile(List<File> fileParts, String folder) throws IOException {
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append(folder);
        stringBuilder.append(File.separator);
        String referenceFileName = fileParts.get(0).getName();
        if(Character.isDigit(referenceFileName.charAt(referenceFileName.length()-1))){
            stringBuilder.append(fileParts.get(0).getName(), 0, fileParts.get(0).getName().length()-4);
        }else{
            stringBuilder.append(referenceFileName);
        }
        File into = new File(stringBuilder.toString());
        try (FileOutputStream fos = new FileOutputStream(into);
             BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
            for (File f : fileParts) {
                Files.copy(f.toPath(), mergingStream);
            }
        }
        return into;
    }

    public static void splitFile(File f) throws IOException {
        int partCounter = 2;

        int sizeOfFiles = 1024 * 1024 * MAX_FILE_SIZE_IN_MB;// MB
        byte[] buffer = new byte[sizeOfFiles];

        String fileName = f.getName();

        //try-with-resources to ensure closing stream
        try (FileInputStream fis = new FileInputStream(f);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                String filePartName = String.format("%s.%03d", fileName, partCounter++);
                File newFile = new File(f.getParent(), filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                }
            }
        }
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

    public static void splitMTreeFilesInStorage(){
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mtree");
        stringBuilder.append(File.separator);
        File dir = new File(stringBuilder.toString());
        File[] directoryListing = dir.listFiles();
        for(File f : directoryListing){
            if((f.length() / (1024 * 1024) >MAX_FILE_SIZE_IN_MB) && Character.isLetter(f.getName().charAt(f.getName().length()-1))){
                String name = f.getName();
                String[] params  = name.split("_");
                int k = Integer.valueOf(params[0]);
                boolean isAdjusted = Boolean.valueOf(params[params.length-1].substring(0,params[params.length-1].length()-6));
                double alpha = Double.valueOf(params[params.length-2]);
                double[] p = new double[params.length-3];
                for(int i=0; i<params.length-3; i++){
                    p[i] = Double.valueOf(params[i+1]);
                }
                MTree tree = loadMTree(k,p,alpha,isAdjusted);
                splitStoreMTree(tree);
                f.delete();
            }
        }
    }


    public static void splitMCDFCacheFilesInStorage(){
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mcdfcache");
        stringBuilder.append(File.separator);
        File dir = new File(stringBuilder.toString());
        File[] directoryListing = dir.listFiles();
        for(File f : directoryListing){
            if((f.length() / (1024 * 1024) >MAX_FILE_SIZE_IN_MB) && Character.isLetter(f.getName().charAt(f.getName().length()-1))){
                System.out.println("Splitting "+f.getName());
                String name = f.getName();
                String[] ps  = name.split("_");
                double[] p = new double[ps.length];
                ps[ps.length-1] = ps[ps.length-1].substring(0,ps[ps.length-1].length()-6);
                for(int i = 0; i<ps.length; i++){
                    p[i] = Double.valueOf(ps[i]);
                }
                MCDFCache cache = loadMCDFCache(p);
                splitStoreMCDFCache(cache);
                f.delete();
            }
        }
    }

    public static void storeMTree(MTree mTree) {
        FileOutputStream fOutputStream;
        ObjectOutputStream OOutputStream;
        String filename = createMTreeFileNameFromObject(mTree);
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

    public static void splitStoreMCDFCache(MCDFCache cache){
        FileOutputStream fOutputStream;
        ObjectOutputStream OOutputStream;
        String filename = createMCDFCacheFileNameFromObject(cache);
        try {
            fOutputStream = new FileOutputStream(filename);
            OOutputStream = new ObjectOutputStream(fOutputStream);
            OOutputStream.writeObject(cache);
            File bigFile = new File (filename);
            splitFile(bigFile);
            bigFile.delete();
            OOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void splitStoreMTree(MTree tree){
        FileOutputStream fOutputStream;
        ObjectOutputStream OOutputStream;
        String filename = createMTreeFileNameFromObject(tree);
        try {
            fOutputStream = new FileOutputStream(filename);
            OOutputStream = new ObjectOutputStream(fOutputStream);
            OOutputStream.writeObject(tree);
            File bigFile = new File (filename);
            splitFile(bigFile);
            bigFile.delete();
            OOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MCDFCache loadMCDFCache(double[] p){
        String filename = createMCDFCacheFileNameFromProportions(p);
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        MCDFCache cache;
        //List<File> files = getListOfMCDFFileParts(filename);
        try {
            //File file = mergeFile(files);
            File file = new File(filename);
            fileInputStream = new FileInputStream(file);
            objectInputStream = new ObjectInputStream(fileInputStream);
            cache = (MCDFCache) objectInputStream.readObject();
            objectInputStream.close();
            //file.delete();
        } catch (Exception e) {
            return null;
        }
        return cache;
    }

    public static MTree loadMTree(int k, double[] p, double alpha, boolean isAdjusted){
        String filename = createFileNameFromParameters(k,p,alpha,isAdjusted);
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        try {
            File file = new File(filename);
            fileInputStream = new FileInputStream(file);
            objectInputStream = new ObjectInputStream(fileInputStream);
            MTree mTree = (MTree) objectInputStream.readObject();
            objectInputStream.close();
            return mTree;
        } catch (Exception e) {
            return null;
        }
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

    private static String createMTreeFileNameFromObject(MTree mTree) {
        Path currentRelativePath = Paths.get("");
        int k = mTree.getK();
        double[] p = mTree.getP();
        double alpha = mTree.getUnadjustedAlpha();
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

    public static void mergeMCDFCacheFilesInStorage(){
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mcdfcache");
        stringBuilder.append(File.separator);
        File dir = new File(stringBuilder.toString());
        File[] directoryListing = dir.listFiles();
        HashMap<String,Boolean> alreadyMerged = new HashMap<>();
        for(File f : directoryListing) {
            if(!Character.isDigit(f.getName().charAt(f.getName().length()-1))){
                continue;
            }
            if(alreadyMerged.get(f.getName().substring(0,f.getName().length()-4)) != null){
                continue;
            }
            String filename = f.getName();
            System.out.println("Merging "+f.getName());
            List<File> files = getListOfMCDFFileParts(filename);
            try {
                mergeFile(files, "mcdfcache");
                //merged.createNewFile();
                for(File fPart : files){
                    if(Character.isDigit(fPart.getName().charAt(fPart.getName().length()-1))){
                        alreadyMerged.put(fPart.getName().substring(0,fPart.getName().length()-4),true);
                        fPart.delete();
                    }
                }

            } catch (Exception e) {
            }
        }
    }

    public static void mergeMTreeFilesInStorage(){
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mtree");
        stringBuilder.append(File.separator);
        File dir = new File(stringBuilder.toString());
        File[] directoryListing = dir.listFiles();
        HashMap<String,Boolean> alreadyMerged = new HashMap<>();
        for(File f : directoryListing) {
            if(!Character.isDigit(f.getName().charAt(f.getName().length()-1))){
                continue;
            }
            if(alreadyMerged.get(f.getName().substring(0,f.getName().length()-4)) != null){
                continue;
            }
            String filename = f.getName();
            System.out.println("Merging "+f.getName());
            List<File> files = getListOfMTreeFileParts(filename);
            try {
                mergeFile(files, "mtree");
                for(File fPart : files){
                    if(Character.isDigit(fPart.getName().charAt(fPart.getName().length()-1))){
                        alreadyMerged.put(fPart.getName().substring(0,fPart.getName().length()-4),true);
                        fPart.delete();
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}

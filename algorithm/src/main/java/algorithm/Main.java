package algorithm;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import algorithm.MTree.FairRankingStrategy;
import com.fairsearch.fair.lib.MTableGenerator;
import com.fairsearch.fair.lib.RecursiveNumericFailProbabilityCalculator;
import org.apache.commons.math3.distribution.BinomialDistribution;
import umontreal.ssj.probdistmulti.MultinomialDist;

public class Main {
    private static ArrayList<String> columnHeaders;
    private static List<Candidate> unfairRanking;
    private static List<Candidate> fairRanking;

    public static void prepareDataExperiments(String filename, String separator, boolean hasHeaders) throws IOException {
        unfairRanking = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        List<Double[]> lines = new ArrayList<Double[]>();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] cells = line.split(separator);
            if (hasHeaders) {
                //first line are column headers
                columnHeaders = new ArrayList<>(Arrays.asList(cells));
                hasHeaders = false;
                continue;
            }
            int group = Integer.parseInt(cells[columnHeaders.indexOf("group")]);
            Double score = Double.parseDouble(cells[columnHeaders.indexOf("score")]);
            Candidate candidate = new Candidate(score, group);
            unfairRanking.add(candidate);
        }
        assert columnHeaders.size() == lines.size();
        reader.close();
    }

    private static void parseParametersForMultinomialExperiment(String[] args) {
        int kMax = Integer.parseInt(args[1]);
        ArrayList<Double> pList = new ArrayList<>();
        for (int i = 2; i < args.length - 1; i++) {
            if (args[i].charAt(0) == 'p') {
                Double d = null;
                String decimal = args[i].substring(1);
                if (decimal.contains("/")) {
                    String[] numbers = decimal.split("/");
                    if (numbers.length == 2) {
                        BigDecimal d1 = BigDecimal.valueOf(Double.parseDouble(numbers[0]));
                        BigDecimal d2 = BigDecimal.valueOf(Double.parseDouble(numbers[1]));
                        BigDecimal response = d1.divide(d2, MathContext.DECIMAL128);
                        d = response.doubleValue();
                    }
                } else {
                    d = Double.parseDouble(decimal);
                }
                pList.add(d);
            } else {
                break;
            }
        }
        if (pList.size() < 2) {
            throw new IllegalArgumentException("state at least 2 minimum proportion values in the style: " +
                    "p0.2 p0.2 .... . You may write p1.0/3.0 ..");
        }
        double[] p = new double[pList.size()];
        for (int i = 0; i < p.length; i++) {
            p[i] = pList.get(i);
        }
        if (args[0].equals("failprob")) {
            double alpha = Double.parseDouble(args[args.length - 2]);
            String fileName = args[args.length - 1];
            runMultinomialFailProbabilityExperiment(kMax, p, alpha, fileName);
        }
        if (args[0].equals("adjust")) {
            double alpha = Double.parseDouble(args[args.length - 1]);
            MTree ajustedTree = new MTree(kMax, p, alpha, true);
        }
    }

    public static void appendStrToFile(String fileName, String str) {
        try {
            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(fileName, true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runMultinomialFailProbabilityExperiment(int kMax, double[] p, double alpha, String fileName) {
        String head = "k,failProbability" + '\n';
        Main.appendStrToFile(fileName, head);
        for (int k = 5; k <= kMax; k += 5) {
            if (k >= 500) {
                k += 50;
            }
            MTree tree = new MTree(k, p, alpha, false);
            double failProb = tree.getFailprob();
            Main.appendStrToFile(fileName, "" + k + "," + failProb + '\n');
            System.out.println("finished writing failProbFor k = " + k);
        }
    }

    public static void runBinomialFailProbabilityExperiment(int kMax, double p, double alpha, String fileName) {
        String head = "k,failProbability" + '\n';
        Main.appendStrToFile(fileName, head);
        for (int k = 5; k <= kMax; k += 5) {
            if (k >= 500) {
                k += 50;
            }
            RecursiveNumericFailProbabilityCalculator calculator = new RecursiveNumericFailProbabilityCalculator(k,p,alpha);
            double failProb = calculator.calculateFailProbability(new MTableGenerator(k,p,alpha,false).getMTable());
            Main.appendStrToFile(fileName, "" + k + "," + failProb + '\n');
            System.out.println("finished writing failProbFor k = " + k);
        }
    }

    public static void writeRankingsToCSV(String resultFilename) {
        //write headers
        String fairResultFilename = resultFilename + "_fair.csv";
        Main.appendStrToFile(fairResultFilename, "uuid, score, group\n");
        for (Candidate candidate : fairRanking) {
            Main.appendStrToFile(fairResultFilename, candidate.toString());
        }

        String unfairResultFilename = resultFilename + "_unfair.csv";
        Main.appendStrToFile(unfairResultFilename, "uuid, score, group\n");
        for (Candidate candidate : unfairRanking) {
            Main.appendStrToFile(unfairResultFilename, candidate.toString());
        }
    }

    private static void parseParametersForBinomialExperiment(String[] args) {
        String fileName = args[args.length-1];
        int k = Integer.parseInt(args[1]);
        double p = Double.parseDouble(args[2]);
        double alpha = Double.parseDouble(args[3]);
        if (args[0].equals("failprob-binomial")) {
            runBinomialFailProbabilityExperiment(k,p,alpha,fileName);
        }
    }

    /**
     * ***FailProb Experiment***
     * args structure: kMax p1 p2 ... pn alpha PATH/FileName
     * Creates unadjusted mTrees with parameters k=5 to kMax p1..pn alpha and stores their failprobability in the specified file in csv format.
     * Example for failProb experiment: java -jar algorithm.jar failprob 150 p0.3 p0.2 p0.5 0.05 PATH/TO/EXPERIMENT/FILE/OUTPUT/failProbExperiment1.csv
     * <p>
     * ***Adjust***
     * args structure k p1 ... pn alpha
     * Creates the adjusted mTree with approx. failprobability of alpha
     * Example for adjust experiment: java -jar algorithm.jar adjust 100 p1.0/3.0 p1.0/3.0 p1.0/3.0 0.1
     * <p>
     * ***Data Experiment***
     * args structure data path/to/input/file k p1...pn alpha path/to/output/file (without file extension)
     * Example for data experiment: java -jar MultinomialFair.jar data ../experiments/dataExperiments/data/COMPAS/compas_race_java.csv 500 0.5,0.5 0.1 ../experiments/dataExperiments/results/COMPAS/compas_race
     */

    public static void main(String[] args) throws FileNotFoundException {
        if (Serializer.checkIfStorageDirectoriesExist()) {
            try {
                if (args[0].equals("failprob-multinomial")) {
                    parseParametersForMultinomialExperiment(args);
                }
                if (args[0].equals("failprob-binomial")) {
                    parseParametersForBinomialExperiment(args);
                }
                if (args[0].equals("adjust")) {
                    parseParametersForMultinomialExperiment(args);
                    System.out.println("adjusted MTree stored in ../storage/mtree/..");
                }
                if (args[0].equals("runtime-construct")) {
                    double[] pSym = {1.0/3.0, 1.0/3.0, 1.0/3.0};
                    double[] pASym = {0.2, 0.3, 0.5};
                    double p = 0.5;
                    int kMax = 500;
                    double alpha = 0.1;
                    String fileName = args[args.length-1];
                    String head = "k, symmetric MTree, asymmetric MTree, MTable" +'\n';
                    Main.appendStrToFile(fileName, head);

                    for (int k = 5; k <= kMax; k += 5) {
                        //runtime symmetric
                        long start = System.nanoTime();
                        MTree symmetricTree = new MTree(k, pSym, alpha, false, false, false);
                        long end = System.nanoTime();
                        double timeSymmetricTree = (end - start) / 1000000000.0;

                        //runtime asymmetric
                        start = System.nanoTime();
                        MTree asymmetricTree = new MTree(k, pASym, alpha, false, false, false);
                        end = System.nanoTime();
                        double timeAsymmetricTree = (end - start) / 1000000000.0;

                        //runtime mtable
                        start = System.nanoTime();
                        MTableGenerator mtableGenerator = new MTableGenerator(k, p, alpha, false);
                        end = System.nanoTime();
                        double timeMtable = (end - start) / 1000000000.0;

                        String line = ""+k+","+timeSymmetricTree+","+timeAsymmetricTree+","+timeMtable+'\n';
                        Main.appendStrToFile(fileName,line);
                    }
                }
                if(args[0].equals("runtime-cdf")){
                    int trialsMax = 10000;
                    double[] p = {0.2, 0.3, 0.5};
                    double p_bin = 0.5;
                    String fileName = args[args.length-1];
                    String head = "trials, time for mcdf, time for bcdf" +'\n';
                    Main.appendStrToFile(fileName, head);
                    for(int trials = 10; trials<=trialsMax; trials+=10) {

                        //runtime mcdf
                        int[] signatureAsArray = new int[3];
                        signatureAsArray[0] = trials;
                        signatureAsArray[1] = (int) Math.round(trials / p[1]);
                        signatureAsArray[2] = (int) Math.round(trials / p[2]);
                        long start = System.nanoTime();
                        double mcdf = MultinomialDist.cdf(trials, p, signatureAsArray);
                        long end = System.nanoTime();
                        double timeMcdf = (end - start) / 1000000000.0;

                        //runtime cdf
                        start = System.nanoTime();
                        BinomialDistribution dist = new BinomialDistribution(trials, p_bin);
                        double cdf =dist.cumulativeProbability(trials,trials/2);
                        end = System.nanoTime();
                        double timeCdf = (end - start) / 1000000000.0;

                        String line = ""+trials+","+timeMcdf+","+timeCdf+""+'\n';
                        Main.appendStrToFile(fileName,line);
                    }
                }
                if(args[0].equals("runtime-multinomial-adjust")){
                    System.out.println("WARNING: This will move your mcdfcache and mtree files to an archive directory!");
                    moveMcdfStorageFilesToArchive();
                    moveMTreeStorageFilesToArchive();
                    int kMax = 200;
                    double[] p = {1.0/3.0, 1.0/3.0, 1.0/3.0};
                    double alpha = 0.1;
                    double p_bin = 0.5;
                    String fileName = args[args.length-1];
                    String head = "k, time for regression adjustment, time for binary search adjustment" +'\n';
                    Main.appendStrToFile(fileName, head);
                    for(int k=10; k<=kMax; k+=10){
                        long start = System.nanoTime();
                        MTree mTree = new MTree(k,p,alpha,true,false,true);
                        long end = System.nanoTime();
                        double timeRegression = (end - start) / 1000000000.0;

                        moveMcdfStorageFilesToArchive();
                        moveMTreeStorageFilesToArchive();

                        start = System.nanoTime();
                        MTree mTree2 = new MTree(k,p,alpha,true,false,false);
                        end = System.nanoTime();
                        double timeBinary = (end - start) / 1000000000.0;

                        moveMcdfStorageFilesToArchive();
                        moveMTreeStorageFilesToArchive();

                        String line = ""+timeRegression+","+timeBinary+'\n';
                        Main.appendStrToFile(fileName,line);
                    }
                }
                if (args[0].equals("data")) {
                    String datafile = args[1];
                    int k = Integer.parseInt(args[2]);
                    String[] pStringArray = args[3].split(",");
                    double[] p = Arrays.stream(pStringArray)
                            .mapToDouble(Double::parseDouble)
                            .toArray();
                    double alpha = Double.parseDouble(args[4]);
                    String resultFilename = args[5] + "_k=" + k + "_p=" + Arrays.toString(p) + "_alpha=" + alpha;

                    Main.prepareDataExperiments(datafile, ",", true);
                    MultinomialFairRanker ranker = new MultinomialFairRanker(k, p, alpha, true, unfairRanking);
                    Main.fairRanking = ranker.buildFairRanking(FairRankingStrategy.MOST_LIKELY, k);
                    Main.writeRankingsToCSV(resultFilename);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException("Directories ./storage/mtree and ./storage/mcdfcache required.");
        }
    }

    private static void moveMTreeStorageFilesToArchive() throws IOException {
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mtree");
        stringBuilder.append(File.separator);
        currentRelativePath = Paths.get(stringBuilder.toString());
        File mcdfCacheDir = currentRelativePath.toFile();
        if(Files.notExists(Paths.get(mcdfCacheDir.getAbsolutePath() + File.separator+"archive"))){
            new File(mcdfCacheDir.getAbsolutePath() + File.separator+"archive"). mkdir();
        }
        for(File file : mcdfCacheDir.listFiles()){
            if(!file.isDirectory()){
                Files.move(file.toPath(),
                        Paths.get(mcdfCacheDir.getAbsolutePath() + File.separator+"archive"
                                + File.separator + file.getName()));
            }
        }
    }

    private static void moveMcdfStorageFilesToArchive() throws IOException {
        Path currentRelativePath = Paths.get("");
        StringBuilder stringBuilder = new StringBuilder(currentRelativePath.toAbsolutePath().toString());
        stringBuilder.append(File.separator);
        stringBuilder.append("storage");
        stringBuilder.append(File.separator);
        stringBuilder.append("mcdfcache");
        stringBuilder.append(File.separator);
        currentRelativePath = Paths.get(stringBuilder.toString());
        File mcdfCacheDir = currentRelativePath.toFile();
        if(Files.notExists(Paths.get(mcdfCacheDir.getAbsolutePath() + File.separator+"archive"))){
            new File(mcdfCacheDir.getAbsolutePath() + File.separator+"archive"). mkdir();
        }
        for(File file : mcdfCacheDir.listFiles()){
            if(!file.isDirectory()){
                Files.move(file.toPath(),
                        Paths.get(mcdfCacheDir.getAbsolutePath() + File.separator+"archive"
                                + File.separator + file.getName()));
            }
        }
    }
}

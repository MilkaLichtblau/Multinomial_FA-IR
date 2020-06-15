package algorithm;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class Main {
    private static ArrayList<String> columnHeaders;
    private static List<Candidate> unfairRanking;

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

    private static void parseParametersForFailProbExperiment(String[] args) {
        int kMax = Integer.parseInt(args[1]);
        ArrayList<Double> pList = new ArrayList<>();
        for(int i=2; i<args.length-1; i++){
            if(args[i].charAt(0) == 'p'){
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
                }else{
                    d = Double.parseDouble(decimal);
                }
                pList.add(d);
            }else{
                break;
            }
        }
        if(pList.size()<2){
            throw new IllegalArgumentException("state at least 2 minimum proportion values in the style: " +
                    "p0.2 p0.2 .... . You may write p1.0/3.0 ..");
        }
        double[] p = new double[pList.size()];
        for(int i=0; i<p.length; i++){
            p[i] = pList.get(i);
        }
        double alpha = Double.parseDouble(args[args.length-1]);
        runFailProbabilityExperiment(kMax,p,alpha);
    }

    public static void appendStrToFile(String fileName, String str) {
        Path currentRelativePath = Paths.get("");
        String extendedFileName = currentRelativePath.getParent().toAbsolutePath().toString() + File.separator +
                "experiments" +
                File.separator +
                "dataExperiments" +
                File.separator +
                "results" +
                File.separator +
                "FailProbabilityExperiments" +
                File.separator +
                fileName;
        try {
            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(
                    new FileWriter(extendedFileName, true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runFailProbabilityExperiment(int kMax, double[] p, double alpha) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("failProbabilityExperiment_");
        stringBuilder.append(kMax);
        stringBuilder.append("_");
        for (double v : p) {
            stringBuilder.append(v);
            stringBuilder.append('_');
        }
        stringBuilder.append(alpha);
        stringBuilder.append(".csv");
        String fileName = stringBuilder.toString();
        String head = "k,failProbability" + '\n';
        Main.appendStrToFile(fileName, head);
        for (int k = 5; k <= kMax; k+=5) {
            if (k >= 500) {
                k += 50;
            }
            MTree tree = new MTree(k, p, alpha, false);
            double failProb = tree.getFailprob();
            Main.appendStrToFile(fileName,""+k+","+ failProb +'\n');
            System.out.println("finished writing failProbFor k = "+k);
        }

    }
    
    public static void main(String[] args) {
        try {
            if(args[0].equals("failprob")){
                parseParametersForFailProbExperiment(args);
            }
            if(args[0].equals("prepare")){
                prepareDataExperiments("..", ",", true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

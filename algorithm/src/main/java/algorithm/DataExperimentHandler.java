package algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import algorithm.MTree.FairRankingStrategy;

public class DataExperimentHandler {
    
    private ArrayList<String> columnHeaders;
    private List<Candidate> unfairRanking;
    private List<Candidate> fairRanking;
    private String resultFilename;
    
    public DataExperimentHandler(String resultFilename) {
        this.resultFilename = resultFilename;
    }
    
    public void prepareDataExperiment(String filename, int k, String separator, boolean hasHeaders) throws IOException {
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

        //write ranking to CSV for later evaluation
        String unfairResultFilename = this.resultFilename + "_unfair.csv";
        try {
            //write headers to new file
            BufferedWriter writer = new BufferedWriter(new FileWriter(unfairResultFilename));
            writer.write("uuid, score, group\n");
            writer.close();
            for (Candidate candidate : unfairRanking) {
                appendStrToFile(unfairResultFilename, candidate.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void runDataExperiment(int k, double[] p, double alpha) {
        MultinomialFairRanker ranker = new MultinomialFairRanker(k, p, alpha, true, unfairRanking);
        fairRanking = ranker.buildFairRanking(FairRankingStrategy.MOST_LIKELY, k);
        String fairResultFilename = resultFilename + "_fair.csv";
        try {
            //write headers to new file
            BufferedWriter writer = new BufferedWriter(new FileWriter(fairResultFilename));
            writer.write("uuid, score, group\n");
            writer.close();
            for (Candidate candidate : fairRanking) {
                appendStrToFile(fairResultFilename, candidate.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void appendStrToFile(String fileName, String str) {
        try {
            // Open given file in append mode.
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
            out.write(str);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


}

package algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
    
    public static void main(String[] args) {
        String datafile = args[1];
        
        try {
            prepareDataExperiments(datafile, ",", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

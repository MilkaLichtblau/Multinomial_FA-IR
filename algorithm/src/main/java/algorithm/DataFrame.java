package algorithm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class DataFrame {
    private Double[][] data;
    private String[] columnHeaders;
    

    public Double[][] readCSV(String filename, String separator, boolean hasHeaders) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        List<Double[]> lines = new ArrayList<Double[]>();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            String[] cells = line.split(separator);
            if (hasHeaders) {
                //first line are column headers
                this.columnHeaders = cells;
                hasHeaders = false;
                continue;
            }
            Double[] cellsAsDouble = Arrays.stream(cells).map(Double::valueOf).toArray(Double[]::new);
            lines.add(cellsAsDouble);
        }
        assert columnHeaders.length == lines.size();
        reader.close();
        // convert list to a String array.
        this.data = new Double[lines.size()][0];
        lines.toArray(this.data);
        return this.data;
    }

    public Double[][] getData() {
        return data;
    }
    
    public Double[] getRow(int index) {
        Double[] row = new Double[this.data.length];  
        for(int i=0; i<row.length; i++){
           row[i] = this.data[index][i];
        }
        return row;
    }
    
    public Double[] getColumn(String columnName) {
        int colIndex = ArrayUtils.indexOf(this.columnHeaders, columnName);
        Double[] column = new Double[this.data[0].length];  
        for(int i=0; i<column.length; i++){
           column[i] = this.data[i][colIndex];
        }
        return column;
    }
    
    public Double[] getColumn(int index) {
        Double[] column = new Double[this.data[0].length];  
        for(int i=0; i<column.length; i++){
           column[i] = this.data[i][index];
        }
        return column;
    }
    
    public String[] getColumnHeaders() {
        return columnHeaders;
    }
}

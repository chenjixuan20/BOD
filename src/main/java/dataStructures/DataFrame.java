package dataStructures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataFrame {
    protected List<List<Integer>> data = new ArrayList<>();
    private List<String> columnName = new ArrayList<>();

    public List<String> getColumnName() {
        return columnName;
    }

    public void setColumnName(List<String> columnName) {
        this.columnName = columnName;
    }

    public int getColumnCount() {
        return columnName.size();
    }

    public int getRowCount(){
        return data.size();
    }

    public List<List<Integer>> getData(){
        return this.data;
    }

    public List<Integer> getRow(int row){
        return data.get(row);
    }

    public int getCell(int row, int column){
        return data.get(row).get(column);
    }

    public static DataFrame fromCsv(String filePath){
        return  fromCsv(filePath, "utf-8", true);
    }

    public static DataFrame fromCsv(String filePath, String encodingm, boolean hasHead){
        try{
            File inputFile = new File(filePath);
            if(!inputFile.exists()){
                return null;
            }
            FileInputStream fis = new FileInputStream(inputFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            DataFrame result = new DataFrame();
            if(hasHead){
                String line = reader.readLine();
                if(line == null){
                    return null;
                }
                String[] parts = line.split(",");
                result.columnName = Arrays.asList(parts);
            }
            String line;
            while ((line = reader.readLine()) != null){
                List<Integer> list = new ArrayList<>();
                String[] parts = line.split(",");
                for(String part : parts){
                    list.add(Integer.parseInt(part));
                }
                result.data.add(list);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

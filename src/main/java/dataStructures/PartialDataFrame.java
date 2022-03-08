package dataStructures;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PartialDataFrame extends DataFrame {
    private Set<Integer> rowIndexes;
    private List<Integer> realIndexes;
    private DataFrame originalDataFrame;

    public List<Integer> getRealIndexes(){
        return this.realIndexes;
    }

    public DataFrame getOriginalDataFrame(){
        return this.originalDataFrame;
    }

    public Set<Integer> getRowIndexes(){
        return this.rowIndexes;
    }

    public PartialDataFrame(DataFrame dataFrame, Set<Integer> rowIndexes) {
        this.originalDataFrame=dataFrame;
        this.rowIndexes =rowIndexes;
        //realIndexes克隆rowIndexes
        this.realIndexes=new ArrayList<>(rowIndexes);
        for (Integer row : rowIndexes) {
            //将行索引在r中对应的行，抽样到PartData中，也就是继承父类DataFrame的data
            data.add(originalDataFrame.getRow(row));
        }
    }

    public boolean containRow(int row){
        return rowIndexes.contains(row);
    }
    public void addRow(int row){
        if(!rowIndexes.contains(row)){
//            System.out.println("addRow");
            rowIndexes.add(row);
//            System.out.println("row: " + row);
//            System.out.println(originalDataFrame.getRow(row));
            data.add(originalDataFrame.getRow(row));
            realIndexes.add(row);
        }
    }

    public int getRowsCount(){
        return super.getRowCount();
    }

    public void addRows(Collection<Integer> rows){

        for (Integer row : rows) {
            addRow(row);
        }
    }

    public int getRealIndex(int row){
        return realIndexes.get(row);
    }

}

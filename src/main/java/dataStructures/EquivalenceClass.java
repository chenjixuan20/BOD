package dataStructures;

import dataStructures.fd.FDTreeNodeEquivalenceClasses;
import dataStructures.od.AttributeAndDirection;
import dataStructures.od.DataAndIndex;
import dataStructures.od.ODTree;
import dataStructures.od.ODTreeNodeEquivalenceClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class EquivalenceClass{
    public int[] indexes;
    public List<Integer> clusterBegins;
    private final static Comparator<DataAndIndex> reverseComparator = (i1, i2) -> i2.data-i1.data;
    private final static Comparator<DataAndIndex> normalComparator = (i1,i2) -> i1.data-i2.data;

    public boolean initialized(){
        return indexes!=null;
    }

    /**
     * OD
     * 初始化列组合的EquivalenceClass
     * @param data
     */
    private void initialize(DataFrame data){
        int countRow = data.getRowCount();
        indexes = new int[countRow];
        /*
        rowIndex从0开始，index中也从0开始
         */
        for (int i = 0; i < countRow; i++) {
            indexes[i] = i;
        }
        clusterBegins=new ArrayList<>();
        clusterBegins.add(0);
        clusterBegins.add(countRow);
    }

    /**
     * OD
     * 新的attribute加入了attributeList
     * 获得加入了新attritube的新的EquivalenceClass
     * @param attributeAndDirection
     * @param data
     * @return
     */
    public EquivalenceClass merge(DataFrame data, AttributeAndDirection attributeAndDirection){
        //lazy update
        if(!initialized()){
            initialize(data);
        }

        int column = attributeAndDirection.attribute;
        Comparator<DataAndIndex> comparator= attributeAndDirection.direction == AttributeAndDirection.UP
                ?normalComparator:reverseComparator;

        //若sp中cluster数已经等于row数，则不用进行计算了，直接返回
        if(clusterBegins.size()==data.getRowCount()+1)
            return this;
        List<Integer> newBegins = new ArrayList<>();
        //对每一个cluster进行处理，若col的值不同，则拆分
        for(int beginPointer = 0; beginPointer < clusterBegins.size() - 1; beginPointer++) {
            int groupBegin = clusterBegins.get(beginPointer);
            int groupEnd = clusterBegins.get(beginPointer + 1);
            //若cluster大小为1，则直接作为处理后的cluster，继续处理下一个cluster
            if(groupBegin == groupEnd-1){
                newBegins.add(groupBegin);
                continue;
            }
            int value = 0;
            try {
                value = data.getCell(indexes[groupBegin],column);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean same = true;
            List<DataAndIndex> mergeData=new ArrayList<>();
            for(int i = groupBegin; i < groupEnd; i++){
                int row=indexes[i];
                int rowValue=data.getCell(row,column);
                if(rowValue!=value){
                    same=false;
                }
                //记录新加入的col在这个cluster中的DataAndIndex
                mergeData.add(new DataAndIndex(rowValue,row));
            }
            //新加入的col在cluster中的记录的行的值都相同则直接加入
            if(same){
                newBegins.add(groupBegin);
                continue;
            }
            //对这些DataAndIndex按照给定规则排序
            mergeData.sort(comparator);
            int fillPointer = groupBegin;
            //生成新的index和clusterBegins
            for (int i = 0; i < mergeData.size(); i++) {
                if(i == 0 || mergeData.get(i-1).data != mergeData.get(i).data){
                    newBegins.add(fillPointer);
                }
                indexes[fillPointer] = mergeData.get(i).index;
                fillPointer++;
            }
        }
        clusterBegins=newBegins;
        clusterBegins.add(data.getRowCount());
        return this;
    }

    public EquivalenceClass fdMerge(int attribute, DataFrame data){
        //lazy update
        if(!initialized()){
            initialize(data);
        }

        int column = attribute;
        Comparator<DataAndIndex> comparator = normalComparator;

        if(clusterBegins.size() == data.getRowCount()+1)
            return this;

        List<Integer> newBegins = new ArrayList<>();
        for(int beginPointer=0; beginPointer < clusterBegins.size()-1; beginPointer++) {
            int groupBegin = clusterBegins.get(beginPointer);
            int groupEnd = clusterBegins.get(beginPointer + 1);
            if(groupBegin == groupEnd-1){
                newBegins.add(groupBegin);
                continue;
            }
            int value=0;
            try {
                value=data.getCell(indexes[groupBegin],column);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean same = true;
            List<DataAndIndex> mergeData=new ArrayList<>();
            for(int i = groupBegin; i < groupEnd; i++){
                int row=indexes[i];
                int rowValue=data.getCell(row,column);
                if(rowValue!=value){
                    same=false;
                }
                mergeData.add(new DataAndIndex(rowValue,row));
            }
            if(same){
                newBegins.add(groupBegin);
                continue;
            }
            mergeData.sort(comparator);
            int fillPointer = groupBegin;
            for (int i = 0; i < mergeData.size(); i++) {
                if(i == 0 || mergeData.get(i-1).data != mergeData.get(i).data){
                    newBegins.add(fillPointer);
                }
                indexes[fillPointer] = mergeData.get(i).index;
                fillPointer++;
            }
        }
        clusterBegins=newBegins;
        clusterBegins.add(data.getRowCount());
        return this;
    }

    @Override
    public String toString() {
        return "EquivalenceClass{" +
                "indexes = " + Arrays.toString(indexes) +
                ", begins = " + clusterBegins +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o == null) return false;
        if (!(o instanceof EquivalenceClass)) return false;
        EquivalenceClass that = (EquivalenceClass) o;
        if( this.clusterBegins == that.clusterBegins) return true;
        if(this.clusterBegins == null || that.clusterBegins == null) return false;
        return this.clusterBegins.size() == that.clusterBegins.size();
    }

    public EquivalenceClass deepClone(){
        EquivalenceClass result = new EquivalenceClass();
        if(initialized()){
            result.indexes = Arrays.copyOf(indexes, indexes.length);
            result.clusterBegins = new ArrayList<>(clusterBegins);
        }
        return result;
    }


    public static void main(String[] args) {
        DataFrame data = DataFrame.fromCsv("Data/WorldPopulation-int.csv");
        AttributeAndDirection A = AttributeAndDirection.getInstance(0,AttributeAndDirection.UP);
        AttributeAndDirection B = AttributeAndDirection.getInstance(4,AttributeAndDirection.UP);
        AttributeAndDirection C = AttributeAndDirection.getInstance(6,AttributeAndDirection.UP);
        AttributeAndDirection D = AttributeAndDirection.getInstance(3,AttributeAndDirection.UP);
        ODTreeNodeEquivalenceClasses ode = new ODTreeNodeEquivalenceClasses();

        ode.mergeNodeTest(B, data, ODTree.ODTreeNodeStatus.SPLIT);
        ode.mergeNodeTest(C, data, ODTree.ODTreeNodeStatus.SPLIT);
        ode.mergeNodeTest(A, data, ODTree.ODTreeNodeStatus.SPLIT);

        ode.mergeNodeTest(B, data, ODTree.ODTreeNodeStatus.VALID);
        ode.mergeNodeTest(C, data, ODTree.ODTreeNodeStatus.VALID);
        ode.mergeNodeTest(D, data, ODTree.ODTreeNodeStatus.VALID);
        System.out.println(ode);
        System.out.println(ode.check(data).status);


    }
}

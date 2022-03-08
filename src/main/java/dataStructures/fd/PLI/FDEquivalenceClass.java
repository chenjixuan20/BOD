package dataStructures.fd.PLI;

import dataStructures.DataFrame;
import dataStructures.EquivalenceClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * PLI版本
 */

public class FDEquivalenceClass {
    public List<List<Integer>> PLI = new ArrayList<>();

    public boolean initialized(){
        return !PLI.isEmpty();
    }

    public FDEquivalenceClass initialize(DataFrame data, int attribute){
        HashMap<Integer, List<Integer>> valueIndexMap = new HashMap<>();
        int rowCount = data.getRowCount();
        int col = attribute;
        for(int i = 0; i < rowCount; i++){
            int value = data.getCell(i, col);
            if(!valueIndexMap.containsKey(value)){
                List<Integer> list = new ArrayList<>();
                list.add(i);
                valueIndexMap.put(value, list);
            }else{
                List<Integer> list = valueIndexMap.get(value);
                list.add(i);
                valueIndexMap.put(value,list);
            }
        }
        for(List<Integer> list:valueIndexMap.values()){
            if(list.size() >= 2){
                PLI.add(list);
            }
        }
        return this;
    }

    public FDEquivalenceClass fdMerge(DataFrame data, int attribute){
        if(!initialized()){
            return initialize(data, attribute);
        }
        int column = attribute;
        int clusterCount = PLI.size();
        if(clusterCount == 0){
            return this;
        }
        List<List<Integer>> newPLI = new ArrayList<>();
        for(int i = 0; i < clusterCount; i++){
            List<Integer> cluster = PLI.get(i);
            HashMap<Integer, List<Integer>> map = new HashMap<>();
            for(int j = 0; j < cluster.size(); j++){
                int index = cluster.get(j);
                int value = data.getCell(index, column);
                if(!map.containsKey(value)){
                    List<Integer> list = new ArrayList<>();
                    list.add(index);
                    map.put(value, list);
                }else {
                    List<Integer> list = map.get(value);
                    list.add(index);
                    map.put(value, list);
                }
            }
            for(List<Integer> list: map.values()){
                if(list.size() >= 2){
                    newPLI.add(list);
                }
            }
        }
        PLI = newPLI;
        return this;
    }

    @Override
    public String toString() {
        return "FDEquivalenceClass:{ PLI =  " + PLI + " }";
    }

    public FDEquivalenceClass deepClone(){
        FDEquivalenceClass result = new FDEquivalenceClass();
        if(initialized()){
            result.PLI = new ArrayList<>(PLI);
        }
        return result;
    }

    //测试PLI生成
    public static void main(String[] args) {
        DataFrame data = DataFrame.fromCsv("Data/test.csv");
        FDEquivalenceClass fdEquivalenceClass = new FDEquivalenceClass();
        fdEquivalenceClass.fdMerge(data, 5);
        System.out.println(fdEquivalenceClass.toString());
        fdEquivalenceClass.fdMerge(data,0);
        System.out.println(fdEquivalenceClass.toString());



        EquivalenceClass equivalenceClass = new EquivalenceClass();
        equivalenceClass.fdMerge(5,data);
        List<List<Integer>> lists = FDEquivalenceClass.changeToPLI(equivalenceClass);
        System.out.println(lists);
        equivalenceClass.fdMerge(0,data);
        lists = FDEquivalenceClass.changeToPLI(equivalenceClass);
        System.out.println(lists);

    }

    public static List<List<Integer>>  changeToPLI(EquivalenceClass equivalenceClass){
        List<List<Integer>> lists = new ArrayList<>();
        List<Integer> begin = equivalenceClass.clusterBegins;
        int[] indexs = equivalenceClass.indexes;
        for(int i = 0; i < begin.size()-1; i++){
            int clusterBegin = begin.get(i);
            int clusterEnd = begin.get(i+1);
            if(clusterEnd > clusterBegin+1){
                List<Integer> cluster = new ArrayList<>();
                for(int j = clusterBegin; j < clusterEnd; j++){
                    cluster.add(indexs[j]);
                }
                lists.add(cluster);
            }
        }
        return lists;
    }

}

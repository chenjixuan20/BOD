package dataStructures.fd;

import dataStructures.DataFrame;
import dataStructures.EquivalenceClass;
import util.Timer;

import java.util.*;

public class FDTreeNodeEquivalenceClasses{
    public EquivalenceClass left;
    public EquivalenceClass right;
    public static long mergeTime = 0;
    public static long checkTime = 0;
    public static long refinementTime = 0;
    public static long cloneTime = 0;


    public FDTreeNodeEquivalenceClasses(){
        left = new EquivalenceClass();
        right = new EquivalenceClass();
    }

    public FDTreeNodeEquivalenceClasses(EquivalenceClass left, EquivalenceClass right){
        this.left = left;
        this.right = right;
    }

    /**
     * 得到列索引在right中的那一个cluster中
     * @return
     */
    public int[] getRowToRightClusterIndex(){
        int[] rowToRightClusterIndex = new int[right.indexes.length];
        int rightClusterIndex = -1;
        int clusterBeignsPointer = 0;
        for(int i = 0; i < right.indexes.length; i++){
            if(i == right.clusterBegins.get(clusterBeignsPointer)){
                rightClusterIndex++;
                clusterBeignsPointer++;
            }
            rowToRightClusterIndex[right.indexes[i]] = rightClusterIndex;
        }
        return rowToRightClusterIndex;
    }

    /**
     * 传统的fd检测方法
     * @return
     */
    public String checkFDOld(){
        Timer timer =new Timer();
        int[] mapToRight = getRowToRightClusterIndex();
        if(left.clusterBegins.size() == (left.indexes.length + 1)){
            checkTime += timer.getTimeUsed();
            return "valid";
        }
        if(right.clusterBegins.size() == 2){
            checkTime += timer.getTimeUsed();
            return "valid";
        }
        for(int leftBeginPointer = 0; leftBeginPointer < left.clusterBegins.size() - 1; leftBeginPointer++){
            int groupBegin = left.clusterBegins.get(leftBeginPointer);
            int groupEnd = left.clusterBegins.get(leftBeginPointer + 1);
            if(groupEnd - groupBegin == 1) continue;
            int value = mapToRight[left.indexes[groupBegin]];
            for(int i = groupBegin + 1; i < groupEnd; i++){
                if(value != mapToRight[left.indexes[i]]){
                    checkTime += timer.getTimeUsed();
                    return "non-valid";
                }
                value = mapToRight[left.indexes[i]];
            }
        }
        checkTime += timer.getTimeUsed();
        return "valid";
    }

    public List<Integer> getVioHelper( List<Integer> leftBegin, List<Integer> newLeftBegin){
//        System.out.println(leftBegin);
//        System.out.println(newLeftBegin);
        List<Integer> segmentations = new ArrayList<>();
        List<Integer> rowIndexs = new ArrayList<>();
        int pre = 0;
        int rear = 1;
        int new_pre = 0;
        int new_rear = 1;
        while (rear < leftBegin.size() && new_rear < newLeftBegin.size()) {
            if(leftBegin.get(rear).equals(newLeftBegin.get(new_rear))){
                pre++;
                rear++;
                new_pre++;
                new_rear++;
            }else{
                //找到了leftBegin中被分割了的cluster
//                System.out.println(leftBegin.get(rear) == newLeftBegin.get(new_rear));
//                System.out.println(leftBegin.get(rear));
//                System.out.println(newLeftBegin.get(new_rear));
                //找到该cluster中最后一个分割点
                while (true){
                    if(new_rear < newLeftBegin.size() && !leftBegin.get(rear).equals(newLeftBegin.get(new_rear))){
                        new_rear++;
                    }else{
//                        System.out.println(new_rear);
                        break;
                    }
                }
                break;
            }
        }
        for(int i = new_pre + 1; i <= new_rear; i++){
            segmentations.add(newLeftBegin.get(i));
        }
        for(Integer segmentation : segmentations){
            rowIndexs.add(segmentation-1);
        }
        return rowIndexs;
    }

    public List<Set<Integer>> getVioHelperMoreCluster(List<Integer> leftBegin, List<Integer> newLeftBegin){
        Random rand = new Random();
        List<Set<Integer>> rowPairs = new ArrayList<>();
        int pre = 0;
        int rear = 1;
        int new_pre = 0;
        int new_rear = 1;
        while (rear < leftBegin.size() && new_rear < newLeftBegin.size()) {
            if(leftBegin.get(rear).equals(newLeftBegin.get(new_rear))){
                pre++;
                rear++;
                new_pre++;
                new_rear++;
            }else{
                //找到了leftBegin中被分割了的cluste 并找到该cluster中最后一个分割点
                while (true){
                    if(new_rear < newLeftBegin.size() && !leftBegin.get(rear).equals(newLeftBegin.get(new_rear))){
                        new_rear++;
                    }else{
                        Set<Integer> rowPair = new HashSet<>();
                        int MAX = new_rear;
                        int MIN = new_pre + 1;
                        while (rowPair.size() < 2){
                            int randNumber =rand.nextInt(MAX - MIN + 1) + MIN;
                            rowPair.add(newLeftBegin.get(randNumber) - 1);
                        }
                        rowPairs.add(rowPair);
                        break;
                    }
                }
                //发现3对vio-rows
                if(rowPairs.size() > 3) break;
                pre = rear;
                new_pre = new_rear;
                rear ++;
                new_rear++;
            }
        }
        List<Set<Integer>> choosePairs = new ArrayList<>();
        //随机抽一个
        while (choosePairs.size() < 1){
           int MAX = rowPairs.size()-1;
           int MIN = 0;
           int randNumber =rand.nextInt(MAX - MIN + 1) + MIN;
           choosePairs.add(rowPairs.get(randNumber));
       }
        return choosePairs;
    }

    public static List<Integer> getVioHelperLessRow( List<Integer> leftBegin, List<Integer> newLeftBegin){
        List<Integer> rowPairIndex = new ArrayList<>();
        int pre = 0;
        int rear = 1;
        int new_pre = 0;
        int new_rear = 1;
        while (rear < leftBegin.size() && new_rear < newLeftBegin.size()) {
            if(leftBegin.get(rear).equals(newLeftBegin.get(new_rear))){
                pre++;
                rear++;
                new_pre++;
                new_rear++;
            }else{
                //找到了leftBegin中被分割了的cluste 并找到该cluster中最后一个分割点
                while (true){
                    if(new_rear < newLeftBegin.size() && !leftBegin.get(rear).equals(newLeftBegin.get(new_rear))){
                        new_rear++;
                    }else{
                        int MAX = new_rear-1;
                        int MIN = new_pre;
                        rowPairIndex.add(newLeftBegin.get(MIN));
                        rowPairIndex.add(newLeftBegin.get(MAX));
                        break;
                    }
                }
            }
            if(rowPairIndex.size() != 0)
                break;
        }
        return rowPairIndex;
    }

    /**
     * refinement方法去验证fd，实验表明refinement方法更快
     * @param attribute
     * @param data
     * @return
     */
    public FDValidationResult checkFDRefinement(int attribute, DataFrame data){
        FDValidationResult result = new FDValidationResult();
        Timer timer = new Timer();
        //处理根节点
        if(this.left.indexes == null){
            EquivalenceClass newLeft = new EquivalenceClass();
            newLeft.fdMerge(attribute, data);
            if(newLeft.clusterBegins.size() == 2){
                result.status = "valid";
                return result;
            }else{
                result.status = "non-valid";
                return result;
            }
        }
        int clusterNum = left.clusterBegins.size();
        EquivalenceClass newLeft = left.deepClone();
        newLeft.fdMerge(attribute, data);
        int newClusterNum = newLeft.clusterBegins.size();
        refinementTime += timer.getTimeUsed();
        if(clusterNum == newClusterNum){
            result.status = "valid";
        }
        else{
            result.status = "non-valid";
            List<Integer> indexs = getVioHelperLessRow(left.clusterBegins, newLeft.clusterBegins);
            for(Integer index : indexs){
                result.violationRows.add(newLeft.indexes[index]);
            }
//            List<Set<Integer>> rowPairs = getVioHelperMoreCluster(left.clusterBegins, newLeft.clusterBegins);
//            for(Set<Integer> pairs : rowPairs){
//                for(Integer row : pairs){
//                    result.violationRows.add(left.indexes[row]);
//                }
//            }
        }
        return result;
   }

    public FDTreeNodeEquivalenceClasses mergeLeftNode(int attribute, DataFrame data){
        Timer timer = new Timer();
        left.fdMerge(attribute, data);
        mergeTime += timer.getTimeUsed();
        return this;
    }

    public FDTreeNodeEquivalenceClasses initializeRight(int attribute, DataFrame data){
        Timer timer = new Timer();
        right = new EquivalenceClass().fdMerge(attribute, data);
        mergeTime += timer.getTimeUsed();
        return this;
    }

    @Override
    public String toString() {
        return "FDTreeNodeEquivalenceClasses:{ " +
                " left: " + left + "}";
    }

    public FDTreeNodeEquivalenceClasses deepClone(){
        Timer timer = new Timer();
        FDTreeNodeEquivalenceClasses result = new FDTreeNodeEquivalenceClasses(left.deepClone(), right.deepClone());
        cloneTime += timer.getTimeUsed();
        return result;
    }

}

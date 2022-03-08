package dataStructures.fd.PLI;

import dataStructures.DataFrame;
import util.Timer;

public class FDTreeNodeEquivalenceClassesPLI {
    public FDEquivalenceClass left;
    public FDEquivalenceClass right;
    private static long mergeTime;
    private static long refinementTime;
    private static long cloneTime;

    public FDTreeNodeEquivalenceClassesPLI(){
        left = new FDEquivalenceClass();
        right = new FDEquivalenceClass();
    }

    public FDTreeNodeEquivalenceClassesPLI(FDEquivalenceClass left, FDEquivalenceClass right){
        this.left = left;
        this.right = right;
    }

    public FDTreeNodeEquivalenceClassesPLI mergeLeftNode(int attribute, DataFrame data){
        Timer timer = new Timer();
        left.fdMerge(data, attribute);
        mergeTime += timer.getTimeUsed();
        return this;
    }

    public FDTreeNodeEquivalenceClassesPLI initializeRight(int attribute, DataFrame data){
        right = new FDEquivalenceClass().fdMerge(data, attribute);
        return this;
    }

    public String checkFDRefinement(DataFrame data, int attribute){
        Timer timer = new Timer();
        int clusterNum = left.PLI.size();
        if(clusterNum == 0 ){
            refinementTime += timer.getTimeUsed();
            return "valid";
        }
        FDEquivalenceClass newLeft = left.deepClone();
        newLeft.fdMerge(data, attribute);
        int newLeftClusterNum = newLeft.PLI.size();
        if(clusterNum == newLeftClusterNum){
            int count = 0;
            int newCount = 0;

            for(int i = 0; i < left.PLI.size(); i++){
                count += left.PLI.get(i).size();
            }
            for(int i = 0; i < newLeft.PLI.size(); i++){
                newCount += newLeft.PLI.get(i).size();
            }
            if(count == newCount){
                refinementTime += timer.getTimeUsed();
                return "valid";
            }
        }
        refinementTime += timer.getTimeUsed();
        return "non-valid";
    }

//    public String checkFD(DataFrame data, int attribute){
//        int clusterNum = left.PLI.size();
//        if(clusterNum == 0 ){
//            return "valid";
//        }
//        FDEquivalenceClass newLeft = left.deepClone();
//        newLeft.fdMerge(data, attribute);
//        int newLeftClusterNum = newLeft.PLI.size();
//        if(clusterNum == newLeftClusterNum){
//
//        }
//
//    }

    public FDTreeNodeEquivalenceClassesPLI deepClone(){
        Timer timer = new Timer();
        FDTreeNodeEquivalenceClassesPLI result = new FDTreeNodeEquivalenceClassesPLI(left.deepClone(), right.deepClone());
        cloneTime += timer.getTimeUsed();
        return result;
    }

    @Override
    public String toString() {
        return "FDTreeNodeEquivalenceClassesPLI:{ left=" + left +" right=" + right + " }";
    }

}

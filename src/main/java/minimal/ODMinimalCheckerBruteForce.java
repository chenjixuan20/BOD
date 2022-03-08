package minimal;

import dataStructures.DataFrame;
import dataStructures.fd.FDCandidate;
import dataStructures.od.AttributeAndDirection;
import dataStructures.od.ODCandidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ODMinimalCheckerBruteForce extends ODMinimalChecker{
    public CopyOnWriteArrayList<ODCandidate> ods;

    public ODMinimalCheckerBruteForce(){
        ods = new CopyOnWriteArrayList<>();
    }


    public boolean isListMinimalFDMap(List<AttributeAndDirection> expandList,
                                      Map<String, List<List<Integer>>> fdMap){
        if(expandList.size() == 1) return true;
        String expandAttribute = String.valueOf(expandList.get(expandList.size() - 1).attribute);
        List<List<Integer>> leftOfExpandRight = fdMap.get(expandAttribute);
        List<Integer> expandListAttributes = new ArrayList<>();
        for(int i = 0; i < expandList.size() - 1; i++){
            expandListAttributes.add(expandList.get(i).attribute);
        }

        if(leftOfExpandRight == null){
            return true;
        }

        //FD前推后
        for (List<Integer> integers : leftOfExpandRight) {
            if (expandListAttributes.containsAll(integers))
                return false;
        }

        //OD后推前
        for(ODCandidate od : ods){
            List<AttributeAndDirection> left = od.leftAndRightAttributeList.left;
            List<AttributeAndDirection> right = od.leftAndRightAttributeList.right;
            int leftIndex = getIndex(expandList, left);
            int rightIndex = getIndex(expandList, right);
            if(leftIndex != -1 && rightIndex != -1 &&
                    rightIndex + right.size() == leftIndex){
                return false;
            }
            expandList = reverseDirection(expandList);
            leftIndex = getIndex(expandList, left);
            rightIndex = getIndex(expandList, right);
            if(leftIndex != -1 && rightIndex != -1 &&
                    rightIndex + right.size() == leftIndex){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isListMinimal(List<AttributeAndDirection> list){
        for(ODCandidate od : ods){
            List<AttributeAndDirection> left = od.leftAndRightAttributeList.left;
            List<AttributeAndDirection> right = od.leftAndRightAttributeList.right;

            int leftIndex = getIndex(list, left), rightIndex = getIndex(list, right);

            if(leftIndex != -1 && rightIndex != -1 &&
                    (
                            leftIndex < rightIndex  //前推后
                            || rightIndex + right.size() == leftIndex //后推前
                    )){
                return false;
            }
            list = reverseDirection(list);
            leftIndex = getIndex(list, left);
            rightIndex = getIndex(list, right);
            if(leftIndex != -1 && rightIndex != -1 &&
                    (
                            leftIndex < rightIndex   //前推后
                            || rightIndex + right.size() == leftIndex   //后推前
                    )){
                return false;
            }
        }
        return true;
    }

    @Override
    public void insert(ODCandidate candidate){
        ods.add(candidate);
    }

    private int getIndex(List<AttributeAndDirection> context, List<AttributeAndDirection> pattern){
        if(context.size() < pattern.size()) return -1;
        int end = context.size() - pattern.size();
        for(int i = 0; i <= end; i++){
            if(exactMatch(context, pattern, i ))
                return i;
        }
        return -1;
    }

    public static boolean m(List<Integer> expandList, List<List<Integer>> leftOfExpandRight){
        for (List<Integer> integers : leftOfExpandRight) {
            if (expandList.containsAll(integers))
                return false;
        }
        return true;
    }


}

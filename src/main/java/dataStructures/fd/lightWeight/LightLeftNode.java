package dataStructures.fd.lightWeight;


import dataStructures.fd.FDTreeNodeEquivalenceClasses;

import java.util.ArrayList;
import java.util.List;

public class LightLeftNode {
    public Integer attribute;

    public LightLeftNode[] nextLeft;

    public LightLeftNode parent;

    public FDTreeNodeEquivalenceClasses ecInDataSet;

    public Status status;


    public LightLeftNode(){

    }

    public LightLeftNode(Integer attribute, Integer attributeNum){
        this.attribute = attribute;
        this.nextLeft = new LightLeftNode[attributeNum];
        this.status = Status.UNKNOWN;
    }



    private Boolean hasNext(){
        if(this.nextLeft == null) return false;
        for(LightLeftNode node : this.nextLeft){
            if(node != null) return true;
        }
        return false;
    }


    private List<List<Integer>> print(){
        List<List<Integer>> res = new ArrayList<>();
        if(!this.hasNext()){
            List<Integer> list = new ArrayList<>();
            list.add(this.attribute);
            res.add(list);
            return res;
        }
        for(LightLeftNode node : this.nextLeft){
            if(node != null){
                List<List<Integer>> sub = node.print();
                for(List<Integer> subList : sub){
                    List<Integer> temp = new ArrayList<>(subList);
                    temp.add(0,this.attribute);
                    res.add(temp);
                }
            }
        }
        return res;
    }


    @Override
    public String toString() {
        return this.print().toString();
    }

    public enum Status{
        VALID,SPLIT,UNKNOWN;

        @Override
        public String toString() {
            if(equals(VALID))
                return "V";
            else if(equals(SPLIT))
                return "SP";
            else if(equals(UNKNOWN))
                return "UK";
            return "?";
        }
    }

}

package dataStructures.fd.lightWeight;


import java.util.*;

public class MapNode {
    private Integer RHS;

    private LightLeftNode[] LHSs;

    public MapNode(){
    }

    public MapNode(Integer RHS, Integer attributeNum){
        this.RHS = RHS;
        this.LHSs = new LightLeftNode[attributeNum];
    }

    public Integer getRHS() {
        return RHS;
    }

    public LightLeftNode[] getLHSs() {
        return LHSs;
    }

    @Override
    public String toString() {
        List<LightLeftNode> res = new ArrayList<>();
        for(LightLeftNode node : LHSs){
            if(node != null) res.add(node);
        }
        return "LHS: " + res.toString();
    }
}

package dataStructures.od;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ODCandidate implements Comparable<ODCandidate>{
    public LeftAndRightAttributeList leftAndRightAttributeList;
    public List<AttributeAndDirection> odPath;
    public ODTree.ODTreeNode odByODTreeNode;

    /**
     * 根据ODTree上的ODTreeNode，得到ODCandidate
     * @param odByODTreeNode
     */
    public ODCandidate(ODTree.ODTreeNode odByODTreeNode){
        if((odByODTreeNode == null)) return;
        ODTree.ODTreeNode node = odByODTreeNode;
        this.odByODTreeNode = odByODTreeNode;
        odPath = new ArrayList<>();
        leftAndRightAttributeList = new LeftAndRightAttributeList();
        while(!node.isRoot()){
            odPath.add(node.attribute);

            if(node.parent.status == ODTree.ODTreeNodeStatus.VALID)
                leftAndRightAttributeList.right.add(node.attribute);
            else if(node.parent.status == ODTree.ODTreeNodeStatus.SPLIT)
                leftAndRightAttributeList.left.add(node.attribute);
            node = node.parent;
        }
        Collections.reverse(odPath);
        Collections.reverse(leftAndRightAttributeList.left);
        Collections.reverse(leftAndRightAttributeList.right);
    }

    @Override
    public int compareTo(ODCandidate o) {
        return odPath.size()-o.odPath.size();
    }

    @Override
    public String toString(){
        return "["+leftAndRightAttributeList.toString()+"]";
    }



}

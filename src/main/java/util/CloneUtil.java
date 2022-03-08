package util;

import dataStructures.od.AttributeAndDirection;
import dataStructures.od.ODTree;

import java.util.ArrayList;
import java.util.List;

public class CloneUtil{

    public <T>List<T> deepCloneList(List<T> list){
        return new ArrayList<T>(list);
    }

    public void copyConfirmNode(ODTree resultTree, ODTree.ODTreeNode resultTreeNode, ODTree.ODTreeNode referenceTreeNode){
        for (ODTree.ODTreeNode referenceChildNode:referenceTreeNode.children) {
            if(referenceChildNode!=null && referenceChildNode.confirm){
                ODTree.ODTreeNode resultChildNode =resultTree.new ODTreeNode
                        (resultTreeNode,referenceChildNode.attribute);
                resultChildNode.status=referenceChildNode.status;
                resultChildNode.confirm();
                copyConfirmNode(resultTree,resultChildNode,referenceChildNode);
            }
        }
    }

}

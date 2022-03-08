package minimal;

import dataStructures.fd.FDCandidate;
import dataStructures.od.AttributeAndDirection;
import dataStructures.od.ODCandidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ODMinimalCheckCocurrentTree extends ODMinimalChecker  {

    private int countAttribute;
    private ODMinimalCheckCocurrentTreeNode root;


    @Override
    public synchronized void insert(ODCandidate candidate){
        List<AttributeAndDirection> left = candidate.leftAndRightAttributeList.left;
        List<AttributeAndDirection> right = candidate.leftAndRightAttributeList.right;

        AttributeAndDirection leftLast=left.get(left.size()-1);
        AttributeAndDirection rightLast=right.get(right.size()-1);
        //first insert left->right
        if(leftLast.direction==AttributeAndDirection.DOWN){
            left=reverseDirection(left);
            right=reverseDirection(right);
        }
        ODMinimalCheckCocurrentTree.ODMinimalCheckCocurrentTreeNode node=findNode(left);
        if(node.rightList ==null){
            node.rightList =new CopyOnWriteArrayList<>();
        }
        node.rightList.add(right);
        //next insert right -> left
        if(rightLast.direction!= leftLast.direction){
            if(rightLast.direction==AttributeAndDirection.DOWN) {
                left = reverseDirection(left);
                right = reverseDirection(right);
            }
            else {
                left=candidate.leftAndRightAttributeList.left;
                right=candidate.leftAndRightAttributeList.right;
            }
        }
        node=findNode(right);
        if(node.leftList==null){
            node.leftList=new CopyOnWriteArrayList<>();
        }
        node.leftList.add(left);
    }

    private ODMinimalCheckCocurrentTree.ODMinimalCheckCocurrentTreeNode findNode(List<AttributeAndDirection> list){
        ODMinimalCheckCocurrentTree.ODMinimalCheckCocurrentTreeNode node=root;
        for(int i=list.size()-1;i>=0;i--){
            AttributeAndDirection attributeAndDirection=list.get(i);
            int index=attributeAndDirection2childrenIndex(attributeAndDirection);
            if(node.children.get(index)==null){
                node=new ODMinimalCheckCocurrentTree.ODMinimalCheckCocurrentTreeNode(attributeAndDirection,node);
            }else {
                node=node.children.get(index);
            }
        }
        return node;
    }

    @Override
    public boolean isListMinimal(List<AttributeAndDirection> list){
        HashMap<Integer,Integer> attribute2Index=new HashMap<>();
        //attribute和list中的index对应
        for (int i = 0; i < list.size(); i++) {
            attribute2Index.put(list.get(i).attribute,i);
        }
        if(list.get(list.size()-1).direction==AttributeAndDirection.DOWN){
            list=reverseDirection(list);
        }
        ODMinimalCheckCocurrentTree.ODMinimalCheckCocurrentTreeNode node=root;
        for(int i=list.size()-1; i>=0; i--) {
            //得到list中的最后一个元素，也就是expandAttribute
            if(node.children == null) return false;
            AttributeAndDirection attributeAndDirection=list.get(i);
            node=node.children.get(attributeAndDirection2childrenIndex(attributeAndDirection));
            if(node==null)
                break;
            //case XLYR  前推后
            if(node.leftList!=null){
                for(List<AttributeAndDirection> pattern:node.leftList){
                    int leftBegin=attribute2Index.getOrDefault(pattern.get(0).attribute,-1);
                    if(leftBegin!=-1 && leftBegin+pattern.size()<=i && exactMatch(list,pattern,leftBegin))
                        return false;
                }
            }
            //case XRYL  后推前
            if (node.rightList != null) {
                for(List<AttributeAndDirection> pattern:node.rightList){
                    int rightBegin=attribute2Index.getOrDefault(pattern.get(0).attribute,-1);
                    if(rightBegin!=-1 && rightBegin+pattern.size()==i && exactMatch(list,pattern,rightBegin))
                        return false;
                }
            }
        }
        return true;
    }


    public ODMinimalCheckCocurrentTree(int countAttribute) {
        this.countAttribute = countAttribute;
        root=new ODMinimalCheckCocurrentTree.ODMinimalCheckCocurrentTreeNode(AttributeAndDirection.getInstance(0,AttributeAndDirection.UP),null);
    }

    private class ODMinimalCheckCocurrentTreeNode{
        AtomicReferenceArray<ODMinimalCheckCocurrentTreeNode> children;
        CopyOnWriteArrayList<List<AttributeAndDirection>> leftList;
        CopyOnWriteArrayList<List<AttributeAndDirection>> rightList;

        private ODMinimalCheckCocurrentTreeNode(AttributeAndDirection attribute
                , ODMinimalCheckCocurrentTree.ODMinimalCheckCocurrentTreeNode parent) {
            if(parent!=null){
                int index=attributeAndDirection2childrenIndex(attribute);
                parent.children.set(index, this);
            }
            children=new AtomicReferenceArray<>(new ODMinimalCheckCocurrentTree.ODMinimalCheckCocurrentTreeNode[2*countAttribute]);
        }
    }

    private int attributeAndDirection2childrenIndex(AttributeAndDirection attributeAndDirection){
        return attributeAndDirection.attribute+
                (attributeAndDirection.direction==AttributeAndDirection.DOWN?countAttribute:0);
    }
}

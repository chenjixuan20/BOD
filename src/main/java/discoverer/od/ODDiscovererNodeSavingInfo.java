package discoverer.od;

import dataStructures.od.AttributeAndDirection;
import dataStructures.od.ODTree;
import dataStructures.od.ODTreeNodeEquivalenceClasses;

import java.util.ArrayList;
import java.util.List;

public class ODDiscovererNodeSavingInfo {
    public ODTree.ODTreeNode nodeInResultTree;
    public ODTreeNodeEquivalenceClasses odTreeNodeEquivalenceClasses;
    public List<AttributeAndDirection> left ;
    public List<AttributeAndDirection> right ;

    public ODDiscovererNodeSavingInfo(ODTree.ODTreeNode nodeInResultTree,
                                      ODTreeNodeEquivalenceClasses odTreeNodeEquivalenceClasses){
        this.nodeInResultTree = nodeInResultTree;
        this.odTreeNodeEquivalenceClasses = odTreeNodeEquivalenceClasses;
    }

    public ODDiscovererNodeSavingInfo(ODTree.ODTreeNode nodeInResultTree,
                                      ODTreeNodeEquivalenceClasses odTreeNodeEquivalenceClasses,
                                      List<AttributeAndDirection> left,
                                      List<AttributeAndDirection> right){
        this.nodeInResultTree = nodeInResultTree;
        this.odTreeNodeEquivalenceClasses = odTreeNodeEquivalenceClasses;
        this.left = left;
        this.right = right;
    }

    public List<AttributeAndDirection> listDeepClone(List<AttributeAndDirection> list){
        List<AttributeAndDirection> result = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            result.add(list.get(i));
        }
        return result;
    }

    @Override
    public String toString() {
        return "left: " + left + "\n" + "right: " + right;
    }
}

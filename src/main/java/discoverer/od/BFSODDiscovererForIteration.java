package discoverer.od;

import dataStructures.DataFrame;
import dataStructures.fd.FDCandidate;
import dataStructures.od.AttributeAndDirection;
import dataStructures.od.ODCandidate;
import dataStructures.od.ODTree;
import dataStructures.od.ODTree.ODTreeNode;
import dataStructures.od.ODTreeNodeEquivalenceClasses;
import minimal.ODMinimalCheckTree;
import minimal.ODMinimalChecker;

import java.util.*;

/**
 * 直接计算SortedPatition版
 */
public class BFSODDiscovererForIteration extends ODDiscoverer{
    public static final int INITIAL_RETURN_THRESHOLD =200;
    private boolean complete=true;
    private Queue<ODDiscovererNodeSavingInfo> queue;
    private ODMinimalChecker odMinimalChecker;
    private int returnThreshold;

    public  Set<FDCandidate> fdCandidates = new HashSet<>();

    public boolean isComplete() {
        return complete;
    }

    public ODTree restartDiscovering(DataFrame data,ODTree reference){
        System.out.println("restartDiscovering");
        odMinimalChecker=new ODMinimalCheckTree(data.getColumnCount());
        queue=new LinkedList<>();
        ODTree result=new ODTree(data.getColumnCount());

        returnThreshold= INITIAL_RETURN_THRESHOLD;
        int attributeCount=data.getColumnCount();

        for (int attribute = 0; attribute < attributeCount; attribute++) {
            if(reference!=null) {
                copyConfirmNode(result, result.getRoot().children[attribute]
                        , reference.getRoot().children[attribute]);
            }
            ODTreeNodeEquivalenceClasses odTreeNodeEquivalenceClasses = new ODTreeNodeEquivalenceClasses();
            odTreeNodeEquivalenceClasses.mergeNode(result.getRoot().children[attribute], data);
            queue.offer(new ODDiscovererNodeSavingInfo(result.getRoot().children[attribute]
                    , odTreeNodeEquivalenceClasses));
        }
        BFSTraversing(data,result);
        return result;
    }

    public ODTree continueDiscovering(DataFrame data,ODTree tree){
        System.out.println("continueDiscovering");
        returnThreshold*=2;
        BFSTraversing(data, tree);
        return tree;
    }

    @Override
    public ODTree discover(DataFrame data, ODTree reference) {
        System.out.println(reference);
        if (complete)
            return restartDiscovering(data,reference);
        else
            return continueDiscovering(data,reference);
    }

    private void BFSTraversing(DataFrame data,ODTree result){
        System.out.println("BFSTraversing");
        int attributeCount=data.getColumnCount();
        int newFoundOdCount=0;
        while (!queue.isEmpty()) {

            ODDiscovererNodeSavingInfo info=queue.poll();
            ODTreeNode parent=info.nodeInResultTree;
            ODTreeNodeEquivalenceClasses parentEc = info.odTreeNodeEquivalenceClasses;

            for (int attribute = 0; attribute < attributeCount*2; attribute++) {
                ODTreeNode child;
                if(parent.children[attribute]==null){
                    child=result.new ODTreeNode(parent,result.getAttributeAndDirectionFromIndex(attribute));
                }
                else
                    child=parent.children[attribute];
                ODCandidate childCandidate=new ODCandidate(child);
                child.minimal = odMinimalChecker.isCandidateMinimal(childCandidate);
                if(!child.minimal)
                    continue;
                ODTreeNodeEquivalenceClasses odTreeNodeEquivalenceClasses =
                        parentEc.deepClone();
                odTreeNodeEquivalenceClasses.mergeNode(child,data);
                //fd前推后，
                if(odTreeNodeEquivalenceClasses.equals(parentEc)){
                    child.minimal = false;
                    //valid往右拓展
                    if(parent.status == ODTree.ODTreeNodeStatus.VALID){
                        produceFDCandidate(childCandidate.leftAndRightAttributeList.right);
                    }else {
                        produceFDCandidate(childCandidate.leftAndRightAttributeList.left);
                    }
                }
                if(!child.minimal)
                    continue;

                if(!child.confirm)
                    child.status=odTreeNodeEquivalenceClasses.check(data).status;
                if(child.status!= ODTree.ODTreeNodeStatus.SWAP){
                    queue.offer(new ODDiscovererNodeSavingInfo(child
                            ,odTreeNodeEquivalenceClasses));
                }
                if(child.status== ODTree.ODTreeNodeStatus.VALID){
                    odMinimalChecker.insert(childCandidate);
                    if(!child.confirm){
                        newFoundOdCount++;
                    }
                }
            }
            if(newFoundOdCount>=returnThreshold){
                complete=false;
                return;
            }
        }
        complete=true;
    }

    private void produceFDCandidate(List<AttributeAndDirection> expendList){
        int size = expendList.size();
        List<Integer> fdLeft = new ArrayList<>();
        for(int i = 0; i < size - 1; i++){
            fdLeft.add(expendList.get(i).attribute);
        }
        Collections.sort(fdLeft);
        FDCandidate fdCandidate = new FDCandidate(fdLeft, expendList.get(size-1).attribute);
        //放入set
        fdCandidates.add(fdCandidate);
    }


    private void copyConfirmNode(ODTree resultTree, ODTreeNode resultTreeNode, ODTreeNode referenceTreeNode){
        for (ODTreeNode referenceChildNode:referenceTreeNode.children) {
            if(referenceChildNode!=null && referenceChildNode.confirm){
                ODTreeNode resultChildNode =resultTree.new ODTreeNode
                        (resultTreeNode,referenceChildNode.attribute);
                resultChildNode.status = referenceChildNode.status;
                resultChildNode.confirm();
                copyConfirmNode(resultTree,resultChildNode,referenceChildNode);
            }
        }
    }

    @Override
    public ODTree discoverFD(DataFrame data, List<FDCandidate> fdCandidates) {
        return null;
    }


}

package discoverer.multiple.bod_x;

import dataStructures.DataFrame;
import dataStructures.fd.FDCandidate;
import dataStructures.od.AttributeAndDirection;
import dataStructures.od.ODCandidate;
import dataStructures.od.ODTree;
import dataStructures.od.ODTreeNodeEquivalenceClasses;
import discoverer.od.ODDiscoverer;
import discoverer.od.ODDiscovererNodeSavingInfo;
import minimal.ODMinimalCheckCocurrentTree;
import minimal.ODMinimalChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class LightWeightDiscoverer extends ODDiscoverer {
    private ConcurrentLinkedQueue<ODDiscovererNodeSavingInfo> queue;
    public static final int DISCOVERPOOLSIZE = 5;
    private ODMinimalChecker odMinimalChecker;
    public LightWeightThreadPool pool;
    public LightWeightTran tran;
    public ThreadPoolExecutor discoverExecutor = new ThreadPoolExecutor(DISCOVERPOOLSIZE,DISCOVERPOOLSIZE,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());
    public int odPoint = 0;
    public ODTree.ODTreeNode root;
    public int taskNum = 0;
    public AtomicInteger finishNum = new AtomicInteger(0);
    public boolean isInterrup = false;
    public boolean isDiscoverDone = false;
    public List<ODCandidate> odCandidates = new ArrayList<>();
    public CopyOnWriteArraySet<FDCandidate> fdCandidates = new CopyOnWriteArraySet<>();


    public ODTree startDiscovering(DataFrame data, ODTree reference){
        System.out.println("startDiscovering");
//        odMinimalChecker=new ODMinimalCheckTree(data.getColumnCount());
        odMinimalChecker = new ODMinimalCheckCocurrentTree(data.getColumnCount());
        queue=new ConcurrentLinkedQueue<>();
        ODTree result=new ODTree(data.getColumnCount());

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
        BFSTraversingWithThreads(data,result);
        return result;
    }


    @Override
    public ODTree discover(DataFrame data, ODTree reference) {
        odPoint = 0;
        taskNum = 0;
        isInterrup = false;
        isDiscoverDone = false;
        finishNum.getAndSet(0);
        discoverExecutor = new ThreadPoolExecutor(DISCOVERPOOLSIZE,DISCOVERPOOLSIZE,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());
        return startDiscovering(data,reference);
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


    private void copyConfirmNode(ODTree resultTree, ODTree.ODTreeNode resultTreeNode, ODTree.ODTreeNode referenceTreeNode){
        for (ODTree.ODTreeNode referenceChildNode:referenceTreeNode.children) {
            if(referenceChildNode!=null && referenceChildNode.confirm){
                ODTree.ODTreeNode resultChildNode =resultTree.new ODTreeNode
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

    public boolean isInterrup() {
        return isInterrup;
    }

    public void setInterrup(boolean interrup) {
        isInterrup = interrup;
    }

    public boolean isDiscoverDone() {
        return isDiscoverDone;
    }


    private void BFSTraversingWithThreads(DataFrame data,ODTree result){
        System.out.println("BFSTraversing");
        int attributeCount=data.getColumnCount();
        while (!queue.isEmpty()) {
            int size = queue.size();
            root = result.getRoot();
            if(isInterrup){
                isDiscoverDone = true;
                System.out.println("Interrup");
                return;
            }
            taskNum += size;
            for(int i = 0; i < size; i++){
                ODDiscovererNodeSavingInfo info=queue.poll();
                this.discoverExecutor.execute(()-> {
                    ODTree.ODTreeNode parent=info.nodeInResultTree;
                    ODTreeNodeEquivalenceClasses parentEc = info.odTreeNodeEquivalenceClasses;

                    for (int attribute = 0; attribute < attributeCount*2; attribute++) {
                        ODTree.ODTreeNode child;
                        if (parent.children[attribute] == null) {
                            child = result.new ODTreeNode(parent, result.getAttributeAndDirectionFromIndex(attribute));
                        } else
                            child = parent.children[attribute];
                        ODCandidate childCandidate = new ODCandidate(child);
                        child.minimal = odMinimalChecker.isCandidateMinimal(childCandidate);
                        if (!child.minimal)
                            continue;
                        ODTreeNodeEquivalenceClasses odTreeNodeEquivalenceClasses =
                                parentEc.deepClone();
                        odTreeNodeEquivalenceClasses.mergeNode(child, data);
                        //fd前推后，
                        if (odTreeNodeEquivalenceClasses.equals(parentEc)) {
                            child.minimal = false;
                            //valid往右拓展
                            if (parent.status == ODTree.ODTreeNodeStatus.VALID) {
                                produceFDCandidate(childCandidate.leftAndRightAttributeList.right);
                            } else {
                                produceFDCandidate(childCandidate.leftAndRightAttributeList.left);
                            }
                        }
                        if (!child.minimal)
                            continue;

                        if (!child.confirm)
                            child.status = odTreeNodeEquivalenceClasses.check(data).status;
                        if (child.status != ODTree.ODTreeNodeStatus.SWAP) {
                            queue.offer(new ODDiscovererNodeSavingInfo(child
                                    , odTreeNodeEquivalenceClasses));
                        }
                        if (child.status == ODTree.ODTreeNodeStatus.VALID) {
                            odMinimalChecker.insert(childCandidate);
                        }
                    }
                finishNum.getAndIncrement();
                if(taskNum == finishNum.get()) LockSupport.unpark(tran.producer);
            });

            }
            LockSupport.park(tran.producer);
            odCandidates = result.getAllOdsOrderByBFS();
            if(!isInterrup&&odPoint<odCandidates.size()) {
               tran.pushList(new ArrayList<>(odCandidates.subList(odPoint, odCandidates.size())));
                odPoint = odCandidates.size();
            }
        }
        isDiscoverDone = true;
        if((tran.pool.executor.getActiveCount() == 0)&&(tran.layerTask.isEmpty()))  LockSupport.unpark(tran.consumer);
    }
}
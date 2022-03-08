package validator.od;

import dataStructures.DataFrame;
import dataStructures.od.*;

import java.util.*;

public class ODPrefixBasedIncrementalValidator extends ODValidator{
    public static int VIOROWS_THRESHOLD = 10;

    @Override
    public Set<Integer> validate(ODTree tree, DataFrame data) {
        VisitCountConsideredMap<ODTree.ODTreeNode, ODTreeNodeEquivalenceClasses> cache
                =new VisitCountConsideredMap<>();

        Set<Integer> violationRows=new HashSet<>();
        List<ODCandidate> ods=tree.getAllOdsOrderByDFS();
        List<ODCandidate> notConfirmedODs= chooseODs(ods);

        if(notConfirmedODs.size()==0){
            return violationRows;
        }

        //得到了key node对应在visited set中最近的value node
        HashMap<ODTree.ODTreeNode, ODTree.ODTreeNode> nodeForNodeToGetCache=
                predictCacheVisitCount(cache,notConfirmedODs);

        cache.put(tree.getRoot(),new ODTreeNodeEquivalenceClasses());

        for(ODCandidate od:notConfirmedODs){
            ODValidationResult result= validateOneOdCandidate
                    (data,od.odByODTreeNode,cache,nodeForNodeToGetCache);
            violationRows.addAll(result.violationRows);
        }
        return violationRows;
    }

    protected List<ODCandidate> chooseODs(List<ODCandidate> ods){
        List<ODCandidate> result=new ArrayList<>();
        for (ODCandidate od:ods){
            if(!od.odByODTreeNode.confirm){
                result.add(od);
            }
        }
        return result;
    }


    /**
     * 对list中所有的od,找其在visited set中对应的那个父节点p，并把途径的父结点均加入visited set
     * 得到每个node的cache node（也就是每个结点对应的在visited set中的最近的父节点）
     * @param cache
     * @param ods
     * @return
     */
    private HashMap<ODTree.ODTreeNode, ODTree.ODTreeNode> predictCacheVisitCount(VisitCountConsideredMap<ODTree.ODTreeNode,ODTreeNodeEquivalenceClasses>
                                                                          cache, List<ODCandidate> ods){
        //visited set
        HashSet<ODTree.ODTreeNode> possibleCacheNodes=new HashSet<>();
        HashMap<ODTree.ODTreeNode, ODTree.ODTreeNode> result=new HashMap<>();
        //根结点（root)加入visited set
        possibleCacheNodes.add(ods.get(0).odByODTreeNode.getRoot());
        //对list中所有的od,找其在visited set中对应的那个父节点p，并把途径的父结点均加入visited set
        for(ODCandidate od:ods){
            ODTree.ODTreeNode odNode=od.odByODTreeNode,node=odNode;
            while (!possibleCacheNodes.contains(node)){
                possibleCacheNodes.add(node);
                node=node.parent;
            }
            //不太懂cache的作用
            cache.addVisitCount(node);
            //得到od对应的node(n)的visted set中的p结点
            result.put(odNode,node);
        }
        return result;
    }

    /**
     * 验证在抽样数据集r'中发现的某一个od candidate，在整体数据集r中是否成立
     * @param data
     * @param node
     * @param cache
     * @param nodeForNodeToGetCache
     * @return
     */
    protected ODValidationResult validateOneOdCandidate(DataFrame data, ODTree.ODTreeNode node,
                                                        VisitCountConsideredMap<ODTree.ODTreeNode,ODTreeNodeEquivalenceClasses> cache,
                                                        HashMap<ODTree.ODTreeNode, ODTree.ODTreeNode> nodeForNodeToGetCache
    ){
        if(!node.accessible()){
            return new ODValidationResult();
        }
        Stack<ODTree.ODTreeNode> path=new Stack<>();
        //从map中得到key为node的value cacheNode
        ODTree.ODTreeNode cacheNode=nodeForNodeToGetCache.get(node);
        //向上找父节点，直到找到cacheNode（也就是最近的在visited set中的父节点）
        do {
            //把向上路径中的父节点压入栈中
            path.push(node);
            node=node.parent;
        }while (node!=cacheNode);
        ODTreeNodeEquivalenceClasses odTreeNodeEquivalenceClasses=
                cache.get(node);
        if(cache.containKey(node)){
            odTreeNodeEquivalenceClasses=odTreeNodeEquivalenceClasses.deepClone();
        }
        while(!path.isEmpty()){
            node=path.pop();
            odTreeNodeEquivalenceClasses.mergeNode(node,data);
            if(cache.mayPut(node))
                cache.put(node,odTreeNodeEquivalenceClasses.deepClone());
            if(!node.confirm){
                node.confirm();
                ODValidationResult result=odTreeNodeEquivalenceClasses.check(data);
                if(result.status!=node.status){
                    cache.addVisitCount(cacheNode);
                    updateCacheVisitCountBeforeNodeChildrenCut
                            (node,cache,nodeForNodeToGetCache);
                    node.setStatus(result.status);
                    result.status= ODTree.ODTreeNodeStatus.UNKNOWN;
                    return result;
                }
            }
        }
        ODValidationResult result=new ODValidationResult();
        result.status=node.status;
        return result;
    }

    protected void updateCacheVisitCountBeforeNodeChildrenCut(ODTree.ODTreeNode nodeToCutChildren,
                                                              VisitCountConsideredMap<ODTree.ODTreeNode,ODTreeNodeEquivalenceClasses> cache,
                                                              HashMap<ODTree.ODTreeNode, ODTree.ODTreeNode> nodeForNodeToGetCache
    ){
        List<ODCandidate> ods=new ArrayList<>();
        nodeToCutChildren.getAllNodesOrderByDFSRecursion(ods, node -> node.minimal && node.status== ODTree.ODTreeNodeStatus.VALID);
        for (ODCandidate od : ods) {
            cache.addVisitCount(nodeForNodeToGetCache.get(od.odByODTreeNode),-1);
        }
    }
    public Set<Integer> validatebyods(ODTree tree, DataFrame data,boolean isVioRowsEmpty) {
        //不知道cache有什么用
        VisitCountConsideredMap<ODTree.ODTreeNode, ODTreeNodeEquivalenceClasses> cache
                =new VisitCountConsideredMap<>();
        List<ODCandidate> ods = tree.getAllOdsOrderByBFS();
        Set<Integer> violationRows=new HashSet<>();
        int index = 0;
        int count = 1;
        int vioRowsSize = 0;
        List<ODCandidate> notConfirmedODs= chooseODs(ods);

        if(notConfirmedODs.size()==0){
            return violationRows;
        }
        int taskNum = notConfirmedODs.size();
        boolean isTest = false;
        System.out.println("taskNUm:"+taskNum);
        if(taskNum >100&&!isVioRowsEmpty) {
            isTest =true;
        }
        //得到了key node对应在visited set中最近的value node
        HashMap<ODTree.ODTreeNode, ODTree.ODTreeNode> nodeForNodeToGetCache=
                predictCacheVisitCount(cache,notConfirmedODs);

        cache.put(tree.getRoot(),new ODTreeNodeEquivalenceClasses());

        for(ODCandidate od:notConfirmedODs){
            ODValidationResult result= validateOneOdCandidate
                    (data,od.odByODTreeNode,cache,nodeForNodeToGetCache);
            violationRows.addAll(result.violationRows);
            if(isTest){
                index++;
                if(index == (taskNum/10)*count){
                    if(violationRows.size() >= VIOROWS_THRESHOLD){
                        count++;
                        VIOROWS_THRESHOLD *= 1.2;
                        System.out.println("VIOROWS_THRESHOLD"+VIOROWS_THRESHOLD);
                    }
                    else return violationRows;
                }
            }
        }
        return violationRows;
    }
}

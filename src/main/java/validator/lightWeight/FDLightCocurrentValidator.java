package validator.lightWeight;

import dataStructures.DataFrame;
import dataStructures.fd.FDCandidate;
import dataStructures.fd.FDTreeNodeEquivalenceClasses;
import dataStructures.fd.FDValidationResult;
import dataStructures.fd.lightWeight.LightLeftNode;
import dataStructures.fd.lightWeight.MapNode;
import discoverer.multiple.bod_x.LightWeightThreadPool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class FDLightCocurrentValidator {
    private static ConcurrentHashMap<String, MapNode> fdMap = new ConcurrentHashMap<>();
    private static Integer attributeNum;
    public LightWeightThreadPool pool;
    public CopyOnWriteArrayList<FDCandidate> result = new CopyOnWriteArrayList<>();


    public Set<Integer> validate(CopyOnWriteArraySet<FDCandidate> set, DataFrame data) {
        CopyOnWriteArraySet<Integer> res =new CopyOnWriteArraySet<>();
        attributeNum = data.getColumnCount();
        change2Map(set);
        for(String key : fdMap.keySet()){
  //          System.out.println("验证右侧为" + key + "的FD");
            pool.TaskNum.getAndIncrement();
            pool.executor.execute(()-> {
                Queue<LightLeftNode> queue = new LinkedList<>();
                MapNode node = fdMap.get(key);
                //           System.out.println("把第一层加入queue");
                for(LightLeftNode fdNode : node.getLHSs()){
                    if(fdNode != null){
                        //              System.out.println("第一层结点: " + fdNode.attribute + "加入queue");
                        queue.add(fdNode);
                    }
                }
                while(!queue.isEmpty()){
                    LightLeftNode leftNode = queue.poll();
                    if(leftNode.status != LightLeftNode.Status.UNKNOWN) continue;
                    if(leftNode.parent != null){
                        leftNode.ecInDataSet = leftNode.parent.ecInDataSet.deepClone();
                    }else {
                        leftNode.ecInDataSet = new FDTreeNodeEquivalenceClasses();
                    }
                    leftNode.ecInDataSet.mergeLeftNode(leftNode.attribute, data);
                    FDTreeNodeEquivalenceClasses left = leftNode.ecInDataSet.deepClone();
                    FDValidationResult fdResult = left.checkFDRefinement(Integer.parseInt(key), data);
                    if(fdResult.status.equals("valid")){
                        //                  System.out.println("Valid");
                        leftNode.status = LightLeftNode.Status.VALID;
                        //剪枝
                        leftNode.nextLeft = null;
                    }else {
                        //                  System.out.println("Split");
                        leftNode.status = LightLeftNode.Status.SPLIT;
                        if (leftNode.nextLeft != null) {
                            for (LightLeftNode child : leftNode.nextLeft) {
                                if (child != null) {
                                    //                         System.out.println("child: " + child);
                                    queue.add(child);
                                }
                            }
                            if (isLeftNodeLast(leftNode))
                                res.addAll(fdResult.violationRows);
                        }
                    }
                }
            });
        }
        deleteSplit();
        return res;
    }

    public ConcurrentHashMap<String, MapNode> Set2Map(CopyOnWriteArraySet<FDCandidate> set,DataFrame data){
        CopyOnWriteArraySet<Integer> res =new CopyOnWriteArraySet<>();
        attributeNum = data.getColumnCount();
        change2Map(set);
        return fdMap;
    }

    public List<Integer> validatebyKey(String key,DataFrame data){
        Queue<LightLeftNode> queue = new LinkedList<>();
        MapNode node = fdMap.get(key);
        for(LightLeftNode fdNode : node.getLHSs()){
            if(fdNode != null){
                //              System.out.println("第一层结点: " + fdNode.attribute + "加入queue");
                queue.add(fdNode);
            }
        }
        while(!queue.isEmpty()){
            LightLeftNode leftNode = queue.poll();
            if(leftNode.status != LightLeftNode.Status.UNKNOWN) continue;
            if(leftNode.parent != null){
                leftNode.ecInDataSet = leftNode.parent.ecInDataSet.deepClone();
            }else {
                leftNode.ecInDataSet = new FDTreeNodeEquivalenceClasses();
            }
            leftNode.ecInDataSet.mergeLeftNode(leftNode.attribute, data);
            FDTreeNodeEquivalenceClasses left = leftNode.ecInDataSet.deepClone();
            FDValidationResult fdResult = left.checkFDRefinement(Integer.parseInt(key), data);
            if(fdResult.status.equals("valid")){
                //                  System.out.println("Valid");
                leftNode.status = LightLeftNode.Status.VALID;
                //剪枝
                leftNode.nextLeft = null;
            }else {
                //                  System.out.println("Split");
                leftNode.status = LightLeftNode.Status.SPLIT;
                if (leftNode.nextLeft != null) {
                    for (LightLeftNode child : leftNode.nextLeft) {
                        if (child != null) {
                            //                         System.out.println("child: " + child);
                            queue.add(child);
                        }
                    }
                    if (isLeftNodeLast(leftNode))
                        return fdResult.violationRows;
                }
            }
        }
        return new ArrayList<>();
    }

    public Boolean isLeftNodeLast(LightLeftNode node){
        for(LightLeftNode child : node.nextLeft){
            if(child != null) return false;
        }
        return true;
    }

    public List<FDCandidate> getMinimalFD(){
        for(String key : fdMap.keySet()){
            MapNode mapNode = fdMap.get(key);
            Queue<LightLeftNode> queue = new LinkedList<>();
            for(LightLeftNode child : mapNode.getLHSs()){
                if(child != null)
                    queue.add(child);
            }
            while(!queue.isEmpty()){
                LightLeftNode fdNode = queue.poll();
                if(fdNode.status== LightLeftNode.Status.VALID){
                    List<Integer> left = getLHS(fdNode);
                    int right =mapNode.getRHS();
                    if(!hasSubSet(left, right, result))
                        result.add(new FDCandidate(left,right));
                }
                else{
                    for (LightLeftNode fdNodeChild : fdNode.nextLeft){
                        if(fdNodeChild != null)
                            queue.add(fdNodeChild);
                    }
                }
            }
        }
        result.sort((fd1,fd2) -> fd1.left.size() - fd2.left.size());
        return result;
    }

    public List<Integer> getLHS(LightLeftNode node){
        List<Integer> left = new ArrayList<>();
        while(node != null){
            left.add(node.attribute);
            node = node.parent;
        }
        Collections.reverse(left);
        //System.out.println("LHS" + left);
        return left;
    }

    public boolean hasSubSet(List<Integer> left, int right, List<FDCandidate> fdCandidates){
        boolean result = false;
        for (FDCandidate fdCandidate : fdCandidates){
            if(fdCandidate.right == right) {
                result = left.containsAll(fdCandidate.left);
                if(result)  break;
            }
        }
        return result;
    }

    //将set中fd存入map
    public void change2Map(CopyOnWriteArraySet<FDCandidate> set){
        for(FDCandidate fdCandidate : set){
            String key = String.valueOf(fdCandidate.right);
            if(fdMap.containsKey(key)){
                insert2MapNode(fdMap.get(key), fdCandidate);
            }else {
                MapNode mapNode = new MapNode(fdCandidate.right, attributeNum);
                fdMap.put(key,mapNode);
                insert2MapNode(mapNode, fdCandidate);
            }
        }
    }

    //将fd插入mapNode
    private void insert2MapNode(MapNode mapNode, FDCandidate fdCandidate){
        List<Integer> left = fdCandidate.left;
        LightLeftNode[] LHSs = mapNode.getLHSs();

        //先处理mapNode
        Integer first = left.get(0);
        if(LHSs[first] == null){
            LHSs[first] = new LightLeftNode(first,attributeNum);
            LHSs[first].parent = null;
        }
        LightLeftNode node = LHSs[first];
        for(int i = 1; i < left.size(); i++){
            if(node.status == LightLeftNode.Status.VALID) break;
            Integer attribute = left.get(i);
            if(node.nextLeft[attribute] == null){
                node.nextLeft[attribute] = new LightLeftNode(attribute,attributeNum);
                node.nextLeft[attribute].parent = node;
            }
            node = node.nextLeft[attribute];
        }
    }
    public void deleteSplit(){
        for(String key : fdMap.keySet()){
            MapNode node = fdMap.get(key);
            for(LightLeftNode fdNode : node.getLHSs()){
                dfs(fdNode);
            }
        }
    }

    public void dfs(LightLeftNode node){
        if(node == null) return;
        if(node.nextLeft != null){
            for(LightLeftNode child : node.nextLeft){
                dfs(child);
            }
        }
        if(node.status == LightLeftNode.Status.SPLIT && node.nextLeft == null){
            node.parent.nextLeft[node.attribute] = null;
        }
    }
}
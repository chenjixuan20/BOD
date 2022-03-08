package dataStructures.fd.Array;

import dataStructures.fd.FDTreeNodeEquivalenceClasses;

import java.util.ArrayList;
import java.util.List;

public class FDTreeArray {
    private FDTreeNode root;

    public FDTreeNode getRoot(){
        return this.root;
    }

    public FDTreeArray(){
    }

    public FDTreeArray(int attributeNum){
        this.root = new FDTreeNode(attributeNum);
    }

    public class FDTreeNode{
        public int attribute;
        //需要check的右侧
//        public Boolean[] fdRHSCandidate;
        public FDTreeNode parent;
        //comfirmed记录上一层已经验证过的右侧 RHSCandidate[1] = 1表示parent.attribue->1有效,则（p.attr,attr)->1也有效，但是不是最小的。
        //这一层要遍历的就是RHSCandidate[i] = 0的i(rhs)
        public Boolean[] RHSCandidate;
        //是否是最小mininal[1]=1的话表示 attribute->1是最小fd
        public Boolean[] minimal;
        //该节点对应的fd在完整数据集中是否成立
//        public Boolean[] vaildInComplete;
        public List<FDTreeNode> children;
        public List<Integer> vaildRhsInTotal;
        public FDTreeNodeEquivalenceClasses ecInDataSet;

        public FDTreeNode(){}

        public FDTreeNode(FDTreeNode parent, int attribute, int attributeNum, Boolean[] anotherFatherRHSCandidate){
            this.parent = parent;
            this.attribute = attribute;
            //初始为父节点中lhs对应的有效rhs，对应A->B,则AX->B成立，不用去检查的剪枝策略
            //该节点初始化时利用了另一个父节点的confirmed
            this.RHSCandidate = RHSCandidateFromParent(parent.RHSCandidate, attributeNum);
            //合并另一个父节点的RHSCandidate
            if(anotherFatherRHSCandidate != null){
                for(int i = 0; i < anotherFatherRHSCandidate.length; i++){
                    if(anotherFatherRHSCandidate[i]){
                        RHSCandidate[i] = true;
                    }
                }
            }
            this.minimal = new Boolean[attributeNum];
//            this.vaildInComplete = new Boolean[attributeNum];
            for(int i = 0; i < attributeNum; i++){
                minimal[i] = false;
//                vaildInComplete[i] = false;
            }
            this.children = new ArrayList<>();
            this.vaildRhsInTotal = new ArrayList<>();
        }

        public FDTreeNode(int attributeNum){
            this.parent = null;
            this.attribute = -1;
            this.RHSCandidate = new Boolean[attributeNum];
            for(int i = 0; i < attributeNum; i++){
                this.RHSCandidate[i] = false;
            }
            this.minimal = new Boolean[attributeNum];
//            this.vaildInComplete = new Boolean[attributeNum];
            this.children = new ArrayList<>();
            this.vaildRhsInTotal = new ArrayList<>();
        }

        //clone父节点的comfirmed，作为当前节点的初始cimfirmed
        public Boolean[] RHSCandidateFromParent(Boolean[] parent_RHS, int attributeNum){
            Boolean[] RHSCandidate =  new Boolean[attributeNum];
            for(int i = 0; i < attributeNum; i++){
              RHSCandidate[i] = parent_RHS[i];
            }
            return RHSCandidate;
        }

        public Boolean[] comfirmedDeepClone(Boolean[] array, int attributeNum){
            Boolean[] fdRight =  new Boolean[attributeNum];
            for(int i = 0; i < array.length; i++){
                fdRight[i] = array[i];
            }
            return fdRight;
        }

        @Override
        public String toString() {
            return "attribute: " + (attribute);
        }
    }

}

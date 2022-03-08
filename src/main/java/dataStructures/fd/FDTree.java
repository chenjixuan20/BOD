package dataStructures.fd;

import java.util.*;

public class FDTree {
    private FDTreeNode root;

    public FDTreeNode getRoot(){
        return this.root;
    }

    public FDTree(){
    }

    public FDTree(int attributeNum){
        this.root = new FDTreeNode(attributeNum);
    }

    public class FDTreeNode{
        public int attribute;
        //需要遍历的右侧
        public BitSet fdRHSCandidate;
//        public FDTreeNode[] children;
        public FDTreeNode parent;
        //comfirmed记录上一层已经验证过的右侧 comfirmed[1] = 1表示parent.attribue->1有效,则（p.attr,attr)->1也有效，但是不是最小的。
        //这一层要遍历的就是comfirmed[i] = 0的i(rhs)
        public BitSet confirmed;
        //是否是最小mininal[1]=1的话表示 attribute->1是最小fd
        public BitSet mininal;

        public FDTreeNode(){}

        public FDTreeNode(FDTreeNode parent, int attribute, int attributeNum){
            this.parent = parent;
            this.attribute = attribute;
            //该节点的lhs对应的rhs，默认全为true，checkFD()时再把non-valid改为false
            this.fdRHSCandidate = new BitSet(attributeNum);
            for(int i = 0; i < attributeNum; i++){
                fdRHSCandidate.set(i);
            }
            //初始为父节点中lhs对应的有效rhs，对应A->B,则AX->B成立，不用去检查的剪枝策略
            this.confirmed = comfirmedFromParent(parent.confirmed, attributeNum);
//            this.children = new FDTreeNode[attributeNum - attribute - 1];
            this.mininal = new BitSet(attributeNum);
        }

        public FDTreeNode(int attributeNum){
            this.parent = null;
            this.attribute = -1;
            //size为attributeNum,值全为0
            this.fdRHSCandidate = new BitSet(attributeNum);
//            this.children = new FDTreeNode[attributeNum];
            this.confirmed = new BitSet(attributeNum);
            this.mininal = new BitSet(attributeNum);
        }

        @Override
        public String toString() {
            return "FDTreeNode{" +
                    "attribute = " + attribute +
                    ", fdRight = " + fdRHSCandidate +
//                    ", children = " + children.toString() +
                    '}';
        }

        //clone父节点的comfirmed，作为当前节点的初始cimfirmed
        public BitSet comfirmedFromParent(BitSet parent_comfirmed, int attributeNum){
            BitSet comfirmed =  new BitSet(attributeNum);
            for(int i = 0; i < attributeNum; i++){
                if(parent_comfirmed.get(i)){
                    comfirmed.set(i);
                }
            }
            return comfirmed;
        }

        public BitSet comfirmedDeepClone(BitSet bitSet, int attributeNum){
            BitSet fdRight =  new BitSet(attributeNum);
            fdRight = (BitSet)bitSet.clone();
            return fdRight;
        }

    }

}

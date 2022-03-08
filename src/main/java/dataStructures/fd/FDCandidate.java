package dataStructures.fd;

import dataStructures.fd.Array.FDTreeArray;

import java.util.ArrayList;
import java.util.List;

public class FDCandidate {
    public List<Integer> left;
    public int right;
    public FDTreeArray.FDTreeNode fdTreeNode;

    public FDCandidate(List<Integer> left, int right){
        this.left = left;
        this.right = right;
    }

    public FDCandidate(List<Integer> left, int right, FDTreeArray.FDTreeNode fdTreeNode){
        this.left = left;
        this.right = right;
        this.fdTreeNode = fdTreeNode;
    }

    public Boolean isEqual(FDCandidate fdCandidate){
        boolean result = false;
        if(this.left.equals(fdCandidate.left) && this.right == fdCandidate.right){
            result = true;
        }
        return result;
    }

    @Override
    public String toString() {
        List<Integer> outleft = new ArrayList<>();
        for(Integer x : left){
            outleft.add(x+1);
        }
        return  outleft + "->" + (right+1);
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof FDCandidate)) return false;
        FDCandidate that = (FDCandidate) o;
        return this.left.equals(that.left) && this.right == that.right;
    }
    @Override
    public int hashCode(){ return this.left.hashCode();}
}

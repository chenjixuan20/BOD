package dataStructures.od;

import dataStructures.od.ODTree;

import java.util.ArrayList;
import java.util.List;

public class ODValidationResult {
    public ODTree.ODTreeNodeStatus status = ODTree.ODTreeNodeStatus.UNKNOWN;
    public List<Integer> violationRows;

    public ODValidationResult(){
        violationRows = new ArrayList<>();
    }

    public ODValidationResult(ODTree.ODTreeNodeStatus status){
        this();
        this.status = status;
    }
}

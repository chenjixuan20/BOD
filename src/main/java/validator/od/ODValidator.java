package validator.od;

import dataStructures.DataFrame;
import dataStructures.od.ODTree;

import java.util.Set;

public abstract class ODValidator {
    public abstract Set<Integer> validate(ODTree tree, DataFrame data);
}

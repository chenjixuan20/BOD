package dataStructures.od;

import java.util.ArrayList;
import java.util.List;

public class LeftAndRightAttributeList {
    public List<AttributeAndDirection> left;
    public List<AttributeAndDirection> right;

    public LeftAndRightAttributeList(List<AttributeAndDirection> left, List<AttributeAndDirection> right){
        this.left = left;
        this.right = right;
    }

    public LeftAndRightAttributeList() {
        left = new ArrayList<>();
        right = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        outAttributeListNoBracket(left, sb);
        sb.append(ODTree.OUTPUT_DELIMETER);
        outAttributeListNoBracket(right, sb);
        return sb.toString();
    }

    private static void outAttributeListNoBracket(List<AttributeAndDirection> list, StringBuilder sb){
        for (int i = 0; i < list.size(); i++) {
            if(i > 0)
                sb.append(",");
            sb.append(list.get(i));
        }
    }

}

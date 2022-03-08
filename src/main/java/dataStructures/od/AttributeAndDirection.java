package dataStructures.od;

import java.util.ArrayList;
import java.util.List;

public final class AttributeAndDirection {
    public static final int UP = 1;
    public static final int DOWN = -1;
    public final int attribute;
    public final int direction;
    public static int maxAttribute = -1;
    public static List<AttributeAndDirection> ups = new ArrayList<>();
    public static List<AttributeAndDirection> downs = new ArrayList<>();


    private AttributeAndDirection(int attribute, int direction) {
        this.attribute = attribute;
        this.direction = direction;
    }

    public static AttributeAndDirection getInstance(int attribute, int direction){
        while(maxAttribute < attribute){
            maxAttribute++;
            ups.add(new AttributeAndDirection(maxAttribute, UP));
            downs.add(new AttributeAndDirection(maxAttribute, DOWN));
        }
        if(direction == UP){
            return ups.get(attribute);
        }else {
            return downs.get(attribute);
        }
    }

    @Override
    public String toString() {
        //输出的时候使attribute从1开始
        return (attribute+1)+(direction == UP?"↑":"↓");
    }
}

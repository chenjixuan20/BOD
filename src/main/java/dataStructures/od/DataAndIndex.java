package dataStructures.od;

public class DataAndIndex {
    public int data;
    public int index;

    public DataAndIndex(int data, int index){
        this.data = data;
        this.index = index;
    }

    @Override
    public String toString() {
        return "DataAndIndex{" +
                "data = " + data +
                ", index = " + index +
                '}';
    }
}

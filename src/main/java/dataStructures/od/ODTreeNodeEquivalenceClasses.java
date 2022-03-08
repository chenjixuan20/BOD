package dataStructures.od;

import dataStructures.DataFrame;
import dataStructures.EquivalenceClass;
import util.Timer;
import java.util.*;

public class ODTreeNodeEquivalenceClasses {
    public EquivalenceClass left;
    public EquivalenceClass right;
    public static long mergeTime = 0;
    public static long checkTime=0;
    public static long cloneTime=0;

    public ODTreeNodeEquivalenceClasses() {
        left=new EquivalenceClass();
        right=new EquivalenceClass();
    }

    public ODTreeNodeEquivalenceClasses(EquivalenceClass left, EquivalenceClass right){
        this.left = left;
        this.right = right;
    }

    /**
     * 得到列索引在right中的那一个cluster中
     * @return
     */
    public int[] getRowToRightClusterIndex(){
        int[] rowToRightClusterIndex = new int[right.indexes.length];
        int rightClusterIndex = -1;
        int clusterBeignsPointer = 0;
        for(int i = 0; i < right.indexes.length; i++){
            if(i == right.clusterBegins.get(clusterBeignsPointer)){
                rightClusterIndex++;
                clusterBeignsPointer++;
            }
            rowToRightClusterIndex[right.indexes[i]] = rightClusterIndex;
        }
        return rowToRightClusterIndex;
    }

    /**
     * check()函数，来验证ODCandidate是否是成立
     * 已经使用rowToRightClusterIndex数组记录row在right的哪一个cluster中
     * 在left中，如果一个cluster中max != min，则SPLIT
     * 如果min < maxLaste，则SWAP
     * @param data
     * @return
     */
    public ODValidationResult check(DataFrame data){
        Timer timer = new Timer();
        ODValidationResult result = new ODValidationResult();
        result.status = ODTree.ODTreeNodeStatus.VALID;

        if(!left.initialized() || !right.initialized())
            return result;

        int[] rowToRightClusterIndex = getRowToRightClusterIndex();
        int max = 0, min = 0, maxValue = 0, minValue = 0;
        int maxLast = 0, maxLastValue = 0;
        for(int beginPointer = 0; beginPointer < left.clusterBegins.size() - 1; beginPointer++){
            /*
             left中的每个cluster的开头和结尾的position
             */
            int groupBegin = left.clusterBegins.get(beginPointer);
            int groupEnd = left.clusterBegins.get(beginPointer + 1);
            //max和min是元组
            max = min = left.indexes[groupBegin];
            //maxValue和minValue是元组在rhs的clusterIndex
            maxValue = minValue = rowToRightClusterIndex[max];

            for(int i = groupBegin + 1; i < groupEnd; i++){
                int index = left.indexes[i];
                int value = rowToRightClusterIndex[index];
                if(value < minValue){
                    minValue = value;
                    min = index;
                }
                if(value > maxValue){
                    maxValue = value;
                    max = index;
                }
            }

            //在valid情况下，找到一个split违约，将s,t两个元组（s,t在右侧clusterIndex不同）加入violationRows
            if(result.status == ODTree.ODTreeNodeStatus.VALID && min != max){
                result.status = ODTree.ODTreeNodeStatus.SPLIT;
                result.violationRows.add(min);
                result.violationRows.add(max);
            }
            if(beginPointer >= 1 && maxLastValue > minValue){
                result.status = ODTree.ODTreeNodeStatus.SWAP;
                result.violationRows.clear();
                result.violationRows.add(min);
                result.violationRows.add(maxLast);
                break;
            }
            maxLast = max;
            maxLastValue = maxValue;
        }
        checkTime += timer.getTimeUsed();
        return result;
    }

    /**
     * 新结点根据规则加入了left或者right中，生产了新的ODCandidate
     * 把新结点node，根据规则和数据，合并到left或者right的等价类中，得到新的ODCandidate的EquivalenceClass
     * @param node
     * @param data
     * @return
     */
    public ODTreeNodeEquivalenceClasses mergeNode(ODTree.ODTreeNode node, DataFrame data){
        Timer mergeTimer =new Timer();
        AttributeAndDirection attribute = node.attribute;
        if(node.parent.status == ODTree.ODTreeNodeStatus.VALID)
            right.merge(data, attribute);
        else
            left.merge(data, attribute);
        mergeTime += mergeTimer.getTimeUsed();
        return this;
    }


    public ODTreeNodeEquivalenceClasses findAndHandleEquivalenceClass(ODTree.ODTreeNode node,HashMap<String, EquivalenceClass> map,
                                              List<AttributeAndDirection> list, DataFrame data){
        List<Integer> attributeList = new ArrayList<>();
        List<Integer> attributeListClone = new ArrayList<>();
        List<Integer> directionList = new ArrayList<>();
        List<List<Integer>> trueEc = new ArrayList<>();

        for (AttributeAndDirection attributeAndDirection : list) {
            attributeList.add(attributeAndDirection.attribute);
            directionList.add(attributeAndDirection.direction);
            attributeListClone.add(attributeAndDirection.attribute);
        }
        /*
        当attributeList的顺序和方向变化时，EquivalueClass中每个cluster的内容不变，只有cluster的顺序发生变化
        FD中的attributeList是按照升序排序的，要找到OD中的AttributeAndDirection对应的EquivalueClass(FD中计算过)
        则先需要对AttributeAndDirection进行升序排序，得到cluster次序错乱的EquivalueClass
        再按照AttributeAndDirection中的属性顺序和方向进行还原
        */
        Collections.sort(attributeListClone);
        if(map.get(attributeListClone.toString()) == null){
            return mergeNode(node, data);
        }else{
            EquivalenceClass ec = map.get(attributeListClone.toString());
            int[] indexs = ec.indexes;
            List<Integer> begin = ec.clusterBegins;
            for(int i = 0; i < begin.size() - 1; i++){
                List<Integer> cluster = new ArrayList<>();
                int groupBegin = begin.get(i);
                int groupEnd = begin.get(i + 1);
                for(int j = groupBegin; j < groupEnd; j++){
                    cluster.add(indexs[j]);
                }
                trueEc.add(cluster);
            }
            trueEc.sort(new Comparator<List<Integer>>() {
                @Override
                public int compare(List<Integer> o1, List<Integer> o2) {
                    int sortAttIndex = 0;
                    int sortAtt = attributeList.get(sortAttIndex);
                    int a = data.getCell(o1.get(0), sortAtt);
                    int b = data.getCell(o2.get(0), sortAtt);
                    if (a != b) {
                        if (directionList.get(sortAttIndex) == 1)
                            return a - b;
                        else return b - a;
                    } else {
                        while (a == b && sortAttIndex < attributeList.size()) {
                            sortAttIndex++;
                            sortAtt = attributeList.get(sortAttIndex);
                            a = data.getCell(o1.get(0), sortAtt);
                            b = data.getCell(o2.get(0), sortAtt);
                        }
                        if (directionList.get(sortAttIndex) == 1)
                            return a - b;
                        else return b - a;
                    }
                }
            });
            int rowCount = data.getRowCount();
            int[] newIndex = new int[rowCount];
            List<Integer> newBegin = new ArrayList<>();
            newBegin.add(0);
            int count = 0;
            int beginPoint = 0;
            for (List<Integer> cluster : trueEc) {
                int clusterSize = cluster.size();
                beginPoint += clusterSize;
                newBegin.add(beginPoint);
                for (Integer integer : cluster) {
                    newIndex[count] = integer;
                    count++;
                }
            }
            if(node.parent.status == ODTree.ODTreeNodeStatus.VALID){
                right.indexes = newIndex;
                right.clusterBegins = newBegin;
            }else {
                left.indexes = newIndex;
                left.clusterBegins = newBegin;
            }
            return this;
        }
    }

    /**
     * 测试用
     * @param map
     * @param list
     * @param data
     */
    public ODTreeNodeEquivalenceClasses findAndHandleEquivalenceClass(HashMap<List<Integer>, EquivalenceClass> map,
                                               List<AttributeAndDirection> list, DataFrame data){
//        System.out.println("List: " + list);
        List<Integer> attributeList = new ArrayList<>();
        List<Integer> attributeListClone = new ArrayList<>();
        List<Integer> directionList = new ArrayList<>();
        List<List<Integer>> trueEc = new ArrayList<>();

        for(int i = 0; i < list.size(); i++){
            int att = list.get(i).attribute;
            attributeList.add(att);
            attributeListClone.add(att);
            directionList.add(list.get(i).direction);
        }
//        System.out.println("attributeList: " + attributeList);
//        System.out.println("directionList: " + directionList);
        /*
        当attributeList的顺序和方向变化时，EquivalueClass中每个cluster的内容不变，只有cluster的顺序发生变化
        FD中的attributeList是按照升序排序的，要找到OD中的AttributeAndDirection对应的EquivalueClass(FD中计算过)
        则先需要对AttributeAndDirection进行升序排序，得到cluster次序错乱的EquivalueClass
        再按照AttributeAndDirection中的属性顺序和方向进行还原
        */
        Collections.sort(attributeListClone);
//        System.out.println("attributeListClone: " + attributeListClone);
        EquivalenceClass ec = map.get(attributeListClone);
//        System.out.println("FD中对应的EquivalenceClass: " + ec);

        int[] indexs = ec.indexes;
        List<Integer> begin = ec.clusterBegins;
        //得到List<List<>>形式的EquivalidClass
        for(int i = 0; i < begin.size() - 1; i++){
            List<Integer> cluster = new ArrayList<>();
            int groupBegin = begin.get(i);
            int groupEnd = begin.get(i + 1);
            for(int j = groupBegin; j < groupEnd; j++){
                cluster.add(indexs[j]);
            }
            trueEc.add(cluster);
        }
        //将List<List<>>形式的EquivalidClass按照OD中的要求排序
        //attributeList，directionList记录OD中的要求
        Collections.sort(trueEc, new Comparator<List<Integer>>() {
            @Override
            public int compare(List<Integer> o1, List<Integer> o2) {
                int sortAttIndex = 0;
                int sortAtt = attributeList.get(sortAttIndex);
                int a = data.getCell(o1.get(0), sortAtt);
                int b = data.getCell(o2.get(0), sortAtt);
                if(a != b) {
                    if(directionList.get(sortAttIndex) == 1)
                        return a - b;
                    else return b - a;
                }else{
                    while (a == b && sortAttIndex < attributeList.size()){
                        sortAttIndex++;
                        sortAtt = attributeList.get(sortAttIndex);
                        a = data.getCell(o1.get(0), sortAtt);
                        b = data.getCell(o2.get(0), sortAtt);
                    }
                    if(directionList.get(sortAttIndex) == 1)
                        return a - b;
                    else return b - a;
                }
            }
        });
//        System.out.println("排序后的等价类： " + trueEc);
        /*
        将List<List<>>还原成index begin形式
         */
        int rowCount = data.getRowCount();
        int[] newIndex = new int[rowCount];
        List<Integer> newBegin = new ArrayList<>();
        newBegin.add(0);
        int count = 0;
        int beginPoint = 0;
        for(int i = 0; i < trueEc.size(); i++){
            List<Integer> cluster = trueEc.get(i);
            int clusterSize = cluster.size();
            beginPoint += clusterSize;
            newBegin.add(beginPoint);
            for(int j = 0; j < clusterSize; j++){
                newIndex[count] = cluster.get(j);
                count++;
            }
        }
        left.clusterBegins = newBegin;
        left.indexes = newIndex;
        return this;
    }


    public ODTreeNodeEquivalenceClasses mergeNodeTest(AttributeAndDirection attribute, DataFrame data, ODTree.ODTreeNodeStatus status){
        if(status == ODTree.ODTreeNodeStatus.VALID){
            right.merge(data, attribute);
        }else {
            left.merge(data, attribute);
        }
        return this;
    }

    @Override
    public String toString() {
        return "ODTreeNodeEquivalenceClasses{" +
                "leftList=" + left +
                ", rightList=" + right +
                '}';
    }

    public ODTreeNodeEquivalenceClasses deepClone(){
        Timer timer = new Timer();
        ODTreeNodeEquivalenceClasses result = new ODTreeNodeEquivalenceClasses(left.deepClone(), right.deepClone());
        cloneTime += timer.getTimeUsed();
        return result;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof ODTreeNodeEquivalenceClasses)) return false;
        ODTreeNodeEquivalenceClasses that = (ODTreeNodeEquivalenceClasses) o;
        return this.left.equals(that.left) && this.right.equals(that.right);
    }

}

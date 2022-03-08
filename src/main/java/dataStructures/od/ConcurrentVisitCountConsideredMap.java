package dataStructures.od;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentVisitCountConsideredMap<K,V> {

    private ConcurrentHashMap<K,Integer> remainingVisitCount=new ConcurrentHashMap<>();
    private ConcurrentHashMap<K,V> map=new ConcurrentHashMap<>();

    public ConcurrentVisitCountConsideredMap() {
    }

    public void addVisitCount(K key,int addCount) {
        if (key != null) {
            if (addCount != 0) {
                //getOrDefault() 方法获取指定 key 对应对 value，如果找不到 key ，则返回设置的默认值。
                int count = remainingVisitCount.getOrDefault(key, 0) + addCount;
                if (count > 0) {
                    remainingVisitCount.put(key, count);
                } else {
                    map.remove(key);
                    remainingVisitCount.remove(key);
                }
            }
        }
    }
    public boolean mayPut(K key){
        return remainingVisitCount.containsKey(key);
    }

    public boolean containKey(K key){
        return map.containsKey(key);
    }

    public void addVisitCount(K key){
        addVisitCount(key,1);
    }

    public void put(K key,V value){
        int count=remainingVisitCount.getOrDefault(key,0);
        if(count>0){
            map.put(key,value);
        }
    }

    public void put(K key,V value,int addVisitCount){
        addVisitCount(key,addVisitCount);
        put(key,value);
    }

    public V get(K key){
        while(!map.containsKey(key)){
//            System.out.println("key:"+key+" "+"count:"+remainingVisitCount.get(key));
//            System.out.println("\n"+"map:"+map.toString()+"\n");

//            ODTree.ODTreeNode node = (ODTree.ODTreeNode) key;
//            node = node.getRoot();
//            key = (K)node;
        }
        int count=remainingVisitCount.getOrDefault(key,0);
        V result=map.get(key);
        if(count==1){
            map.remove(key);
            remainingVisitCount.remove(key);
        }else {
            remainingVisitCount.put(key,count-1);
        }
        return result;
    }

    public ConcurrentHashMap<K, V> getMap() {
        return map;
    }

    public ConcurrentHashMap<K, Integer> getRemainingVisitCount() {
        return remainingVisitCount;
    }
}

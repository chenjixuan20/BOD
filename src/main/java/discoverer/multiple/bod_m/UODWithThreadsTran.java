package discoverer.multiple.bod_m;

import dataStructures.od.ODCandidate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class UODWithThreadsTran {
    public CopyOnWriteArrayList<CopyOnWriteArrayList<ODCandidate>> layerTask = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<ODCandidate> task = new CopyOnWriteArrayList<>();
    public AtomicBoolean status = new AtomicBoolean(false);
    public UODWithThreadsPool pool;
    public UODWithThreadsConsumer consumer;
    public UODWithThreadsProducer producer;
    public void pushList(List<ODCandidate> ods){
        CopyOnWriteArrayList<ODCandidate> copy = new CopyOnWriteArrayList<>(ods);
        layerTask.add(copy);
        System.out.println("push:"+copy.size());
        if(pool.executor.getActiveCount() == 0 && pool.executor.getQueue().isEmpty()){
            LockSupport.unpark(this.consumer);
        }
    }
    public List<ODCandidate> popList(){
        while(layerTask.isEmpty()||consumer.discover.isInterrup()){
            //等待消费
            if(consumer.discover.isDiscoverDone())  LockSupport.unpark(this.producer);
            System.out.println("park consumer");
            LockSupport.park(this.consumer);
        }
        task = new CopyOnWriteArrayList<>(layerTask.remove(0));
        System.out.println("layerod:"+task);
        return task;
    }
}

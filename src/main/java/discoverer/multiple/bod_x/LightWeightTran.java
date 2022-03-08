package discoverer.multiple.bod_x;

import dataStructures.od.ODCandidate;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class LightWeightTran {
    public CopyOnWriteArrayList<CopyOnWriteArrayList<ODCandidate>> layerTask = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<ODCandidate> task = new CopyOnWriteArrayList<>();
    public AtomicBoolean status = new AtomicBoolean(false);
    public LightWeightThreadPool pool;
    public LightWeightConsumer consumer;
    public LightWeightProducer producer;
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
            if(consumer.discover.isDiscoverDone()){
                if(consumer.discover.fdCandidates.size()>0) {
                    System.out.println("--fdvalidating--");
                    System.out.println("fdNum:" + consumer.discover.fdCandidates.size());
                    pool.FDValidate(consumer.discover.fdCandidates);
                }
                else{
                    LockSupport.unpark(producer);
                }
            }
            System.out.println("park consumer");
            LockSupport.park(this.consumer);
        }
        task = new CopyOnWriteArrayList<>(pool.odvalidator.chooseODs(layerTask.remove(0)));
        return task;
    }

}

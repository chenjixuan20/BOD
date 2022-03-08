package discoverer.multiple.bod_x;

import dataStructures.DataFrame;
import dataStructures.fd.FDCandidate;
import dataStructures.fd.lightWeight.MapNode;
import dataStructures.od.ODCandidate;
import util.Timer;
import validator.lightWeight.FDLightCocurrentValidator;
import validator.od.ODConcurrentValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class LightWeightThreadPool {
    public ThreadPoolExecutor executor;
    public LightWeightDiscoverer discover;
    public ODConcurrentValidator odvalidator;
    public FDLightCocurrentValidator fdvalidator;
    public LightWeightTran tran;
    public AtomicInteger TaskNum = new AtomicInteger(0);
    public boolean isFDEnd = false;
    public DataFrame data;
    public Set<Integer> vioRows = new HashSet<>();
    public static final int PoolSize = 2;
    public Timer timer = new Timer();
    public LightWeightThreadPool(LightWeightDiscoverer discover, DataFrame data){
        this.executor = new ThreadPoolExecutor(PoolSize,PoolSize,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()){
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                //setActiveNum(activeNum-1);
//                System.out.println("线程结束，剩余线程数："+executor.getActiveCount());
            }
        };
        this.data = data;
        this.fdvalidator = new FDLightCocurrentValidator();
        this.odvalidator = new ODConcurrentValidator();
        this.discover = discover;
        this.fdvalidator.pool = this;
    }
    public void ODValidate(List<ODCandidate> odCandidates){
        timer.reset();
        this.executor.execute(()-> {
            pushVioRows(odvalidator.validatebyods(odCandidates, data, vioRows.isEmpty(),tran.producer.cutOD));
            if (vioRows.size() > 20) {
                discover.setInterrup(true);

            }

            if (TaskNum.get() == executor.getCompletedTaskCount() + 1) {
                System.out.println("od验证finish数量为：" + TaskNum);
                System.out.println("OD验证后，vioRows:" + vioRows.size());
                System.out.println("ODValidateTime： " + timer.getTimeUsed() / 1000.0 + "s");
                System.out.println("unpark consumer");
                LockSupport.unpark(tran.consumer);
            }
        }
        );
    }

    public void ODValidateOne(ODCandidate od){
        this.executor.execute(()-> {
                    if(vioRows.size() > 30){
                        discover.setInterrup(true);
                    }
                    if(TaskNum.get() == executor.getCompletedTaskCount()+1){
                        System.out.println("od验证finish数量为："+TaskNum);
                        System.out.println("OD验证后，vioRows:"+vioRows.size());
                        System.out.println("ODValidateTime： " + timer.getTimeUsed() /1000.0 + "s");
                        LockSupport.unpark(tran.consumer);
                    }
                }
        );
    }

    public void FDValidate(CopyOnWriteArraySet<FDCandidate> set) {
        ConcurrentHashMap<String, MapNode> fdMap = fdvalidator.Set2Map(set, data);
        for (String key : fdMap.keySet()) {
            //          System.out.println("验证右侧为" + key + "的FD");
            TaskNum.getAndIncrement();
            executor.execute(() -> {
                vioRows.addAll(fdvalidator.validatebyKey(key, data));
                if (TaskNum.get() == executor.getCompletedTaskCount() + 1)
                    LockSupport.unpark(tran.producer);
            });

        }
    }
    public void reStart(){
        this.TaskNum.set(0);
        this.executor = new ThreadPoolExecutor(PoolSize,PoolSize,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }
    public void pushVioRows(Set<Integer> vioRows){
        this.vioRows.addAll(vioRows);
    }
    public boolean isFDEnd() {
        return isFDEnd;
    }

    public void setFDEnd(boolean FDEnd) {
        isFDEnd = FDEnd;
    }
}

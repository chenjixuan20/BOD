package discoverer.multiple.bod_m;

import dataStructures.DataFrame;
import dataStructures.od.ODCandidate;
import util.Timer;
import validator.od.ODConcurrentValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class UODWithThreadsPool {
    public ThreadPoolExecutor executor;
    public UODWithThreadsDiscover discover;
    public ODConcurrentValidator odvalidator;
    public UODWithThreadsTran tran;
    public AtomicInteger TaskNum = new AtomicInteger(0);
    public DataFrame data;
    public Set<Integer> vioRows = new HashSet<>();
    public static final int PoolSize = 4;
    public Timer timer = new Timer();
    public UODWithThreadsPool(UODWithThreadsDiscover discover,DataFrame data){
        this.executor = new ThreadPoolExecutor(PoolSize,PoolSize,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()){
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                //setActiveNum(activeNum-1);
//                System.out.println("线程结束，剩余线程数："+executor.getActiveCount());
            }
        };
        this.data = data;
        this.odvalidator = new ODConcurrentValidator();
        this.discover = discover;
    }
    public void ODValidate(List<ODCandidate> odCandidates){
        timer.reset();
        this.executor.execute(()-> {
            pushVioRows(odvalidator.validatebyods(odCandidates, data, vioRows.isEmpty(),tran.producer.cutOD));


                    if (TaskNum.get() == executor.getCompletedTaskCount() + 1) {
                        System.out.println("od验证finish数量为：" + TaskNum);
                        System.out.println("OD验证后，vioRows:" + vioRows.size());
                        System.out.println("ODValidateTime： " + timer.getTimeUsed() / 1000.0 + "s");
                        if (vioRows.size() > 10) {
                            discover.setInterrup(true);
                            System.out.println("unlock producer");
                            LockSupport.unpark(tran.producer);
                        }
                        else {
                            System.out.println("unpark consumer");
                            LockSupport.unpark(tran.consumer);
                        }
                    }
                }
        );
    }


    public void reStart(){
        this.TaskNum.set(0);
        this.executor = new ThreadPoolExecutor(PoolSize,PoolSize,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }
    public void pushVioRows(Set<Integer> vioRows){
        this.vioRows.addAll(vioRows);
    }

}

package discoverer.multiple.bod_x;

import dataStructures.od.ODCandidate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class LightWeightConsumer extends Thread{
    public LightWeightThreadPool pool;
    public LightWeightDiscoverer discover;
    public LightWeightTran tran;
    public AtomicBoolean status = new AtomicBoolean(false);
    public List<ODCandidate> odCandidateList;
    public List<ODCandidate> sublist;
    public int size=0;

    public LightWeightConsumer(LightWeightTran tran, LightWeightDiscoverer discover, LightWeightThreadPool pool){
        this.pool = pool;
        this.tran = tran;
        this.discover = discover;
        odCandidateList = new ArrayList<>();
    }

    @Override
    public void run(){
        while(true){
            odCandidateList = tran.popList();
            if (!odCandidateList.isEmpty()) {
//                if(pool.executor.isShutdown()) pool.reStart();
                if(odCandidateList.size() > pool.executor.getMaximumPoolSize()) {
                    pool.TaskNum.addAndGet(pool.executor.getMaximumPoolSize() - 1);
                    size = odCandidateList.size() / (pool.executor.getMaximumPoolSize() - 1);
                    for (int i = 0; i < (pool.executor.getMaximumPoolSize() - 1); i++) {
                        if(i == pool.executor.getMaximumPoolSize() - 2)
                            sublist = new ArrayList<>(odCandidateList.subList(i*size,odCandidateList.size()));
                        else
                            sublist = new ArrayList<>(odCandidateList.subList(i * size, (i + 1) * size));
                        pool.ODValidate(sublist);
                    }
                }
                else{
                    pool.TaskNum.getAndIncrement();
                    pool.ODValidate(odCandidateList);
                }
//                pool.TaskNum.getAndIncrement();
//                System.out.println(odCandidateList);
//                pool.ODValidate(odCandidateList);
                LockSupport.park(this);
            }
        }
    }
}

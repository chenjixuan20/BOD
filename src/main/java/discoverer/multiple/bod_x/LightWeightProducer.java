package discoverer.multiple.bod_x;

import dataStructures.DataFrame;
import dataStructures.PartialDataFrame;
import dataStructures.fd.FDCandidate;
import dataStructures.od.ODCandidate;
import dataStructures.od.ODTree;
import dataStructures.od.ODTreeNodeEquivalenceClasses;
import minimal.ODMinimalCheckTree;
import minimal.ODMinimalChecker;
import util.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

public class LightWeightProducer extends Thread {
    public DataFrame data;
    public PartialDataFrame sampleData;
    public ODTree odTree;
    public LightWeightThreadPool pool;
    public LightWeightDiscoverer discoverer;
    public ConcurrentHashMap<ODTree.ODTreeNode, ODTree.ODTreeNodeStatus> cutOD;
    private long totalDiscoverTime=0;
    private long totalValidateTime=0;
    private long totalProductTime=0;
    private long totalCloneTime=0;
    private long totalCheckTime=0;
    private long totalMinimalTime=0;

    public LightWeightProducer(LightWeightThreadPool pool, LightWeightDiscoverer discoverer, DataFrame data, PartialDataFrame sampleData){
        this.sampleData = sampleData;
        this.data = data;
        this.discoverer = discoverer;
        this.pool = pool;
        this.discoverer.pool = pool;
        this.cutOD = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        Timer timer=new Timer();
        Runtime r = Runtime.getRuntime();
        int round = 0;
        while (true) {
            round++;
            System.out.println("------");
            System.out.println("第" + round + "轮开始");
            Timer roundTimer = new Timer();
            int subRound = 0;
            while (true) {
                subRound++;
                System.out.println("\n第" + subRound + "次迭代");
                Timer subtimer = new Timer();
                odTree = discoverer.discover(sampleData, odTree);
                System.out.println("发现用时: " + subtimer.getTimeUsedAndReset() / 1000.0 + "s");
                System.out.println("OD数量: " + odTree.getAllOdsOrderByBFS().size());
                LockSupport.park(this);
                for(ODTree.ODTreeNode node : cutOD.keySet()){
                    node.status = cutOD.get(node);
                    node.concurrentCutChildren(cutOD);
                }
                totalValidateTime+=subtimer.getTimeUsed();
                System.out.println("检测用时: "+subtimer.getTimeUsedAndReset()/1000.0+"s");
                System.out.println("剩余OD数量: "+odTree.getAllOdsOrderByBFS().size());
                System.out.println("FD数量: " + discoverer.fdCandidates.size());

                System.out.println("FD:"+discoverer.fdCandidates);
                dealPartTime();
                if(pool.vioRows.size()==0){
                    if(!discoverer.isInterrup()){
                        pool.executor.shutdown();
                        discoverer.discoverExecutor.shutdown();
                        System.out.println("------");
                        System.out.println("第"+round+"轮结束");
                        System.out.println("本轮用时"+roundTimer.getTimeUsed()/1000+"s");
                        System.out.println("新数据集大小"+sampleData.getRowCount());
                        System.out.println("------");
                        System.out.println("-----------------------------------------------------");
                        System.out.println("最终统计");
                        System.out.println("用时"+timer.getTimeUsed()/1000.0+"s");
                        System.out.println("OD数量"+odTree.getAllOdsOrderByBFS().size());
                        System.out.println("OD:"+odTree.getAllOdsOrderByBFS());

                        List<FDCandidate> list = pool.fdvalidator.getMinimalFD();
                        System.out.println("FD数量"+list.size());
                        System.out.println("FD:"+list);

                        System.out.println("数据集大小"+sampleData.getRowCount());
                        System.out.println("discover时间:"+totalDiscoverTime/1000.0+"s");
                        System.out.println("validate时间:"+totalValidateTime/1000.0+"s");

                        System.out.println("check时间:"+totalCheckTime/1000.0+"s");
                        System.out.println("minimal检查时间:"+totalMinimalTime/1000.0+"s");
                        System.out.println("product时间:"+totalProductTime/1000.0+"s");
                        System.out.println("clone时间:"+totalCloneTime/1000.0+"s");

                        long startM = r.freeMemory();
                        long endM = r.totalMemory();
                        System.out.println("MemoryCost: " + String.valueOf((endM-startM)/1024/1024) + "MB");



                        List<Integer> rows =new ArrayList<>(sampleData.getRealIndexes());
                        rows.sort((Integer a, Integer b)->(a-b));
                        System.out.println(rows);
                        System.out.println("-----------------------------------------------------");
                        return;
                    }else {
                        System.out.println("violateRow size is 0 && discoverer is not complete");
                        pool.reStart();
                        if (odTree.getAllOdsOrderByDFS().size()>10000000){
                            for (ODCandidate od : odTree.getAllOdsOrderByBFS()) {
                                System.out.println("发现od超过10000000");
//                                System.out.println(od);
                            }
                        }
                    }
                }
                else {
                    sampleData.addRows(pool.vioRows);
                    pool.vioRows.clear();
                    pool.reStart();
                    pool.tran.layerTask.clear();
                    cutOD.clear();
//                    pool.odvalidator.Init();
                    System.out.println("------");
                    System.out.println("第"+round+"轮结束");
                    System.out.println("本轮用时"+roundTimer.getTimeUsed()/1000.0+"s");
                    System.out.println("新数据集大小"+sampleData.getRowCount());
                    System.out.println("------");
                    break;
                }
            }
        }
    }
    void dealPartTime(){
        System.out.println("check时间 "+ ODTreeNodeEquivalenceClasses.checkTime/1000.0+"s");
        System.out.println("minimal检查时间 "+ ODMinimalCheckTree.odMinimalCheckTime/1000.0+"s");
        System.out.println("product时间 "+ODTreeNodeEquivalenceClasses.mergeTime/1000.0+"s");
        System.out.println("clone时间 "+ODTreeNodeEquivalenceClasses.cloneTime/1000.0+"s");
        totalCheckTime+=ODTreeNodeEquivalenceClasses.checkTime;
        totalMinimalTime+= ODMinimalCheckTree.odMinimalCheckTime;
        totalProductTime+=ODTreeNodeEquivalenceClasses.mergeTime;
        totalCloneTime+=ODTreeNodeEquivalenceClasses.cloneTime;
        ODTreeNodeEquivalenceClasses.checkTime=0;
        ODTreeNodeEquivalenceClasses.cloneTime=0;
        ODTreeNodeEquivalenceClasses.mergeTime=0;
        ODMinimalChecker.odMinimalCheckTime=0;
    }
}

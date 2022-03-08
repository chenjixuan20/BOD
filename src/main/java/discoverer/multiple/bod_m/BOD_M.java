package discoverer.multiple.bod_m;

import dataStructures.DataFrame;
import dataStructures.PartialDataFrame;
import sampler.OneLevelCheckingSampler;

public class BOD_M {
    public static void main(String[] args) {
        DataFrame data = DataFrame.fromCsv("Data/Exp1,Exp2-N&T/WP-20K-7.csv");
        OneLevelCheckingSampler sampler = new OneLevelCheckingSampler();
        PartialDataFrame sampleData = sampler.sample(data);
        System.out.println("抽样数据集大小：" + sampleData.getRowsCount());

        UODWithThreadsDiscover discoverer= new UODWithThreadsDiscover();
        UODWithThreadsPool pool= new UODWithThreadsPool(discoverer,data);
        UODWithThreadsTran tran = new UODWithThreadsTran();
        tran.pool = pool;
        pool.tran = tran;
        discoverer.tran = tran;
        UODWithThreadsProducer producer = new UODWithThreadsProducer(pool,discoverer,data,sampleData);
        UODWithThreadsConsumer consumer = new UODWithThreadsConsumer(tran,discoverer,pool);
        tran.consumer = consumer;
        tran.producer = producer;
        producer.start();
        consumer.start();

    }
}

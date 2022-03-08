package discoverer.multiple.bod_x;

import dataStructures.DataFrame;
import dataStructures.PartialDataFrame;
import sampler.OneLevelCheckingSampler;

public class BOD_X {
    public static void main(String[] args) {
        DataFrame data = DataFrame.fromCsv("Data/Exp1,Exp2-N&T/WP-20K-7.csv");
        OneLevelCheckingSampler sampler = new OneLevelCheckingSampler();
        PartialDataFrame sampleData = sampler.sample(data);
        System.out.println("抽样数据集大小：" + sampleData.getRowsCount());

        LightWeightDiscoverer discoverer= new LightWeightDiscoverer();
        LightWeightThreadPool pool= new LightWeightThreadPool(discoverer,data);
        LightWeightTran tran = new LightWeightTran();
        tran.pool = pool;
        pool.tran = tran;
        discoverer.tran = tran;
        LightWeightProducer producer = new LightWeightProducer(pool,discoverer,data,sampleData);
        LightWeightConsumer consumer = new LightWeightConsumer(tran,discoverer,pool);
        tran.consumer = consumer;
        tran.producer = producer;
        producer.start();
        consumer.start();

    }
}

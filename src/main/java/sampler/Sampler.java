package sampler;

import dataStructures.DataFrame;
import dataStructures.PartialDataFrame;

import java.util.Random;
import java.util.Set;

public abstract class Sampler {
    protected Random random;

    public Sampler() {
        random=new Random();
    }

    public void setRandomSeed(long randomSeed){
        random.setSeed(randomSeed);
    }

    final public PartialDataFrame sample(DataFrame data){
        int sampleRowCount=Math.max(5,Math.min(data.getRowCount()/100,100));
        return sample(data,new SampleConfig(sampleRowCount));
    }

    final public PartialDataFrame sample(DataFrame data,SampleConfig adviceConfig){
        if(data==null)
            return null;
        int dataLineCount = data.getRowCount();
        //确实抽样方式：固定行数或者百分比
        int sampleLineCount = adviceConfig.isUsePercentage()?
                (int)(dataLineCount * adviceConfig.samplePercentage):
                adviceConfig.sampleLineCount;
        if(sampleLineCount>dataLineCount)
            sampleLineCount=dataLineCount;
        //根据子类实现的chooseLines方法，抽样行索引
        Set<Integer> sampleLines=chooseLines(data,sampleLineCount);
        PartialDataFrame result=new PartialDataFrame(data,sampleLines);
        result.setColumnName(data.getColumnName());
        return result;
    }

    protected abstract Set<Integer> chooseLines(DataFrame data, int adviceSampleSize);


}

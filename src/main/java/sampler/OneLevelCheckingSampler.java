package sampler;

import dataStructures.DataFrame;
import dataStructures.EquivalenceClass;
import dataStructures.PartialDataFrame;
import dataStructures.od.AttributeAndDirection;
import dataStructures.od.ODTreeNodeEquivalenceClasses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OneLevelCheckingSampler extends Sampler {
    private static final int LOW_BOUND = 10;
    private static final int UPPER_BOUND = 100;
    long seed = -1;

    @Override
    protected Set<Integer> chooseLines(DataFrame data, int adviceSampleSize) {
        Set<Integer> result = new HashSet<>();
        int rowCount = data.getRowCount();
        int columnCount = data.getColumnCount();
        if (rowCount <= LOW_BOUND) {
            for (int i = 0; i < rowCount; i++) {
                result.add(i);
            }
            return result;
        }

        int candidateSize = Math.min(rowCount, UPPER_BOUND);
        PartialDataFrame candidateData;
        RandomSampler sampler = new RandomSampler();
        if (seed != -1) {
            sampler.setRandomSeed(seed);
        }
        candidateData = sampler.sample(data, new SampleConfig(candidateSize));
        System.out.println("随机抽样抽样数量： " + candidateSize);

        //所有单属性的sorted partition [i][0]存升序sp，[i][1]存降序sp
        EquivalenceClass[][] equivalenceClasses = new EquivalenceClass[columnCount][2];
        for (int column = 0; column < columnCount; column++) {
            equivalenceClasses[column][0] = new EquivalenceClass();
            equivalenceClasses[column][0].merge(candidateData,
                    AttributeAndDirection.getInstance(column, AttributeAndDirection.UP));
            equivalenceClasses[column][1] = new EquivalenceClass();
            equivalenceClasses[column][1].merge(candidateData,
                    AttributeAndDirection.getInstance(column, AttributeAndDirection.DOWN));
        }

        for (int c1 = 0; c1 < columnCount; c1++) {
            for (int c2 = c1 + 1; c2 < columnCount; c2++) {
                //od的lhs为[c1]，rhs为[c2] 验证该od是否成立，并且记录违反的元组
                //这些violationRows的索引是对于随机抽样得到的数据集的也就是candidateData
                //不是对于原数据集r上的
                result.addAll( new ODTreeNodeEquivalenceClasses
                        (equivalenceClasses[c1][0], equivalenceClasses[c2][0])
                        .check(candidateData).violationRows);

                result.addAll(new ODTreeNodeEquivalenceClasses
                        (equivalenceClasses[c1][0], equivalenceClasses[c2][1])
                        .check(candidateData).violationRows);
            }
        }
        Set<Integer> realResult = new HashSet<>();
        //result是在randomSample candidateData上的索引，而不是对于原数据集r上的索引
        for (Integer i : result) {
            //根据随机抽样得到的candidateData的索引得到原数据集r上的索引
            realResult.add(candidateData.getRealIndex(i));
        }
        return realResult;
    }

    @Override
    public void setRandomSeed(long randomSeed) {
        seed = randomSeed;
    }
}

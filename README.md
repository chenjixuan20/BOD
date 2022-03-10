# BOD

code for Effective and Efficient Lexicographical Order Dependency Discovery 

## Program

you can find our code in

```
src/main/java/
```

As you can see clearly that we have encapsulated the different components.

## Experiment

 We put the entries for BOD,BODM, and BOD* in the Discoverer folder,such as

```
discoverer/multiple/bod_m/BOD_M
```

We have also integrated the dataset used in the experiment into the corresponding folder, such as

```
data/exp1/FLI-500K-17
```

Note that the data set we used is a processed dataset.  You can combine different data sets with algorithms and do all kinds of experiments.  You can use the tools in "/perparation" to process the dataset to obtain a dataset that meets the requirements of the program.

 During the experiment, you can adjust the parameters as much as you like, such as the number of threads in "/bod_x/LightWeightDiscoverer",

```
public static final int DISCOVERPOOLSIZE = 4;
```
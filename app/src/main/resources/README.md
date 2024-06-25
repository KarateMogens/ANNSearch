## ANNSearchBuild

This repository contains an executable build of the [ANNSearch implementation](https://github.com/KarateMogens/ANNSearch) created by Malte Helin Johnsen for his Master's thesis written in the spring of 2024 at ITU. This repository contains a complete build of the code as well as the smallest of the three datasets, Fashion-MNIST. 

### Requirements
- Java 11

---

### Quick-start


The file `config.properties`contains the configuration file, used to configure each experiment trial. The configuration file is commented and should be self-explanatory. The most important think to note, is that for each set of `datastructureArgs` the full set of `searchStrategyArgs` are executed. The config file is preconfigured for a mock experiment trial.

To run the experiment, run the following command from the directory which this file is enclosed in:

`java -jar app.jar config.properties`

It may be necessary to allow the JVM to use more memory than what is allocated by default. This is done in the following way:

`java -Xms6g -Xmx6g app.jar config.properties`

The above setting allocates 6GB of heap memory. It is recommended to allocate a maximum of 75% of total memory in this way.

The results of each experiment trial will be printed to the terminal, as well as saved to a `.hdf5` file. Note that both the file structure of the directory `results` and the result file itself, is compatible with the [ANNBenchmarks tool](https://github.com/erikbern/ann-benchmarks/).

Depending on the configuration, building the index structure may take a while and by default uses all CPU-resources which are available to the program. The log-file `logs/application.log`contains detailed information on the execution of the program, such as the index structure building status etc. If the program is taking a long time to run, I recommend to consult the logs to identify the time-consuming task.

Index structures are serialized after construction. Therefore, any subsequent trials of the same or compatible index structures can skip index construction for faster execution.

---
### Adding Datasets

This repo includes the Fashion-MNIST dataset. To add more datasets, simply download the `.hdf5`file from [ANN-benchmarks](https://ann-benchmarks.com/) and enclose it in the `data` directory. Note that for other datasets than those supplied by default, the secondary index structure to be used for the Natural Classifier Search and the Quick-select Natural Classifier Search strategies must be computed. The program does this automatically for the first experiment trial, but it may take a VERY long time.
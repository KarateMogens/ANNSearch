/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lsh;

// IO
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import ch.systemsx.cisd.hdf5.*;

// Logging
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Other
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import java.text.SimpleDateFormat;  
import java.util.Date;  

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);
    
    Map<String, Method> datastructures;
    ANNSearcherFactory factory = ANNSearcherFactory.getInstance();
    Properties configProperties;
    float[][] test;
    int[][] neighbors;
    float[][] train;
    float[][] distances;
    String metric;
    // FOR JAR BUILD
    // private static final String DATADIRECTORY = "./data";
    // private static final String RESULTSDIRECTORY = "./results";

    // FOR RUNNING IN IDE
    private static final String DATADIRECTORY = "app/src/main/resources/data";
    private static final String RESULTSDIRECTORY = "app/src/main/resources/results";

    public App(String configFilePath) {

        logger.info("Starting application");

        // Load config gile
        configProperties = new Properties();
        try {
            configProperties.load(new FileInputStream(new File(configFilePath)));
            logger.info("Successfully loaded configfile: " + configFilePath);
        } catch (IOException e){
            logger.error(".properties file not found");
        }

        // Check if corpus file exists
        String datasetFile = getProperty("dataset");
        String datasetFilePath = DATADIRECTORY + "/" + datasetFile;
        if (!Utils.fileExists(datasetFilePath)) {
            logger.error("No dataset file located at the specified directory: " + datasetFilePath);
            return;
        }
        
        // Load dataset file
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(datasetFilePath));
        test = reader.readFloatMatrix("test");
        neighbors = reader.readIntMatrix("neighbors");
        train = reader.readFloatMatrix("train");
        distances = reader.readFloatMatrix("distances");
        reader.close();
        logger.info("Succesfully loaded dataset: " + datasetFilePath);

        // Normalize to unit sphere in case of angular distance
        metric = getProperty("metric");
        if (metric.equals("angular")) {
            train = Utils.normalizeCorpus(train);
            test = Utils.normalizeCorpus(test);
            logger.info("Normalized dataset to unit length");
        }

        factory.setDataset(configProperties.getProperty("dataset"), train);
    }

    private void runBenchmarks() {

        MicroBenchmark benchmark = new MicroBenchmark();

        String datastructure = getProperty("datastructure");
        String[] datastructureArgs = getPropertyList("datastructureArgs");
        String searchStrategy = getProperty("searchStrategy");
        String[] searchStrategyArgs = getPropertyList("searchStrategyArgs");  

        for (String datastructureArgList : datastructureArgs) {

            String[] args = getArgs(datastructureArgList);
            ANNSearcher searcher = getSearcher(datastructure, args);
            if (searcher == null) {
                continue;
            }

            // //To focus profiling
            // Scanner myScanner = new Scanner(System.in);
            // logger.info("Ready for profiling");
            // int myint = myScanner.nextInt();

            for (String searchStrategyArgsList : searchStrategyArgs) {
                String[] strategyArgs = getArgs(searchStrategyArgsList);
                logger.info("Benchmarking: " + datastructure + " " + Arrays.toString(args) + " - " + searchStrategy + " " + Arrays.toString(strategyArgs));

                MicroBenchmark.Results results = null;
                switch (searchStrategy) {
                    case "lookupSearch":
                        results = benchmark.benchmark(searcher, test, train, searchStrategy, metric, Integer.parseInt(strategyArgs[0]));
                        break;
                    case "votingSearch":
                        results = benchmark.benchmark(searcher, test, train, searchStrategy, metric, Integer.parseInt(strategyArgs[0]), Integer.parseInt(strategyArgs[1]));
                        break;
                    case "naturalClassifierSearch":
                        results = benchmark.benchmark(searcher, test, train, searchStrategy, metric, Integer.parseInt(strategyArgs[0]), Float.parseFloat(strategyArgs[1]));
                        break;
                    case "naturalClassifierSearchRawCount":
                        results = benchmark.benchmark(searcher, test, train, searchStrategy, metric, Integer.parseInt(strategyArgs[0]), Integer.parseInt(strategyArgs[1]));
                        break;
                    case "naturalClassifierSearchSetSize":
                        results = benchmark.benchmark(searcher, test, train, searchStrategy, metric, Integer.parseInt(strategyArgs[0]), Integer.parseInt(strategyArgs[1]));
                        break;
                    case "bruteForceSearch":
                        results = benchmark.benchmark(searcher, test, train, searchStrategy, metric, Integer.parseInt(strategyArgs[0]));
                        break;
                    default:
                        logger.error("Error calling search strategy: " + searchStrategy + ". Search strategy is not valid");
                        continue;
                }
                System.out.println("\n" + datastructure + " " + Arrays.toString(args) + " - " + searchStrategy + " " + Arrays.toString(strategyArgs));
                printStats(results);
                writeResults(results, datastructure, args, searchStrategy, strategyArgs);
            }
        }
    }


    private ANNSearcher getSearcher(String datastructure, String[] args) {
        // Return the ANNSearch object based on arguments from config file
        try {
            switch (datastructure) {
                case "RKDTree":
                    return factory.getNCTreeSearcher(Integer.parseInt(args[0]), Integer.parseInt(args[1]), "RKD", Integer.parseInt(args[2]));
                case "RPTree":
                    return factory.getNCTreeSearcher(Integer.parseInt(args[0]), Integer.parseInt(args[1]), "RP", Integer.parseInt(args[2]));
                case "LSH":
                    return factory.getNCLSHSearcher(Integer.parseInt(args[0]), Float.parseFloat(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
                case "AngLSH":
                    return factory.getAngNCLSHSearcher(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                case "C2LSH":
                    return factory.getNCC2LSHSearcher(Integer.parseInt(args[0]), Integer.parseInt(args[1]),  Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
            }   
        } catch (FileNotFoundException e) {
            logger.error("Error getting datastructure: " + datastructure + "with arguments: " + Arrays.toString(args));
        }
        return null;
    }


    private String[] getArgs(String argsString) {
        return argsString.trim().split(" +");
    }

    private String getProperty(String key) {
        return configProperties.getProperty(key).trim();
    }

    private String[] getPropertyList(String key) {
        return getProperty(key).split(";");
    }

    private void printStats(MicroBenchmark.Results results) {
        // These statistics are only meant for guiding parameter configuration
        // the mean recall is calculated differently (based on distance) in ANN benchmarks
        // and may give differing results
        results.calculateStatistics(neighbors, distances);
        System.out.printf("%-25s %s%n", "Mean seconds/query:", results.getMeanQueryTime());
        System.out.printf("%-25s %s%n", "Std. dev query time:", results.getStandardDeviation());
        System.out.printf("%-25s %s%n", "Max query time:", results.getMaxTime());
        System.out.printf("%-25s %s%n", "Min query time:", results.getMinTime());
        System.out.printf("%-25s %s%n", "Mean queries/second:", results.getQueriesPrSecond());
        System.out.printf("%-25s %s%n", "Mean C-size:", results.getMeanCandidateSetSize());
        System.out.printf("%-25s %s%n", "Median C-size:", results.getMedianCandidateSetSize());
        System.out.printf("%-25s %s%n", "Mean # neighbors found:", results.getMeanNeighborsFound());
        System.out.printf("%-25s %s%n", "Mean recall:", results.getMeanRecall());
    }

    private void writeResults(MicroBenchmark.Results results, String datastructure, String[] datastructureArgs, String searchStrategy, String[] searchStrategyArgs) {
        logger.trace("Writing benchmarking results to .hdf5");
        String identifier = createIdentifier(datastructure, datastructureArgs, searchStrategy, searchStrategyArgs);
        File resultsFile = createHDF5(results, identifier);
        IHDF5Writer writer = HDF5FactoryProvider.get().open(resultsFile);
        // Write results
        writer.writeIntMatrix("neighbors", results.getNeighbors());
        writer.writeFloatMatrix("distances", results.getDistances());
        writer.writeFloatArray("times", results.getQueryTimes());
        // Write algorithm name
        writer.string().setAttr("/", "algo", datastructure + searchStrategy);
        writer.string().setAttr("/", "name", datastructure + Arrays.toString(datastructureArgs) + searchStrategy + Arrays.toString(searchStrategyArgs));
        // Write dataset name
        String dataset = getProperty("dataset");
        dataset = dataset.substring(0, dataset.lastIndexOf("."));
        writer.string().setAttr("/", "dataset", dataset);
        // Write distance metric
        writer.string().setAttr("/", "metric", metric);
        // Batchmode - always false
        writer.bool().setAttr("/", "batch_mode", false);
        // Number of runs
        writer.int32().setAttr("/", "run_count", 1);
        // Build time - NOT SUPPORTED
        writer.float32().setAttr("/", "build_time", 1.0f);
        // Write avg. neighbors found
        writer.float32().setAttr("/", "candidates", results.getMeanNeighborsFound());
        // Write timestamp
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
        Date date = new Date();  
        writer.string().setAttr("/", "timestamp", formatter.format(date));
        // Number of neighbors to return (k)
        writer.int32().setAttr("/", "count", results.getCount());
        // Mean query time (best mean query time if multiple runs are used)
        writer.float32().setAttr("/", "best_search_time", results.getMeanQueryTime());
        // Mean candidate set size 
        writer.float32().setAttr("/", "mean_candidate_set_size", results.getMeanCandidateSetSize());
        // Median candidate set size 
        writer.float32().setAttr("/", "median_candidate_set_size", results.getMedianCandidateSetSize());
        // Standard deviation of query time
        writer.float32().setAttr("/", "std_dev_query_time", results.getStandardDeviation());

        writer.close();
    }

    private String createIdentifier(String datastructure, String[] datastructureArgs, String searchStrategy, String[] searchStrategyArgs) {
        StringBuilder builder = new StringBuilder();
        builder.append(datastructure);
        for (String arg : datastructureArgs) {
            builder.append(arg);
            builder.append("_");
        }
        builder.append(searchStrategy);
        for (String arg : searchStrategyArgs) {
            builder.append(arg);
            builder.append("_");
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }

    private File createHDF5(MicroBenchmark.Results results, String identifier) {
        try {
            String datasetName = getProperty("dataset").substring(0, getProperty("dataset").lastIndexOf("."));
            String resultpath = RESULTSDIRECTORY + "/" + datasetName + "/" + results.getCount();
            Path datastructureFileDirectory = Paths.get(resultpath);
            Files.createDirectories(datastructureFileDirectory);
            File resultsFile = new File(resultpath + "/" + identifier + ".hdf5" );
            resultsFile.createNewFile();
            return resultsFile;
        } catch (IOException e) {
            logger.error("Error creating a results file");
            return null;
        }  
    }

    public static void main(String[] args) {

        // C2LSH.PointerSet myPointerSet = new C2LSH.PointerSet(5);

        // int oldR = 1;
        // int R = 2;
        // while (true) {
        //     // for (int iterate = oldR; oldR <= R; oldR++) {
        //     //     System.out.println(myPointerSet.next());
        //     // }
        //     oldR = R;
        //     R = R*R;
        //     myPointerSet.increaseWidth(R);
        // }
       
        // // FOR JAR BUILD
        // App myApp = new App(args[0]);
        // FOR RUNNING IN IDE
        App myApp = new App("app/src/main/resources/config.properties");
        

        myApp.runBenchmarks();
        logger.info("Terminating application");

    }
}

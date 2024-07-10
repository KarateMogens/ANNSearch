package lsh;

// IO
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

// Serialization
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

// Other imports
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.*;

// Logging
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class ANNSearcherFactory {

    private static final Logger logger = LogManager.getLogger(ANNSearcherFactory.class);
    private static final ANNSearcherFactory factory = new ANNSearcherFactory();
    private static final Kryo kryo = new Kryo();

    private static String DATASETFILENAME;
    private static String DATASET;
    private static float[][] corpusMatrix;

    // FOR JAR BUILD
    private static String DATASTRUCTUREDIRECTORY = "./datastructures";
    // FOR RUNNING IN IDE
    // private static String DATASTRUCTUREDIRECTORY = "app/src/main/resources/datastructures";

    private ANNSearcherFactory() {}

    public static ANNSearcherFactory getInstance() {
        kryo.register(C2LSH.class, new JavaSerializer());
        kryo.register(RKDTree.class, new JavaSerializer());
        kryo.register(RPTree.class, new JavaSerializer());
        kryo.register(HashTable.class, new JavaSerializer());
        kryo.register(AngHashTable.class, new JavaSerializer());
        kryo.register(BinaryHash.class, new JavaSerializer());
        kryo.register(Tree.class, new JavaSerializer());
        kryo.register(java.util.ArrayList.class, new JavaSerializer());
        kryo.register(int[][].class, new JavaSerializer());
        kryo.setReferences(false); // Turned off to save memory during serialiation
        Locale.setDefault(Locale.UK);
        return factory;
    }

    public void setDataset(String datasetFile, float[][] corpusMatrixInput) {
        
        DATASETFILENAME = datasetFile;
        DATASET = datasetFile.substring(0, DATASETFILENAME.lastIndexOf("."));
        
        DATASTRUCTUREDIRECTORY = DATASTRUCTUREDIRECTORY + "/" + DATASET + "/";
        Path datastructureFileDirectory = Paths.get(DATASTRUCTUREDIRECTORY);
        try {
            Files.createDirectories(datastructureFileDirectory);
        } catch (IOException e) {
            logger.error("Error creating directory: " + datastructureFileDirectory);
        }   
        logger.info("Set dataset of ANNSearcherFactor to: " + DATASETFILENAME);

        corpusMatrix = corpusMatrixInput;

    }


    // ------------ Partition Tree ------------

    private ANNSearcher getTreeSearcher(int maxLeafSize, int L, String type) throws FileNotFoundException {

        if (!type.equals("RP") && !type.equals("RKD")) {
            logger.error("Could not get Tree searcher: " + type + " is not a valid type.");
        }

        if (DATASETFILENAME == null) {
            throw new FileNotFoundException("No dataset specified.");
        }

        // float[][] corpusMatrix = getCorpusMatrix();

        List<Searchable> searchables;
        File datastructure = getSuitableForest(maxLeafSize, L, type);

        if (datastructure == null) {
            
            searchables = searchableForest(maxLeafSize, L, corpusMatrix, type);
            
            // Write searchables to disk
            String fileName = String.format(type + "Tree_%1$d_%2$d.ser", maxLeafSize, L);
            writeToDisk(searchables, DATASTRUCTUREDIRECTORY, fileName);
        } else {

            searchables = (List<Searchable>) readFromDisk(DATASTRUCTUREDIRECTORY, datastructure);
            if (searchables.size() > L) {
                searchables = new LinkedList<>(searchables.subList(0, L));
            }
        }

        return new ANNSearcher(searchables, corpusMatrix);
    }

    public ANNSearcher getNCTreeSearcher(int maxLeafSize, int L, String type, int k) throws FileNotFoundException {

        ANNSearcher mySearcher = getTreeSearcher(maxLeafSize, L, type);
        int[][] secondaryIndex = getSecondIndex(k);
        mySearcher.setSecondaryIndex(secondaryIndex, k);
        return mySearcher;
    }

    private List<Searchable> searchableForest(int maxLeafSize, int L, float[][] corpusMatrix, String type) {
        logger.info("Started constructing searchable  forest: maxLeafSize = " + maxLeafSize + ", L = " +  L + ", type = "+ type);

        List<Searchable> searchables = new ArrayList<Searchable>(Collections.nCopies(L, null));
        ExecutorService pool = new ForkJoinPool();
        Future<?> finished = pool.submit(new BuildForestTask(searchables, 0, L-1, pool, type, maxLeafSize, corpusMatrix));
        try {
            finished.get();
            BuildForestTask.resetCount();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error constructing searchable forest: maxLeafSize = " + maxLeafSize + ", L = " +  L + ", type = "+ type);
            System.exit(1);
        }
        pool.shutdown();

        logger.info("Finished constructing searchable forest: maxLeafSize = " + maxLeafSize + ", L = " +  L + ", type = "+ type);
        return searchables;
    }

    // ------------ CLASSIC LSH ------------

    private ANNSearcher getLSHSearcher(int K, float r, int L) throws FileNotFoundException {

        if (DATASETFILENAME == null) {
            throw new FileNotFoundException("No dataset specified.");
        }

        // float[][] corpusMatrix = getCorpusMatrix();

        List<Searchable> searchables;
        File datastructure = getSuitableLSH(K, r, L);
        
        if (datastructure == null) {
            // Create a new list of HashTables
            searchables = searchableLSH(K, r, L, corpusMatrix);

            // Write searchables to disk
            String fileName = String.format("LSH_%1$d_%2$f_%3$d.ser", K, r, L);
            writeToDisk(searchables, DATASTRUCTUREDIRECTORY, fileName);

        } else {
            // Read searchables from disk and reduce size
            searchables = (List<Searchable>) readFromDisk(DATASTRUCTUREDIRECTORY, datastructure);
            if (searchables.size() > L) {
                searchables = new LinkedList<>(searchables.subList(0, L));
            }
        }

        return new ANNSearcher(searchables, corpusMatrix);
    }

    public ANNSearcher getNCLSHSearcher(int K, float r, int L, int k) throws FileNotFoundException {

        ANNSearcher LSHSearcher = getLSHSearcher(K, r, L);
        int[][] secondaryIndexMatrix = getSecondIndex(k);
        LSHSearcher.setSecondaryIndex(secondaryIndexMatrix, k);

        return LSHSearcher;
    }   

    private List<Searchable> searchableLSH(int K, float r, int L, float[][] corpusMatrix) {
        
        logger.info("Started constructing LSH: K = " + K + ", r = " + r + ", L = " + L);

        List<Searchable> searchables = new ArrayList<Searchable>(Collections.nCopies(L, null));
        ExecutorService pool = new ForkJoinPool();
        Future<?> finished = pool.submit(new BuildLSHTask(searchables, 0, L-1, pool, "", K, r, corpusMatrix));
        try {
            finished.get();
            BuildLSHTask.resetCount();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error constructing searchable LSH: K = " + K + ", L = " +  L + ", type = normal");
            System.exit(1);
        }
        pool.shutdown();
        logger.info("Finished constructing LSH " + "K = " + K + ", r = " + r + ", L = " + L);
        return searchables;
    }


    // ------------ Angular LSH ------------


    private ANNSearcher getAngLSHSearcher(int K, int L) throws FileNotFoundException {

        if (DATASETFILENAME == null) {
            throw new FileNotFoundException("No dataset specified.");
        }

        // float[][] corpusMatrix = getCorpusMatrix();

        List<Searchable> searchables;
        File datastructure = getSuitableAngLSH(K, L);
        
        if (datastructure == null) {
            // Create a new list of HashTables
            searchables = searchableAngLSH(K, L, corpusMatrix);

            // Write searchables to disk
            String fileName = String.format("AngLSH_%1$d_%2$d.ser", K, L);
            writeToDisk(searchables, DATASTRUCTUREDIRECTORY, fileName);

        } else {
            // Read searchables from disk and reduce size
            searchables = (List<Searchable>) readFromDisk(DATASTRUCTUREDIRECTORY, datastructure);
            if (searchables.size() > L) {
                searchables = new LinkedList<>(searchables.subList(0, L));
            }
        }

        return new ANNSearcher(searchables, corpusMatrix);
    }

    public ANNSearcher getAngNCLSHSearcher(int K, int L, int k) throws FileNotFoundException {

        ANNSearcher LSHSearcher = getAngLSHSearcher(K, L);
        int[][] secondaryIndexMatrix = getSecondIndex(k);
        LSHSearcher.setSecondaryIndex(secondaryIndexMatrix, k);

        return LSHSearcher;
    }   

    private List<Searchable> searchableAngLSH(int K, int L, float[][] corpusMatrix) {
        
        logger.info("Started constructing AngLSH: K = " + K + ", L = " + L);
        int d = corpusMatrix[0].length;

        List<Searchable> searchables = new ArrayList<Searchable>(Collections.nCopies(L, null));
        ExecutorService pool = new ForkJoinPool();
        Future<?> finished = pool.submit(new BuildLSHTask(searchables, 0, L-1, pool, "", K, 0.0f, corpusMatrix));
        try {
            finished.get();
            BuildLSHTask.resetCount();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error constructing searchable LSH: K = " + K + ", L = " +  L + ", type = AngLSH");
            System.exit(1);
        }
        pool.shutdown();
        logger.info("Finished constructing AngLSH " + "K = " + K + ", L = " + L);
        return searchables;
    }
    

    /* ----------- Collision Counting LSH ----------- */

    public ANNSearcher getC2LSHSearcher(int K, int minSize, int threshold, int L, boolean angular) throws FileNotFoundException {

        if (DATASETFILENAME == null) {
            throw new FileNotFoundException("No dataset specified.");
        }

        // float[][] corpusMatrix = getCorpusMatrix();

        List<Searchable> searchables;
        File datastructure = getSuitableC2LSH(K, L, angular);
        
        if (datastructure == null) {
            // Create a new list of HashTables
            searchables = searchableC2LSH(K, minSize, threshold, angular, L, corpusMatrix);

            // Write searchables to disk
            String fileName;
            if (angular) {
                fileName = String.format("AngC2LSH_%1$d_%2$d.ser", K, L);
            } else {
                fileName = String.format("C2LSH_%1$d_%2$d.ser", K, L);
            }
            
            writeToDisk(searchables, DATASTRUCTUREDIRECTORY, fileName);

        } else {
            // Read searchables from disk and reduce size
            searchables = (List<Searchable>) readFromDisk(DATASTRUCTUREDIRECTORY, datastructure);
            if (searchables.size() > L) {
                searchables = new LinkedList<>(searchables.subList(0, L));
            }
            for (Searchable searchable : searchables) {
                C2LSH myC2LSH = (C2LSH) searchable;
                myC2LSH.setMinSize(minSize);
                myC2LSH.setThreshold(threshold);
            }
        }

        return new ANNSearcher(searchables, corpusMatrix);
    }

    public ANNSearcher getNCC2LSHSearcher(int K, int minSize, int threshold, int L, int k, boolean angular) throws FileNotFoundException {

        ANNSearcher C2LSHSearcher = getC2LSHSearcher(K, minSize, threshold, L, angular);
        int[][] secondaryIndexMatrix = getSecondIndex(k);
        C2LSHSearcher.setSecondaryIndex(secondaryIndexMatrix, k);

        return C2LSHSearcher;
    }   

    private List<Searchable> searchableC2LSH(int K, int minSize, int threshold, boolean angular, int L, float[][] corpusMatrix) {
        logger.info("Started constructing C2LSH: K = " + K + ", minSize = " + minSize + ", threshold = " + threshold + ", L = " +  L);
        int d = corpusMatrix[0].length;

        List<Searchable> searchables = new ArrayList<Searchable>(L);
        for (int l = 0; l < L; l++) {
            Searchable hashTable = new C2LSH(d, K, minSize, threshold, angular);
            hashTable.fit(corpusMatrix);
            searchables.add(l, hashTable);
            logger.trace("Constructed C2LSH " + (l+1) + "/" + L);
        }
        logger.info("Finished constructing C2LSH: K = " + K + ", minSize = " + minSize + ", threshold = " + threshold + ", L = " +  L);
        return searchables;    
    }
    /* ----------- IO Methods ----------- */

    private void writeToDisk(Object object, String directory, String fileName) {
        logger.info("Started writing " + fileName);
        // kryo.register(C2LSH.class, new JavaSerializer());
        // kryo.register(java.util.ArrayList.class, new JavaSerializer());
        try (FileOutputStream myStream = new FileOutputStream(directory + fileName); Output myOutput = new Output(myStream)) {
            kryo.writeClassAndObject(myOutput, object);
            myStream.flush();
            logger.info("Finished writing " + fileName);
        } catch (IOException e) {
            logger.error("There was an error writing " + fileName + "to " + directory);
        }
    }

    private Object readFromDisk(String directory, File targetFile) {
        logger.info("Started loading " + targetFile.getName());
        try (FileInputStream myStream = new FileInputStream(directory + targetFile.getName()); Input myInput = new Input(myStream)) {
            Object myObject = kryo.readClassAndObject(myInput);
            logger.info("Finished loading " + targetFile.getName());
            return myObject;
        } catch (IOException e) {
           logger.error("Error loading file: " + targetFile.getName());
           return null;
        }
    }

    private File getSuitableForest(int maxLeafSize, int L, String type) {
        File directory = new File(DATASTRUCTUREDIRECTORY);
        File[] files = directory.listFiles();
        
        // To iterate over alphabetically order, thus taking smallest suitable first.
        Arrays.sort(files);

        Pattern pattern = Pattern.compile(type + "Tree_(\\d+)_(\\d+).ser");

        for (File file : files) {
            String fileName = file.getName();
            Matcher match = pattern.matcher(fileName);
            if (!match.matches() || Integer.parseInt(match.group(1)) != maxLeafSize) {
                continue;
            }

            if (Integer.parseInt(match.group(2)) < L) {
                continue;
            }
            return file;
        }

        return null;
    }

    private File getSuitableLSH(int K, float r, int L) {
        File directory = new File(DATASTRUCTUREDIRECTORY);
        File[] files = directory.listFiles();

        // To iterate over alphabetically order, thus taking smallest suitable first.
        Arrays.sort(files);

        Pattern pattern = Pattern.compile("LSH_(\\d+)_(\\d+.\\d+)_(\\d+).ser");

        for (File file : files) {
            String fileName = file.getName();
            Matcher match = pattern.matcher(fileName);
            if (!match.matches() || Integer.parseInt(match.group(1)) != K || Float.parseFloat(match.group(2)) != r) {
                continue;
            }

            if (Integer.parseInt(match.group(3)) < L) {
                continue;
            }
            return file;
        }
        return null;
 
    }

    private File getSuitableAngLSH(int K, int L) {
        File directory = new File(DATASTRUCTUREDIRECTORY);
        File[] files = directory.listFiles();

        // To iterate over alphabetically order, thus taking smallest suitable first.
        Arrays.sort(files);

        Pattern pattern = Pattern.compile("AngLSH_(\\d+)_(\\d+).ser");

        for (File file : files) {
            String fileName = file.getName();
            Matcher match = pattern.matcher(fileName);
            if (!match.matches() || Integer.parseInt(match.group(1)) != K) {
                continue;
            }

            if (Integer.parseInt(match.group(2)) < L) {
                continue;
            }
            return file;
        }
        return null;
 
    }

    private File getSuitableC2LSH(int K, int L, boolean angular) {
        File directory = new File(DATASTRUCTUREDIRECTORY);
        File[] files = directory.listFiles();

        // To iterate over alphabetically order, thus taking smallest suitable first.
        Arrays.sort(files);

        Pattern pattern;
        if (angular) {
            pattern = Pattern.compile("AngC2LSH_(\\d+)_(\\d+).ser");
        } else {
            pattern = Pattern.compile("C2LSH_(\\d+)_(\\d+).ser");
        }
        

        for (File file : files) {
            String fileName = file.getName();
            Matcher match = pattern.matcher(fileName);
            if (!match.matches() || Integer.parseInt(match.group(1)) != K) {
                continue;
            }

            if (Integer.parseInt(match.group(2)) < L) {
                continue;
            }
            return file;
        }
        return null;
    }

    private int[][] getSecondIndex(int k) {
        File secondaryIndex = getSecondIndexFile(k);
        int[][] secondaryIndexMatrix;

        if (secondaryIndex == null) {
            
            logger.info("No suitable secondary index of size " + k + " found. Constructing new index");
            // Calculate new ground truth
            secondaryIndexMatrix = Utils.groundTruthParallel(corpusMatrix, k);
            logger.info("Finished constructing new index");
            writeToDisk(secondaryIndexMatrix, DATASTRUCTUREDIRECTORY, String.format("%1$s-groundtruth-%2$d.ser", DATASET, k));
        } else {
            secondaryIndexMatrix = (int[][]) readFromDisk(DATASTRUCTUREDIRECTORY, secondaryIndex);
        }
        return secondaryIndexMatrix;
    }


    private File getSecondIndexFile(int k) {

        File directory = new File(DATASTRUCTUREDIRECTORY);
        File[] files = directory.listFiles();

        if (files == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(DATASET + "-groundtruth-(\\d+).ser");
        for (File file : files) {
            String fileName = file.getName();
            Matcher match = pattern.matcher(fileName);
            if (match.matches() && Integer.parseInt(match.group(1)) >= k) {
                return file;
            }
        }

        return null;
    }

    public static class BuildForestTask implements Runnable {

        private final List<Searchable> searchables;
        private final int low;
        private final int high;
        private final ExecutorService pool;
        private final String type;
        private final int maxLeafSize;
        private final float[][] corpusMatrix;
        private final int threshold = 2;
        private final static AtomicInteger ctr = new AtomicInteger();
        private final Logger logger = LogManager.getLogger(this);

        public BuildForestTask(List<Searchable> searchables, int low, int high, ExecutorService pool, String type, int maxLeafSize, float[][] corpusMatrix) {
            this.searchables = searchables;
            this.low = low;
            this.high = high;
            this.pool = pool;
            this.maxLeafSize = maxLeafSize;
            this.type = type;
            this.corpusMatrix = corpusMatrix;
        }

        public static void resetCount() {
            ctr.set(0);
        }

        @Override
        public void run() {
            // Threshold reached, build trees in local thread
            if ((high-low) <= threshold) {
                for (int i = low; i <= high; i++) {
                    Searchable tree = null;
                    if (type.equals("RP")) {
                        tree = new RPTree(maxLeafSize);
                    } else if (type.equals("RKD")) {
                        tree = new RKDTree(maxLeafSize);
                    }
                    tree.fit(corpusMatrix);
                    searchables.set(i, tree);
                    logger.trace("Constructed " + type + "-Tree: " + ctr.incrementAndGet());
                }
            // Split task into subtasks
            } else {
                int mid = low + (high-low)/2;
                Future<?> f1 = pool.submit(new BuildForestTask(searchables, low, mid, pool, type, maxLeafSize, corpusMatrix));
                Future<?> f2 = pool.submit(new BuildForestTask(searchables, mid+1, high, pool, type, maxLeafSize, corpusMatrix));
                try {
                    f1.get();
                    f2.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error constructing searchable tree: maxLeafSize = " + maxLeafSize + ", type = "+ type);
                    e.printStackTrace();
                    System.exit(1);
                }
            }

        }
    }

    public static class BuildLSHTask implements Runnable {

        private final List<Searchable> searchables;
        private final int low;
        private final int high;
        private final ExecutorService pool;
        private final String type;
        private final int K;
        private final float r;
        private final float[][] corpusMatrix;
        private final int threshold = 2;
        private final static AtomicInteger ctr = new AtomicInteger();
        private final Logger logger = LogManager.getLogger(this);

        public BuildLSHTask(List<Searchable> searchables, int low, int high, ExecutorService pool, String type, int K, float r, float[][] corpusMatrix) {
            this.searchables = searchables;
            this.low = low;
            this.high = high;
            this.pool = pool;
            this.K = K;
            this.r = r;
            this.type = type;
            this.corpusMatrix = corpusMatrix;
        }

        public static void resetCount() {
            ctr.set(0);
        }

        @Override
        public void run() {
            // Threshold reached, build trees in local thread
            if ((high-low) <= threshold) {
                for (int i = low; i <= high; i++) {
                    Searchable lsh = null;
                    if (type.equals("Ang")) {
                        lsh = new AngHashTable(corpusMatrix[0].length, K);
                    } else {
                        lsh = new HashTable(corpusMatrix[0].length, K, r);
                    }
                    lsh.fit(corpusMatrix);
                    searchables.set(i, lsh);
                    logger.trace("Constructed " + type + "LSH: " + ctr.incrementAndGet());
                }
            // Split task into subtasks
            } else {
                int mid = low + (high-low)/2;
                Future<?> f1 = pool.submit(new BuildLSHTask(searchables, low, mid, pool, type, K, r, corpusMatrix));
                Future<?> f2 = pool.submit(new BuildLSHTask(searchables, mid+1, high, pool, type, K, r, corpusMatrix));
                try {
                    f1.get();
                    f2.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error constructing searchable LSH: K = " + K + ", type = "+ type);
                    e.printStackTrace();
                    System.exit(1);
                }
            }

        }
    }

}

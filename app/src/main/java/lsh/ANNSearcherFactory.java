package lsh;

import java.io.BufferedOutputStream;
// Serialization and IO
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;

// Other imports
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ANNSearcherFactory {

    private static final Logger logger = LogManager.getLogger(ANNSearcherFactory.class);
    private static final ANNSearcherFactory factory = new ANNSearcherFactory();
   
    private static String DATASETFILENAME;
    private static String DATASET;
    private static String DATADIRECTORY;
    private static float[][] corpusMatrix;
    private static String metric;

    private ANNSearcherFactory() {}

    public static ANNSearcherFactory getInstance() {
        return factory;
    }

    public void setDataset(String datasetURLString, String metric, float[][] corpusMatrix) {
       

        String datasetName = datasetURLString.substring(datasetURLString.lastIndexOf("/") + 1, datasetURLString.lastIndexOf("."));
        String datadirectory = datasetURLString.substring(0, datasetURLString.lastIndexOf("/") + 1);

        DATASET = datasetName;
        DATASETFILENAME = datasetURLString;
        DATADIRECTORY = datadirectory;
        logger.info("Set dataset of ANNSearcherFactor to: " + datasetURLString);
        this.metric = metric;
        this.corpusMatrix = corpusMatrix;

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
            writeToDisk(searchables, DATADIRECTORY, fileName);
        } else {

            searchables = (List<Searchable>) readFromDisk(DATADIRECTORY, datastructure);
            if (searchables.size() > L) {
                searchables = new LinkedList<>(searchables.subList(0, L));
            }
        }

        return new ANNSearcher(searchables, corpusMatrix);
    }

    public ANNSearcher getNCTreeSearcher(int maxLeafSize, int L, String type, int k) throws FileNotFoundException {

        ANNSearcher mySearcher = getTreeSearcher(maxLeafSize, L, type);
        int[][] secondaryIndex = getSecondIndex(k, mySearcher.getCorpusMatrix());
        mySearcher.setSecondaryIndex(secondaryIndex, k);
        return mySearcher;
    }

    private List<Searchable> searchableForest(int maxLeafSize, int L, float[][] corpusMatrix, String type) {
        logger.info("Started constructing searchable  forest: maxLeafSize = " + maxLeafSize + ", L = " +  L + ", type = "+ type);

        int d = corpusMatrix[0].length;

        List<Searchable> searchables = new ArrayList<Searchable>(L);
        for (int l = 0; l < L; l++) {
            Searchable tree = null;
            if (type.equals("RP")) {
                tree = new RPTree(maxLeafSize);
            } else if (type.equals("RKD")) {
                tree = new RKDTree(maxLeafSize);
            }
            tree.fit(corpusMatrix);
            searchables.add(l, tree);
            logger.trace("Constructed tree " + (l+1) + "/" + L);
        }

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
            writeToDisk(searchables, DATADIRECTORY, fileName);

        } else {
            // Read searchables from disk and reduce size
            searchables = (List<Searchable>) readFromDisk(DATADIRECTORY, datastructure);
            if (searchables.size() > L) {
                searchables = new LinkedList<>(searchables.subList(0, L));
            }
        }

        return new ANNSearcher(searchables, corpusMatrix);
    }

    public ANNSearcher getNCLSHSearcher(int K, float r, int L, int k) throws FileNotFoundException {

        ANNSearcher LSHSearcher = getLSHSearcher(K, r, L);
        int[][] secondaryIndexMatrix = getSecondIndex(k, LSHSearcher.getCorpusMatrix());
        LSHSearcher.setSecondaryIndex(secondaryIndexMatrix, k);

        return LSHSearcher;
    }   

    private List<Searchable> searchableLSH(int K, float r, int L, float[][] corpusMatrix) {
        
        logger.info("Started constructing LSH: K = " + K + ", r = " + r + ", L = " + L);
        int d = corpusMatrix[0].length;

        List<Searchable> searchables = new ArrayList<Searchable>(L);
        for (int l = 0; l < L; l++) {
            Searchable hashTable = new HashTable(d, K, r);
            hashTable.fit(corpusMatrix);
            searchables.add(l, hashTable);
            logger.trace("Constructed LSH " + (l+1) + "/" + L);
        }
        logger.info("Finished constructing LSH " + "K = " + K + ", r = " + r + ", L = " + L);
        return searchables;
    }


    /* ----------- Collision Counting LSH ----------- */

    public ANNSearcher getC2LSHSearcher(int K, int minSize, int threshold, int L) throws FileNotFoundException {

        if (DATASETFILENAME == null) {
            throw new FileNotFoundException("No dataset specified.");
        }

        // float[][] corpusMatrix = getCorpusMatrix();

        List<Searchable> searchables;
        File datastructure = getSuitableC2LSH(K, L);
        
        if (datastructure == null) {
            // Create a new list of HashTables
            searchables = searchableC2LSH(K, minSize, threshold, L, corpusMatrix);

            // Write searchables to disk
            String fileName = String.format("C2LSH_%1$d_%2$d.ser", K, L);
            writeToDisk(searchables, DATADIRECTORY, fileName);

        } else {
            // Read searchables from disk and reduce size
            searchables = (List<Searchable>) readFromDisk(DATADIRECTORY, datastructure);
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

    public ANNSearcher getNCC2LSHSearcher(int K, int minSize, int threshold, int L, int k) throws FileNotFoundException {

        ANNSearcher LSHSearcher = getC2LSHSearcher(K, minSize, threshold, L);
        int[][] secondaryIndexMatrix = getSecondIndex(k, LSHSearcher.getCorpusMatrix());
        LSHSearcher.setSecondaryIndex(secondaryIndexMatrix, k);

        return LSHSearcher;
    }   

    private List<Searchable> searchableC2LSH(int K, int minSize, int threshold, int L, float[][] corpusMatrix) {
        logger.info("Started constructing C2LSH: K = " + K + ", minSize = " + minSize + ", threshold = " + threshold + ", L = " +  L);
        int d = corpusMatrix[0].length;

        List<Searchable> searchables = new ArrayList<Searchable>(L);
        for (int l = 0; l < L; l++) {
            Searchable hashTable = new C2LSH(d, K, minSize, threshold);
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
        try (ObjectOutputStream myStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(directory + fileName)))) {
            myStream.writeObject(object);
            myStream.flush();
            logger.info("Finished writing " + fileName);
        } catch (IOException e) {
            logger.error("There was an error writing " + fileName + "to " + directory);
        }
    }

    private Object readFromDisk(String directory, File targetFile) {
        logger.info("Started loading " + targetFile.getName());
        try (ObjectInputStream myStream = new ObjectInputStream(new FileInputStream(directory + targetFile.getName()))) {
            Object myObject = myStream.readObject();
            logger.info("Finished loading " + targetFile.getName());
            return myObject;
        } catch (IOException|ClassNotFoundException e) {
           logger.error("Error loading file: " + targetFile.getName());
           return null;
        }
    }

    private File getSuitableForest(int maxLeafSize, int L, String type) {
        File directory = new File(DATADIRECTORY);
        File[] files = directory.listFiles();

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
        File directory = new File(DATADIRECTORY);
        File[] files = directory.listFiles();

        Pattern pattern = Pattern.compile("LSH_(\\d+)_(\\d+,\\d+)_(\\d+).ser");

        for (File file : files) {
            String fileName = file.getName();
            Matcher match = pattern.matcher(fileName);
            if (!match.matches() || Integer.parseInt(match.group(1)) != K || Float.parseFloat(match.group(2).replace(",", ".")) != r) {
                continue;
            }

            if (Integer.parseInt(match.group(3)) < L) {
                continue;
            }
            return file;
        }
        return null;

    }

    private File getSuitableC2LSH(int K, int L) {
        File directory = new File(DATADIRECTORY);
        File[] files = directory.listFiles();

        Pattern pattern = Pattern.compile("C2LSH_(\\d+)_(\\d+).ser");

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

    private int[][] getSecondIndex(int k, float[][] corpusMatrix) {
        File secondaryIndex = getSecondIndexFile(k);
        int[][] secondaryIndexMatrix;

        if (secondaryIndex == null) {
            
            logger.info("No suitable secondary index of size " + k + " found. Constructing new index");
            // Calculate new ground truth
            secondaryIndexMatrix = Utils.groundTruthParallel(corpusMatrix, k);
            logger.info("Finished constructing new index");
            writeToDisk(secondaryIndexMatrix, DATADIRECTORY, String.format("%1$s-groundtruth-%2$d.ser", DATASET, k));
        } else {
            secondaryIndexMatrix = (int[][]) readFromDisk(DATADIRECTORY, secondaryIndex);
        }
        return secondaryIndexMatrix;
    }


    private File getSecondIndexFile(int k) {

        File directory = new File(DATADIRECTORY);
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

    // private float[][] getCorpusMatrix() {
    //     IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATASETFILENAME));
    //     float[][] corpusMatrix = reader.readFloatMatrix("train");
    //     if (metric.equals("angular")) {
    //         corpusMatrix = Utils.normalizeCorpus(corpusMatrix);
    //     }
    //     return corpusMatrix;
    // }

    


}

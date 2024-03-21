package lsh;

// HDF5 Handling
import ch.systemsx.cisd.hdf5.HDF5FactoryProvider;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

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
import java.util.regex.*;

public class ANNSearcherFactory {

    private static final ANNSearcherFactory factory = new ANNSearcherFactory();
    //private static final String RESOURCEDIRECTORY = "./";
    private static final String RESOURCEDIRECTORY = "app/src/main/resources/";
    
    private static String DATASETFILENAME;
    private static String DATASET;
    private static String DATADIRECTORY;

    private ANNSearcherFactory() {
    }

    public static ANNSearcherFactory getInstance() {
        return factory;
    }

    public void setDataset(String datasetFileName) throws FileNotFoundException {

        String datasetName = datasetFileName.substring(0, datasetFileName.lastIndexOf("."));
        String datadirectory = String.format(RESOURCEDIRECTORY + "%s/",
                datasetName);

        // Check if corpus file exists
        if (!Utils.fileExists(datadirectory + datasetFileName)) {
            throw new FileNotFoundException("The dataset does not exist. Please make sure dataset " + datasetFileName + " exists and is placed in the directory: " + RESOURCEDIRECTORY);
        }

        DATASET = datasetName;
        DATASETFILENAME = datasetFileName;
        DATADIRECTORY = datadirectory;

    }


    // ------------ Partition Tree ------------

    public ANNSearcher getTreeSearcher(int maxLeafSize, int L, String type) throws FileNotFoundException {

        if (!type.equals("RP") || !type.equals("RKD")) {
            // Throw exception
        }

        if (DATASETFILENAME == null) {
            throw new FileNotFoundException("No dataset specified.");
        }

        float[][] corpusMatrix = getCorpusMatrix();

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
                searchables = searchables.subList(0, L);
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
        }
        return searchables;
    }

    // ------------ CLASSIC LSH ------------

    public ANNSearcher getLSHSearcher(int K, float r, int L) throws FileNotFoundException {

        if (DATASETFILENAME == null) {
            throw new FileNotFoundException("No dataset specified.");
        }

        float[][] corpusMatrix = getCorpusMatrix();

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
                searchables = searchables.subList(0, L);
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
        
        int d = corpusMatrix[0].length;

        List<Searchable> searchables = new ArrayList<Searchable>(L);
        for (int l = 0; l < L; l++) {
            Searchable hashTable = new HashTable(d, K, r);
            hashTable.fit(corpusMatrix);
            searchables.add(l, hashTable);
        }
        return searchables;
    }


    /* ----------- Collision Counting LSH ----------- */

    public ANNSearcher getC2LSHSearcher(int K, int minSize, int threshold, int L) throws FileNotFoundException {

        if (DATASETFILENAME == null) {
            throw new FileNotFoundException("No dataset specified.");
        }

        float[][] corpusMatrix = getCorpusMatrix();

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
                searchables = searchables.subList(0, L);
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
        
        int d = corpusMatrix[0].length;

        List<Searchable> searchables = new ArrayList<Searchable>(L);
        for (int l = 0; l < L; l++) {
            Searchable hashTable = new C2LSH(d, K, minSize, threshold);
            hashTable.fit(corpusMatrix);
            searchables.add(l, hashTable);
        }
        return searchables;    
    }
    /* ----------- IO Methods ----------- */

    private void writeToDisk(Object object, String directory, String fileName) {
        try (ObjectOutputStream myStream = new ObjectOutputStream(new FileOutputStream(directory + fileName))) {
            myStream.writeObject(object);
            myStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object readFromDisk(String directory, File targetFile) {
        System.out.println("Started loading");
        try (ObjectInputStream myStream = new ObjectInputStream(
                new FileInputStream(directory + targetFile.getName()))) {
            return myStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Finished loading");
        }
        return null;
    }

    private float[][] getCorpusMatrix() {

        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATADIRECTORY + DATASETFILENAME));
        return reader.readFloatMatrix("train");
    }

    private int[][] getSecondIndex(int k, float[][] corpusMatrix) {
        File secondaryIndex = getSecondIndexFile(k);
        int[][] secondaryIndexMatrix;

        if (secondaryIndex == null) {
            System.out.println("No suitable secondary index found. Constructing new index.");
            // Calculate new ground truth
            secondaryIndexMatrix = Utils.groundTruthParallel(corpusMatrix, k);
            writeToDisk(secondaryIndexMatrix, DATADIRECTORY, String.format("%1$s-%2$d.ser", DATASET, k));
        } else {
            secondaryIndexMatrix = (int[][]) readFromDisk(DATADIRECTORY, secondaryIndex);
        }
        return secondaryIndexMatrix;
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
            System.out.println("Loaded Tree from storage: " + file.getName());
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
            System.out.println("Loaded ClassicLSH from storage: " + file.getName());
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
            System.out.println("Loaded C2LSH from storage: " + file.getName());
            return file;
        }
        return null;
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



}

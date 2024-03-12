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
    private static final String RESOURCEDIRECTORY = "app/src/main/resources/";

    private ANNSearcherFactory() {
    }

    public static ANNSearcherFactory getInstance() {
        return factory;
    }


    // ------------ CLASSIC LSH ------------

    public ANNSearcher getLSHSearcher(int K, float r, int L, String dataset) throws FileNotFoundException {

        final String DATASET = dataset;
        final String DATADIRECTORY = String.format(RESOURCEDIRECTORY + "%s/",
                dataset.substring(0, dataset.lastIndexOf(".")));

        // Check if corpus file exists
        if (!Utils.fileExists(DATADIRECTORY + DATASET)) {
            throw new FileNotFoundException("The dataset does not exist. Please make sure dataset " + dataset + " exists and is placed in the directory: " + RESOURCEDIRECTORY);
        }

        // Read corpusmatrix from disk
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATADIRECTORY + DATASET));
        float[][] corpusMatrix = reader.readFloatMatrix("train");


        List<Searchable> searchables;
        File datastructure = getSuitableLSH(DATADIRECTORY, K, r, L);
        
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

    public ANNSearcher getNCLSH(int K, float r, int L, int k, String dataset) throws FileNotFoundException {

        final String DATASET = dataset;
        final String DATASETNAME = dataset.substring(0, dataset.lastIndexOf("."));
        final String DATADIRECTORY = String.format(RESOURCEDIRECTORY + "%s/", DATASETNAME);

        ANNSearcher LSHSearcher = getLSHSearcher(K, r, L, DATASET);

        File secondaryIndex = getSecondIndex(DATADIRECTORY, DATASETNAME, k);
        int[][] secondaryIndexMatrix;

        if (secondaryIndex == null) {
            System.out.println("No suitable secondary index found. Constructing new index.");
            // Calculate new ground truth
            secondaryIndexMatrix = Utils.groundTruthParallel(LSHSearcher.getCorpusMatrix(), k);
            writeToDisk(secondaryIndexMatrix, DATADIRECTORY, String.format("%1$s-%2$d.ser", DATASETNAME, k));
        } else {
            secondaryIndexMatrix = (int[][]) readFromDisk(DATADIRECTORY, secondaryIndex);
        }

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
        try (ObjectInputStream myStream = new ObjectInputStream(
                new FileInputStream(directory + targetFile.getName()))) {
            return myStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getSuitableLSH(String dataDirectory, int K, float r, int L) {

        File directory = new File(dataDirectory);
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

    private File getSecondIndex(String dataDirectory, String dataset, int k) {

        File directory = new File(dataDirectory);
        File[] files = directory.listFiles();

        if (files == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(dataset + "-groundtruth-(\\d+).ser");
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

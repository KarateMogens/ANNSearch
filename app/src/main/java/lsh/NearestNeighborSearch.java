package lsh;

import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;

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
import java.io.ObjectInputStream;

public class NearestNeighborSearch {
    
    private List<EnsembleHash> allEnsembleHash;
    private float[][] corpusMatrix;
    private String DATADIRECTORY;
    private String DATASET;

    //Hyperparameters
    private int L;
    private int K;
    private float r;

    public NearestNeighborSearch(int L, int K, float r, String dataset) throws FileNotFoundException {
        this.DATASET = dataset;
        this.DATADIRECTORY = String.format("src/main/resources/%s/", dataset.substring(0, dataset.lastIndexOf(".")));
        this.L = L;
        this.K = K;
        this.r = r;
        
        // Check if corpus file exists
        if (!Utils.fileExists(DATADIRECTORY + DATASET)) {
            throw new FileNotFoundException("");
        }

        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATADIRECTORY + DATASET));
        corpusMatrix = reader.readFloatMatrix("train");

        File datastructure = getDatastructure();
        if (datastructure == null) {
            buildIndexStructure();
            writeToDisk();
        } else {
            readFromDisk(datastructure);
        }
    }

    private void buildIndexStructure() {

        
        int d = corpusMatrix[0].length;

        allEnsembleHash = new LinkedList<>();
        for (int i = 0; i < L; i++) {
            EnsembleHash hashTable = new EnsembleHash(d, K, r);
            hashTable.fit(corpusMatrix);
            allEnsembleHash.add(hashTable);
        }
    }

    public Set<Integer> search(float[] qVec, int k) {

        Set<Integer> candidateSet = new HashSet<>();
        for (EnsembleHash hash : allEnsembleHash) {
            List<Integer> queryResult = hash.query(qVec);
            if (queryResult == null) {
                continue;
            }
            candidateSet.addAll(hash.query(qVec));
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }

    // ------------ IO HANDLING ------------

    private void writeToDisk() {
        try {
            ObjectOutputStream myStream = new ObjectOutputStream(new FileOutputStream(DATADIRECTORY + String.format("searchType-%1$d-%2$f-%3$d.ser", K, r, L)));
            myStream.writeObject(allEnsembleHash);
            myStream.flush();
            myStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFromDisk(File targetFile) {
        try {
            ObjectInputStream myStream = new ObjectInputStream(new FileInputStream(DATADIRECTORY + targetFile.getName()));
            List<EnsembleHash> datastructure = (List<EnsembleHash>) myStream.readObject();
            this.allEnsembleHash = datastructure.subList(0, L);
            myStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private File getDatastructure() {
        
        File directory = new File(DATADIRECTORY);
        File[] files = directory.listFiles();

        String filePrefix = String.format("searchType-%1$d-%2$f-", K, r);
        System.out.println("FILEPREFIX:" + filePrefix);

        for (File file : files) {
            String fileName = file.getName();

            if (!fileName.startsWith(filePrefix)) {
                continue;
            }
         
            int size = Integer.parseInt(fileName.substring(filePrefix.length(), fileName.lastIndexOf("."))); 
            if (size < L) {
                continue;
            }
            return file;
        }

        return null;
        
    }

}

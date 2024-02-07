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
import java.io.ObjectInputStream;

public class KNNSFactory {
    
    private static final KNNSFactory factory = new KNNSFactory();
    private static final String RESOURCEDIRECTORY = "src/main/resources/";

    private KNNSFactory() {}

    public static KNNSFactory getInstance() {
        return factory;
    }

    // ------------ CLASSIC LSH ------------

    public ClassicLSH getClassicLSH(int L, int K, float r, String dataset) {

        final String DATASET = dataset;
        final String DATADIRECTORY = String.format(RESOURCEDIRECTORY + "%s/", dataset.substring(0, dataset.lastIndexOf(".")));

        // Check if corpus file exists
        if (!Utils.fileExists(DATADIRECTORY + DATASET)) {
            throw new FileNotFoundException("");
        }

        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATADIRECTORY + DATASET));
        float[][] corpusMatrix = reader.readFloatMatrix("train");

        File datastructure = getDatastructure(DATADIRECTORY);
        if (datastructure == null) {
            buildIndexStructure();
            writeToDisk();
        } else {
            readFromDisk(datastructure);
        }


    }

    private void writeToDiskCLSH() {
        try {
            ObjectOutputStream myStream = new ObjectOutputStream(new FileOutputStream(DATADIRECTORY + String.format("searchType-%1$d-%2$f-%3$d.ser", K, r, L)));
            myStream.writeObject(allEnsembleHash);
            myStream.flush();
            myStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFromDiskCLSH(File targetFile) {
        try {
            ObjectInputStream myStream = new ObjectInputStream(new FileInputStream(DATADIRECTORY + targetFile.getName()));
            List<HashTable> datastructure = (List<HashTable>) myStream.readObject();
            this.allEnsembleHash = datastructure.subList(0, L);
            myStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private File getDatastructureCLSH(String dataDirectory) {
        
        File directory = new File(dataDirectory);
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

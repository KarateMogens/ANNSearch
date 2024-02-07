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


public class KNNSFactory {
    
    private static final KNNSFactory factory = new KNNSFactory();
    private static final String RESOURCEDIRECTORY = "src/main/resources/";

    private KNNSFactory() {}

    public static KNNSFactory getInstance() {
        return factory;
    }

    // ------------ CLASSIC LSH ------------

    public ClassicLSH getClassicLSH(int L, int K, float r, String dataset) throws FileNotFoundException {

        final String DATASET = dataset;
        final String DATADIRECTORY = String.format(RESOURCEDIRECTORY + "%s/", dataset.substring(0, dataset.lastIndexOf(".")));

        // Check if corpus file exists
        if (!Utils.fileExists(DATADIRECTORY + DATASET)) {
            throw new FileNotFoundException("");
        }

        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATADIRECTORY + DATASET));
        float[][] corpusMatrix = reader.readFloatMatrix("train");

        ClassicLSH classicLSH = new ClassicLSH(L, K, r, corpusMatrix);

        File datastructure = getDatastructureCLSH(DATADIRECTORY, L, K, r);
        if (datastructure == null) {
            List<HashTable> indexStructure = classicLSH.buildIndexStructure();
            String fileName = String.format("searchType-%1$d-%2$f-%3$d.ser", K, r, L);
            writeToDiskCLSH(indexStructure, DATADIRECTORY, fileName);
        } else {
            classicLSH.setIndexStructure(readFromDiskCLSH(DATADIRECTORY, datastructure, L));
        }

        return classicLSH;
    }

    private void writeToDiskCLSH(List<HashTable> indexStructure, String dataDirectory, String fileName) {
        try {
            ObjectOutputStream myStream = new ObjectOutputStream(new FileOutputStream(dataDirectory + fileName)) ;
            myStream.writeObject(indexStructure);
            myStream.flush();
            myStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<HashTable> readFromDiskCLSH(String dataDirectory, File targetFile, int L) {
        try (ObjectInputStream myStream = new ObjectInputStream(new FileInputStream(dataDirectory + targetFile.getName()));) {
            List<HashTable> datastructure = (List<HashTable>) myStream.readObject();
            return datastructure.subList(0, L);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getDatastructureCLSH(String dataDirectory, int L, int K, float r) {
        
        File directory = new File(dataDirectory);
        File[] files = directory.listFiles();

        String filePrefix = String.format("searchType-%1$d-%2$f-", K, r);

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

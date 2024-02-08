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
import java.util.regex.*;




public class KNNSFactory {
    
    private static final KNNSFactory factory = new KNNSFactory();
    private static final String RESOURCEDIRECTORY = "src/main/resources/";

    private KNNSFactory() {}

    public static KNNSFactory getInstance() {
        return factory;
    }

    private void writeToDisk(Object object, String dataDirectory, String fileName) {
        try (ObjectOutputStream myStream = new ObjectOutputStream(new FileOutputStream(dataDirectory + fileName))) {
            myStream.writeObject(object);
            myStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object readFromDisk(String dataDirectory, File targetFile) {
        try (ObjectInputStream myStream = new ObjectInputStream(new FileInputStream(dataDirectory + targetFile.getName()))) {
            return myStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------ CLASSIC LSH ------------

    public ClassicLSH getClassicLSH(int L, int K, float r, String dataset) throws FileNotFoundException {

        final String DATASET = dataset;
        final String DATADIRECTORY = String.format(RESOURCEDIRECTORY + "%s/", dataset.substring(0, dataset.lastIndexOf(".")));

        // Check if corpus file exists
        if (!Utils.fileExists(DATADIRECTORY + DATASET)) {
            throw new FileNotFoundException("");
        }

        ClassicLSH classicLSH;

        File datastructure = getSuitableCLSH(DATADIRECTORY, L, K, r);

        if (datastructure == null) {
            IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATADIRECTORY + DATASET));
            float[][] corpusMatrix = reader.readFloatMatrix("train");
            classicLSH = new ClassicLSH(L, K, r, corpusMatrix);
            String fileName = String.format("ClassicLSH_%1$d_%2$f_%3$d.ser", K, r, L);
            writeToDisk(classicLSH, DATADIRECTORY, fileName);
        } else {
            classicLSH = (ClassicLSH) readFromDisk(DATADIRECTORY, datastructure);
        }

        return classicLSH;
    }

    private File getSuitableCLSH(String dataDirectory, int L, int K, float r) {
        
        File directory = new File(dataDirectory);
        File[] files = directory.listFiles();

        Pattern pattern = Pattern.compile("ClassicLSH_(\\d+)_(\\d+,\\d+)_(\\d+).ser");

        for (File file : files) {
            String fileName = file.getName();

            Matcher match = pattern.matcher(fileName);

            if (!match.matches() || Integer.parseInt(match.group(1)) != K  || Float.parseFloat(match.group(2).replace(",", ".")) != r) {
                continue;
            }

            if (Integer.parseInt(match.group(3)) < L) {
                continue;
            }

            return file;
        }

        return null;
        
    }

    // ------------ NATURAL CLASSIFIER LSH ------------

    public NCLSH getNCLSH(int L, int K, float r, int k, String dataset) throws FileNotFoundException {

        final String DATASET = dataset;
        final String DATADIRECTORY = String.format(RESOURCEDIRECTORY + "%s/", dataset.substring(0, dataset.lastIndexOf(".")));

        // Check if corpus file exists
        if (!Utils.fileExists(DATADIRECTORY + DATASET)) {
             throw new FileNotFoundException("");
        }

        NCLSH classicLSH;

        File datastructure = getSuitableNCLSH(DATADIRECTORY, L, K, r, k);

        // if (datastructure == null) {
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATADIRECTORY + DATASET));
        float[][] corpusMatrix = reader.readFloatMatrix("train");
        //     classicLSH = new ClassicLSH(L, K, r, corpusMatrix);
        //     String fileName = String.format("searchType-%1$d-%2$f-%3$d.ser", K, r, L);
        //     writeToDisk(classicLSH, DATADIRECTORY, fileName);
        // } else {
        //     classicLSH = (ClassicLSH) readFromDisk(DATADIRECTORY, datastructure);
        // }

        return new NCLSH(L, K, r, corpusMatrix, k);
    }

    private File getSuitableNCLSH(String dataDirectory, int L, int K, float r, int k) {
        
        File directory = new File(dataDirectory);
        File[] files = directory.listFiles();

        Pattern pattern = Pattern.compile("NCLSH_(\\d+)_(\\d+,\\d+)_(\\d+)_(\\d+).ser");

        for (File file : files) {
            String fileName = file.getName();

            Matcher match = pattern.matcher(fileName);

            if (!match.matches() || Integer.parseInt(match.group(1)) != K  || Float.parseFloat(match.group(2)) != r) {
                continue;
            }

            if (Integer.parseInt(match.group(3)) < L) {
                continue;
            }

            if (Integer.parseInt(match.group(4)) < k) {
                continue;
            }

            return file;
        }

        return null;
        
    }

}

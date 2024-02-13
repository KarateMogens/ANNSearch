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

public class ANNSearchableFactory {

    private static final ANNSearchableFactory factory = new ANNSearchableFactory();
    private static final String RESOURCEDIRECTORY = "src/main/resources/";

    private ANNSearchableFactory() {
    }

    public static ANNSearchableFactory getInstance() {
        return factory;
    }

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

    // ------------ CLASSIC LSH ------------

    public ClassicLSH getClassicLSH(int L, int K, float r, String dataset) throws FileNotFoundException {

        final String DATASET = dataset;
        final String DATADIRECTORY = String.format(RESOURCEDIRECTORY + "%s/",
                dataset.substring(0, dataset.lastIndexOf(".")));

        // Check if corpus file exists
        if (!Utils.fileExists(DATADIRECTORY + DATASET)) {
            throw new FileNotFoundException("");
        }

        ClassicLSH classicLSH;
        File datastructure = getSuitableCLSH(DATADIRECTORY, L, K, r);

        if (datastructure == null) {
            System.out.println("No suitable ClassicLSH found. Constructing new object.");
            IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATADIRECTORY + DATASET));
            float[][] corpusMatrix = reader.readFloatMatrix("train");
            classicLSH = new ClassicLSH(L, K, r, corpusMatrix);
            String fileName = String.format("ClassicLSH_%1$d_%2$f_%3$d.ser", K, r, L);
            writeToDisk(classicLSH, DATADIRECTORY, fileName);
        } else {
            classicLSH = (ClassicLSH) readFromDisk(DATADIRECTORY, datastructure);
            classicLSH.reduceIndexSize(L);
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

    // ------------ NATURAL CLASSIFIER LSH ------------

    public NCLSH getNCLSH(int L, int K, float r, int k, String dataset) throws FileNotFoundException {

        final String DATASET = dataset;
        final String DATASETNAME = dataset.substring(0, dataset.lastIndexOf("."));
        final String DATADIRECTORY = String.format(RESOURCEDIRECTORY + "%s/", DATASETNAME);

        // Check if corpus file exists
        if (!Utils.fileExists(DATADIRECTORY + DATASET)) {
            throw new FileNotFoundException("");
        }

        File datastructure = getSuitableNCLSH(DATADIRECTORY, L, K, r);
        NCLSH naturalClassifierLSH;
        if (datastructure == null) {
            System.out.println("No suitable NaturalClassifierLSH found. Constructing new object.");
            IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(DATADIRECTORY + DATASET));
            float[][] corpusMatrix = reader.readFloatMatrix("train");
            naturalClassifierLSH = new NCLSH(L, K, r, corpusMatrix, k);
            String fileName = String.format("NCLSH_%1$d_%2$f_%3$d_%4$d.ser", K, r, L, k);
            writeToDisk(naturalClassifierLSH, DATADIRECTORY, fileName);
        } else {
            naturalClassifierLSH = (NCLSH) readFromDisk(DATADIRECTORY, datastructure);
        }

        File secondaryIndex = getSecondIndex(DATADIRECTORY, DATASETNAME, k);
        int[][] secondaryIndexMatrix;
        if (secondaryIndex == null) {
            System.out.println("No suitable secondary index found. Constructing new index.");
            secondaryIndexMatrix = Utils.groundTruthParallel(naturalClassifierLSH.getCorpusMatrix(), k);
            writeToDisk(secondaryIndexMatrix, DATADIRECTORY, String.format("%1$s-%2$d.ser", DATASETNAME, k));
        } else {
            secondaryIndexMatrix = (int[][]) readFromDisk(DATADIRECTORY, secondaryIndex);
        }

        naturalClassifierLSH.setSecondaryIndex(secondaryIndexMatrix);

        return naturalClassifierLSH;
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

    private File getSuitableNCLSH(String dataDirectory, int L, int K, float r) {

        File directory = new File(dataDirectory);
        File[] files = directory.listFiles();

        if (files == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("NCLSH_(\\d+)_(\\d+,\\d+)_(\\d+)_\\d+.ser");

        for (File file : files) {
            String fileName = file.getName();

            Matcher match = pattern.matcher(fileName);

            if (!match.matches() || Integer.parseInt(match.group(1)) != K || Float.parseFloat(match.group(2).replace(",", ".")) != r) {
                continue;
            }

            if (Integer.parseInt(match.group(3)) < L) {
                continue;
            }

            System.out.println("Loaded NCSLH from storage: " + file.getName());
            return file;
        }

        return null;

    }

}

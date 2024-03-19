/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lsh;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.naming.directory.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import ch.systemsx.cisd.hdf5.*;


public class App {


    public static void main(String[] args) {
     
        String FILEPATH = "app/src/main/resources/fashion-mnist-784-euclidean/fashion-mnist-784-euclidean.hdf5";
        String FILENAME = "fashion-mnist-784-euclidean.hdf5";
    
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(FILEPATH));
        float[][] test = reader.readFloatMatrix("test");
        int[][] neighbors = reader.readIntMatrix("neighbors");
        float[][] train = reader.readFloatMatrix("train");
        reader.close();

        ANNSearcherFactory knnsFactory = ANNSearcherFactory.getInstance();
        try {  
            knnsFactory.setDataset(FILENAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    
        ANNSearcher mySearch;

        try {
            //mySearch = knnsFactory.getNCTreeSearcher(32, 4, 10, "RKD");
            //mySearch = knnsFactory.getLSHSearcher(2, 1.0f, 50);
            //mySearch = knnsFactory.getTreeSearcher(32, 5, "RP");
            //mySearch = knnsFactory.getC2LSHSearcher(50, 1000, 3, 1);
            //int[] locatedNeighbors = mySearch.votingSearch(test[0], 10, 2);
            //int[] locatedNeighbors = mySearch.lookupSearch(test[0], 10);
            //int[] locatedNeighbors = mySearch.naturalClassifierSearch(test[0], 10, 1000);
            int[] locatedNeighbors = Utils.bruteForceKNN(train, test[0], 10);
            List<Integer> actualNeighbors = new LinkedList<>();
            for (int i : Arrays.copyOfRange(neighbors[0], 0, 10)) {
                actualNeighbors.add(i); 
            }
            System.out.println(actualNeighbors.toString());
        
            for (Integer neighbor : locatedNeighbors) {
                System.out.println(neighbor);
                if (actualNeighbors.contains(neighbor)) {
                    System.out.println("Correct"); 
                }
            }
        } 
        //  catch (FileNotFoundException e) {
        //    e.printStackTrace();
        // } 
        finally {
            System.out.println("whatever");
        }
        

        

        // ------------ ERROR FINDING IN GROUND TRUTH -----------------------------
        // String FILEPATH = "src/main/resources/fashion-mnist-784-euclidean/fashion-mnist-784-euclidean.hdf5";
        // FILEPATH = "src/main/resources/fashion-mnist-784-euclidean/fashion-mnist-784-euclidean-groundtruth.h5";
        // String FILENAME = "fashion-mnist-784-euclidean.hdf5";

        // IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(FILEPATH));
        // //float[][] test = reader.readFloatMatrix("test");
        // int[][] neighbors = reader.readIntMatrix("neighbors");
        // reader = HDF5FactoryProvider.get().openForReading(new File("src/main/resources/fashion-mnist-784-euclidean/fashion-mnist-784-euclidean.hdf5"));
        // float[][] train = reader.readFloatMatrix("train");
        // reader.close();

        // int[][] calculatedGroundTruth = new int[1][];

        // try (ObjectInputStream myStream = new ObjectInputStream(new FileInputStream("src/main/resources/fashion-mnist-784-euclidean/fashion-mnist-784-euclidean-groundtruth-10.ser"))) {
        //     calculatedGroundTruth = (int[][]) myStream.readObject();
        // } catch (IOException e) {
        //     e.printStackTrace();
        // } catch (ClassNotFoundException e) {
        //     e.printStackTrace();
        // }

        // for (int i = 0; i < neighbors.length; i++) {
        //     for (int j = 0; j < 10; j++) {
        //         if (calculatedGroundTruth[i][j] != neighbors[i][j]) {
        //             System.out.println("Row " + i +  " - error!");
        //             System.out.println("Calculated: " + calculatedGroundTruth[i][j]);
        //             System.out.println("faiss:" + neighbors[i][j]);
        //             System.out.println(Utils.euclideanDistance(train[calculatedGroundTruth[i][j]], train[i]));
        //             System.out.println(Utils.euclideanDistance(train[neighbors[i][j]], train[i]));
        //     }
        //     }
        // }

        
    }
}

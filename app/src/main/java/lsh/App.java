/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lsh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import ch.systemsx.cisd.hdf5.*;
import javassist.bytecode.analysis.Util;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;


public class App {


    public static void main(String[] args) {


        // //  TEST QUICKSELECT
        // Vote[] votes = new Vote[10];
        // votes[0] = new Vote(0, 1);
        // votes[1] = new Vote(0, 1);
        // votes[2] = new Vote(0, 1);
        // votes[3] = new Vote(0, 1);
        // votes[4] = new Vote(0, 1);
        // votes[5] = new Vote(0, 2);
        // votes[6] = new Vote(0, 2);
        // votes[7] = new Vote(0, 2);
        // votes[8] = new Vote(0, 2);
        // votes[9] = new Vote(0, 2);


        // int partition = Utils.quickSelect(votes, 0, 9, 2);

        // System.out.println("partition index: " + partition);

        // StringBuilder builder = new StringBuilder();
        // for (Vote vote : votes) {
        //     builder.append(" ");
        //     builder.append(vote.getVotes());
        // }
        // System.out.println(builder.toString());

        
     
        String FILEPATH = "src/main/resources/fashion-mnist-784-euclidean/fashion-mnist-784-euclidean.hdf5";
        String FILENAME = "fashion-mnist-784-euclidean.hdf5";
    
        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(FILEPATH));
        float[][] test = reader.readFloatMatrix("test");
        int[][] neighbors = reader.readIntMatrix("neighbors");
        float[][] train = reader.readFloatMatrix("train");
        reader.close();

        ANNSearchableFactory knnsFactory = ANNSearchableFactory.getInstance();
        ANNSearchable mySearch;

        try {
            mySearch = knnsFactory.getNCLSH(5, 1, 12.0f, 10, FILENAME);
            int[] locatedNeighbors = mySearch.search(test[0], 10);
            List<Integer> actualNeighbors = new LinkedList<>();
            for (int i : Arrays.copyOfRange(neighbors[0], 0, 10)) {
                actualNeighbors.add(i); 
            }
            System.out.println(actualNeighbors.toString());
            
            for (Integer neighbor : locatedNeighbors) {
                System.out.println(neighbor);
                if (actualNeighbors.contains(neighbor)) {
                    System.out.println("1");
                }
        }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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

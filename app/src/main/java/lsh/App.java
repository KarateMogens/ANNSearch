/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lsh;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import ch.systemsx.cisd.hdf5.*;


public class App {

    public static void main(String[] args) {
     

        String FILEPATH = "src/main/resources/fashion-mnist-784-euclidean/fashion-mnist-784-euclidean.hdf5";
        String FILENAME = "fashion-mnist-784-euclidean.hdf5";

        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(FILEPATH));
        float[][] test = reader.readFloatMatrix("test");
        int[][] neighbors = reader.readIntMatrix("neighbors");
        NearestNeighborSearch mySearch;
        reader.close();

        try {
            mySearch = new NearestNeighborSearch(6, 1, 25.0f, FILENAME);
            Set<Integer> locatedNeighbors = mySearch.search(test[0], 10);
            List<Integer> actualNeighbors = new LinkedList<>();
            for (int i : neighbors[0]) {
                actualNeighbors.add(i);
            }
            
            for (Integer neighbor : locatedNeighbors) {
                if (actualNeighbors.contains(neighbor)) {
                    System.out.println("1");
                }
        }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        

        
    }
}

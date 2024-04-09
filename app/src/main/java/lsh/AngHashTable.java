package lsh;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import java.util.Random;

public class AngHashTable implements Searchable, Serializable {
    
    private BinaryHash[] hashFunctions;
    private Map<Long, List<Integer>> hashIndex;
    private int K;

    public AngHashTable(int d, int K) {
        this.K = K;
        hashIndex = new HashMap<>();
        hashFunctions = new BinaryHash[K];
        for (int i = 0; i < K; i++) {
            hashFunctions[i] = new BinaryHash(d);
        }
    }

    public void fit(float[][] corpusMatrix) {

        // Hash each individual corpus point using all hashfunctions
        for (int cIndex = 0; cIndex < corpusMatrix.length; cIndex++) {
            float[] cVec = corpusMatrix[cIndex];

            long bin = getBin(cVec);

            // Add index to partition
            List<Integer> partition = hashIndex.get(bin);
            if (partition == null) {
                // Create new set of points if partition isn't stored yet
                partition = new LinkedList<>();
                partition.add(cIndex);
                hashIndex.put(bin, partition);
                continue;
            }
            partition.add(cIndex);
        }
    }


    public Collection<Integer> search(float[] qVec) {
        long bin = getBin(qVec);
        return hashIndex.get(bin);
    }

    private long getBin(float[] vec) {

        // Find the hashValue of hashing vec with each individual hash function {1,...,k}
        int[] hashValues = new int[K];
        for (int i = 0; i < K; i++) {
            hashValues[i] = hashFunctions[i].hash(vec);
        }

        return referenceHash(hashValues);
    }

    private long referenceHash(int[] list) {
        // Hash indidivual binary hash values into a single long value
        long listHashValue = 0L;
        for (int i = 0; i < K; i++) {
            listHashValue = (listHashValue << 1) | list[i];
        }
        return listHashValue;

    }

    

}

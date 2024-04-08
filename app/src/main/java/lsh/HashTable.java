package lsh;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;


public class HashTable implements Searchable, Serializable {

    private HashFunction[] hashFunctions;
    private Map<Long, List<Integer>> hashIndex;
    private int K;
    private long P;
    private long[] listHashing;

    public HashTable(int d, int K, double r) {
        this.K = K;
        hashIndex = new HashMap<>();
        hashFunctions = new HashFunction[K];
        for (int i = 0; i < K; i++) {
            hashFunctions[i] = new HashFunction(d, r);
        }
    }

    public void fit(float[][] corpusMatrix) {

        initP();
        initList();

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

    private void initP() {
        
        /* USE MERSENNE PRIME 2^61 - 1 for reduced collision chance */
        P = (1L << 61) -1;
       
        // Initialize the value P for reference hashing where P > n^2 and P is prime
        // P = corpusSize * corpusSize;
        // while (!Utils.isPrime(P)) {
        //     P++;
        // }
    }

    private void initList() {
        Random randomGen = new Random();

        listHashing = new long[K];
        for (int i = 0; i < K; i++) {
            listHashing[i] = (long) (randomGen.nextFloat()*(P-1));
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
        // Hash indidivual hash values into a single long value
        long listHashValue = 0;
        for (int i = 0; i < list.length; i++) {
            listHashValue = (listHashValue + (list[i] * listHashing[i])) % P;
        }
        return listHashValue;

    }

    
}

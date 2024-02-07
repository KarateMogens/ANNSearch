package lsh;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;


public class HashTable implements Serializable {

    private List<HashFunction> hashFunctions;
    private Map<String, List<Integer>> hashIndex;

    public HashTable(int d, int K, double r) {

        hashIndex = new HashMap<>();

        hashFunctions = new LinkedList<>();
        for (int i = 0; i < K; i++) {
            hashFunctions.add(new HashFunction(d, r));
        }

    }

    public void fit(float[][] corpusMatrix) {
        for (int cIndex = 0; cIndex < corpusMatrix.length; cIndex++) {
            float[] cVec = corpusMatrix[cIndex];

            String label = getLabel(cVec);

            // Add index to partition
            List<Integer> partition = hashIndex.get(label);
            if (partition == null) {
                partition = new LinkedList<>();
                partition.add(cIndex);
                hashIndex.put(label, partition);
                continue;
            }
            partition.add(cIndex);
        }
    }

    public List<Integer> query(float[] qVec) {

        String label = getLabel(qVec);
        return hashIndex.get(label);

    }

    private String getLabel(float[] vec) {

        StringBuilder strBuild = new StringBuilder();
        for (HashFunction hashFunction : hashFunctions) {
            strBuild.append(hashFunction.hash(vec));
        }

        return strBuild.toString();
    }

    class HashFunction implements Serializable {

        private float[] aVec;
        private double b;
        private double r;

        public HashFunction(int d, double r) {

            this.r = r;
            // b is drawn from uniform distribution [0,r]
            Random randomGen = new Random();

            b = randomGen.nextFloat() * r;

            // random d-dimensional vector where each component is drawn from N(0,1)
            aVec = new float[d];
            for (int i = 0; i < d; i++) {
                aVec[i] = (float) randomGen.nextGaussian();

            }

        }

        // Implement constructor with seed value for testing? Will generate same random
        // vector each time

        public int hash(float[] xVec) {

            // Calculate a.x
            float dotProd = Utils.dot(xVec, aVec);

            // Calculate the hash-value
            return (int) Math.floor((dotProd + b) / r);

        }

    }
}

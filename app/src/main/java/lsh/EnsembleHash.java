package lsh;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.io.output.StringBuilderWriter;

public class EnsembleHash {

    private List<HashFunction> hashFunctions;
    private Map<String, List<Integer>> hashIndex;
    private float[][] corpusMatrix;
    
    public EnsembleHash(int k, float[][] corpusMatrix, double r) {

        hashIndex = new HashMap<>();
        this.corpusMatrix = corpusMatrix;
    
        hashFunctions = new LinkedList<>();
        for (int i = 0; i < k; i++) {
            hashFunctions.add(new HashFunction(corpusMatrix[0].length, r));
        }

    }

    public void buildIndex() {
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
        System.out.println(hashIndex.toString());
    }

    private String getLabel(float[] vec) {

        StringBuilder strBuild = new StringBuilder();
        for (HashFunction hashFunction : hashFunctions) {
            strBuild.append(hashFunction.hash(vec));
        }
        
        return strBuild.toString();
    }

    public int[] query(float[] qVec) {
    
        // TO IMPLEMENT
        int[] returnValue = {1,2,3};
        return returnValue;
    }

    class HashFunction {

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

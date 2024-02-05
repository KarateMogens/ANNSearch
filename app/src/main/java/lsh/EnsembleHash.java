package lsh;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

//ADD COMMENT

public class EnsembleHash {

    private List<HashFunction> hashFunctions;
    private Map<String, List<Integer>> hashIndex;
    
    public EnsembleHash(int k, int d, double r) {

        hashFunctions = new LinkedList<>();
        for (int i = 0; i < k; i++) {
            hashFunctions.add(new HashFunction(d, r));
        }   

    }

    public int[] query(double[] qVec) {
    
        // TO IMPLEMENT
        int[] returnValue = {1,2,3};
        return returnValue;
    }

    class HashFunction {

        private double[] aVec;
        private double b;
        private double r;

        public HashFunction(int d, double r) {

            this.r = r;
            // b is drawn from uniform distribution [0,r]
            Random randomGen = new Random();

            b = randomGen.nextDouble() * r;

            // random d-dimensional vector where each component is drawn from N(0,1)
            aVec = new double[d];
            for (int i = 0; i < d; i++) {
                aVec[i] = randomGen.nextGaussian();

            }

        }

        // Implement constructor with seed value for testing? Will generate same random
        // vector each time

        public int hash(double[] xVec) {

            // Calculate a.x
            double dotProd = Utils.dot(xVec, aVec);

            // Calculate the hash-value
            return (int) Math.floor((dotProd + b) / r);

        }

    }
}

package lsh;

import java.io.Serializable;
import java.util.Random;

public class HashFunction implements Serializable {

    private float[] aVec;
    private double b;
    private double r;
    private static Random randomGen = new Random();

    public HashFunction(int d, double r) {

        this.r = r;
        // b is drawn from uniform distribution [0,r]
        
        b = randomGen.nextFloat() * r;

        // random d-dimensional vector where each component is drawn from N(0,1)
        aVec = new float[d];
        for (int i = 0; i < d; i++) {
            aVec[i] = (float) randomGen.nextGaussian();
        }

        aVec = Utils.normalize(aVec);

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
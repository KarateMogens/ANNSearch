package lsh;

import java.util.Random;

public class BinaryHash {
    
    private float[] aVec;
    private static Random randomGen = new Random();

    public BinaryHash(int d) {

        // random d-dimensional vector where each component is drawn from N(0,1)
        aVec = new float[d];
        for (int i = 0; i < d; i++) {
            aVec[i] = (float) randomGen.nextGaussian();
        }

    }

    public int hash(float[] xVec) {

        // Calculate a.x
        float dotProd = Utils.dot(xVec, aVec);

        // Calculate the hash-value
        if (dotProd < 0) {
            return 0;
        }
        return 1;

    }

}
